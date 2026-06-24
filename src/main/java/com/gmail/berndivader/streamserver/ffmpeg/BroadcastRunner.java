package com.gmail.berndivader.streamserver.ffmpeg;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import com.github.kokorin.jaffree.StreamType;
import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.FFmpegProgress;
import com.github.kokorin.jaffree.ffmpeg.FFmpegResult;
import com.github.kokorin.jaffree.ffmpeg.FFmpegResultFuture;
import com.github.kokorin.jaffree.ffmpeg.OutputListener;
import com.github.kokorin.jaffree.ffmpeg.ProgressListener;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import com.github.kokorin.jaffree.ffmpeg.UrlOutput;
import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.config.Broadcaster;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.discord.DiscordBot;
import com.gmail.berndivader.streamserver.mysql.GetNextScheduled;
import com.gmail.berndivader.streamserver.mysql.UpdateCurrent;
import com.gmail.berndivader.streamserver.mysql.UpdatePlaylist;
import com.gmail.berndivader.streamserver.term.ANSI;
import com.gmail.berndivader.streamserver.youtube.Broadcast;
import com.gmail.berndivader.streamserver.youtube.BroadcastStatus;
import com.gmail.berndivader.streamserver.youtube.PrivacyStatus;
import com.gmail.berndivader.streamserver.youtube.packets.EmptyPacket;
import com.gmail.berndivader.streamserver.youtube.packets.ErrorPacket;
import com.gmail.berndivader.streamserver.youtube.packets.LiveBroadcastPacket;
import com.gmail.berndivader.streamserver.youtube.packets.LiveStreamPacket;
import com.gmail.berndivader.streamserver.youtube.packets.Packet;
import com.gmail.berndivader.streamserver.youtube.packets.UnknownPacket;

public final class BroadcastRunner extends TimerTask {
	
	private static final Object YOUTUBE_LOCK=new Object();
	private static final Object STREAM_LOCK=new Object();
	
	private static AtomicBoolean stop=new AtomicBoolean(false);
	public static AtomicBoolean hold=new AtomicBoolean(false);
	
	private static volatile FFmpegProgress progress;
	private static volatile FFProbePacket playingPacket;
	private static volatile String message;
	private static volatile File playing;
	private static volatile FFmpegResultFuture ffmpeg;
	
	public static final AtomicInteger index=new AtomicInteger(0);
	public static Packet liveBroadcast=Packet.emtpy();
	public static Packet liveStream=Packet.emtpy();
	
	private static long expiredCounter=0l;
	private static long refreshTimer=0l;
	private static long period=2l;
	
	private static final CopyOnWriteArrayList<File>files=new CopyOnWriteArrayList<File>();
	private static final CopyOnWriteArrayList<File>customs=new CopyOnWriteArrayList<File>();
	
	public static BroadcastRunner instance;
	
	
	public static FFmpegProgress progress() {
		return progress;
	}
	
	public static void progress(FFmpegProgress progress) {
		BroadcastRunner.progress=progress;
	}
	
	public static FFProbePacket playingPacket() {
		return playingPacket;
	}
	
	public static void playingPacket(FFProbePacket packet) {
		BroadcastRunner.playingPacket=packet;
	}
	
	public static String message() {
		return message;
	}
	
	public static void message(String message) {
		BroadcastRunner.message=message;
	}
	
	public static File playing() {
		return playing;
	}
	
	public static void playing(File playing) {
		BroadcastRunner.playing=playing;
	}
	
	public static FFmpegResultFuture ffmpeg() {
		return ffmpeg;
	}
	
	public static void ffmpeg(FFmpegResultFuture ffmpeg) {
		BroadcastRunner.ffmpeg=ffmpeg;
	}
	
	public BroadcastRunner() {
		ANSI.print("[YELLOW]Starting BroadcastRunner...");

		stop.set(false);
		hold.set(false);
		
		refreshFilelist();
		shuffleFilelist();
		
		checkOrReInitiateLiveBroadcast(Config.BROADCASTER.BROADCAST_DEFAULT_TITLE,Config.BROADCASTER.BROADCAST_DEFAULT_DESCRIPTION,Config.broadcastPrivacyStatus());
		startStream();
		
		Helper.SCHEDULED_EXECUTOR.scheduleAtFixedRate(this,0l,period,TimeUnit.SECONDS);
		ANSI.println("[GREEN]DONE![RESET]");
	}
	
	public void stop() throws InterruptedException {
		if(!stop.get()) {
			synchronized(STREAM_LOCK) {
				
				ANSI.print("[YELLOW]Stopping BroadcastRunner...");
				stop.set(true);
				FFmpegResultFuture current=ffmpeg();
		    	if(current!=null&&(!current.isCancelled()||!current.isDone())) {
		    		ANSI.print("[YELLOW][Stop broadcasting...");
		    		
		    		FFmpegResult result=stopStream();
		    		if(result!=null) {
						ANSI.print("[GREEN]DONE!]...[RESET]");
		    		} else {
						ANSI.warn("Problem occured while stopping the BroadcastRunner.");
		    		}
		    		
		    	}
		    	ANSI.println("[GREEN]DONE![RESET]");
				
			}
		}
	}
	
	@Override
	public void run() {
				
		if(!stop.get()) {
			refreshTimer+=period;
			expiredCounter+=period;
			if(!hold.get()) {
				FFmpegResultFuture current=ffmpeg();
	    		if(current==null||current.isCancelled()||current.isDone()) startStream();
				if(expiredCounter>Broadcaster.YOUTUBE_TOKEN_EXPIRE_TIME) {
					checkOrReInitiateLiveBroadcast(Config.BROADCASTER.BROADCAST_DEFAULT_TITLE,Config.BROADCASTER.BROADCAST_DEFAULT_DESCRIPTION,Config.broadcastPrivacyStatus());
					expiredCounter=0l;
				}
				if(refreshTimer>Broadcaster.PLAYLIST_REFRESH_INTERVAL) {
					refreshTimer=0l;
					refreshFilelist();
					shuffleFilelist();
					try {
						new UpdatePlaylist(false);
						hold.set(files.size()==0);
					} catch (InterruptedException | ExecutionException | TimeoutException e) {
						ANSI.error("Failed to update playlist.",e);
					}
				}
			}
			
		}
		
	}
	
	public static void checkOrReInitiateLiveBroadcast(String title,String description,PrivacyStatus privacy) {
		synchronized(YOUTUBE_LOCK) {
			
			if(Config.DEBUG) ANSI.info("[BLUE]Test if broadcast is still live on Youtube...[RESET]");
			try {
				Packet packet=liveBroadcast=Broadcast.getLiveBroadcastWithTries(BroadcastStatus.active,2);
				if(packet instanceof EmptyPacket) {
					ANSI.println("[YELLOW]Try to reinstall livebroadcast on Youtube...");
					
					packet=liveStream=Broadcast.getDefaultLiveStream().get(15l,TimeUnit.SECONDS);
					if(packet instanceof LiveStreamPacket) {
						ANSI.println("[GREEN]Got livestream resource identified by STREAM_KEY...");
						
						LiveStreamPacket live=(LiveStreamPacket)packet;
						packet=Broadcast.insertLiveBroadcast(title,description,privacy).get(15l,TimeUnit.SECONDS);
						if(packet instanceof LiveBroadcastPacket) {
							ANSI.println("[GREEN]Installed a new autostart livebroadcast resource on Youtube...");
							
							LiveBroadcastPacket broadcast=(LiveBroadcastPacket)packet;
							packet=Broadcast.bindBroadcastToStream(broadcast.id,live.id).get(15l,TimeUnit.SECONDS);
							if(packet instanceof LiveBroadcastPacket) {
								ANSI.println("[GREEN]Merged the default livestream with the new livebroadcast together...");
								
								broadcast=(LiveBroadcastPacket)packet;
								if(Config.DEBUG) ANSI.info(broadcast.source().toString());
								
								ANSI.println("[BLUE]The new livestream should go live in a few seconds.[PROMPT]");
							}
						}
					}
					
					if(packet instanceof ErrorPacket) {
						ANSI.println("[RED]FAILED!");
						ErrorPacket error=(ErrorPacket)packet;
						error.printSimple();
					} else if(packet instanceof EmptyPacket) {
						ANSI.println("[RED]FAILED!");
						ANSI.warn("Received EmptyPacket!");
						if(Config.DEBUG) ANSI.info(packet.source().toString());
					} else if(packet instanceof UnknownPacket){
						ANSI.println("[RED]FAILED!");
						ANSI.warn("Unknown packet received!");
						if(Config.DEBUG) ANSI.info(packet.source().toString());
					}
					
				} else if(packet instanceof ErrorPacket) {
					ErrorPacket error=(ErrorPacket)packet;
					error.printSimple();
				} else if(packet instanceof LiveBroadcastPacket) {
					LiveBroadcastPacket broadcast=(LiveBroadcastPacket)packet;
					ANSI.println("Broadcast is live on Youtube.");
					if(Config.DEBUG) ANSI.info(broadcast.source().toString());
				}
			} catch(Exception e) {
				ANSI.error("Failed to restart live broadcast on Youtube.",e);
			}
			
		}
	}
		
	private static void startStream() {
		synchronized(STREAM_LOCK) {
			
			GetNextScheduled scheduled=new GetNextScheduled();
			try {
				String name=scheduled.future.get(20,TimeUnit.SECONDS);
				if(name!=null) {
					getFileByName(name.toLowerCase()).ifPresent(BroadcastRunner::createStream);
					return;
				}
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				ANSI.error("Get next scheduled file failed.",e);
			}
			
			File[]files=getFiles();
			if(files.length>0) {
				createStream(files[index.get()]);
				index.set((index.get()+1)%files.length);
			} else {
				ANSI.info("Broadcasting is on hold because there are no mediafiles inside playlist dirctory.");
				BroadcastRunner.hold.set(true);
			}
			
		}
	}
	
	private static void createStream(File file) {

		playingPacket(FFProbePacket.build(file));
		
		String title="";
		if(playingPacket().isSet(playingPacket().tags.title)) {
			title=playingPacket().tags.title;
		} else {
			int pos=file.getName().lastIndexOf(".");
			if(pos>0) {
				title=file.getName().substring(0,pos);
			} else {
				title=file.getName();
			}
		}
				
		new UpdateCurrent(title,playingPacket().toString());
		
		if(Config.DISCORD.BOT_START&&DiscordBot.instance!=null) DiscordBot.instance.updateStatus(title);
		
		if(Config.DEBUG) {
			ANSI.info("[BLUE]Now playing: "
					+playingPacket().tags.title
					+":"+playingPacket().tags.artist
					+":"+playingPacket().tags.date
					+":"+playingPacket().tags.comment+"[RESET]");
			ANSI.prompt();
		}
		
		Path path=Paths.get(Config.DOWNLOADER.FFMPEG_PATH);
		
		ffmpeg(FFmpeg.atPath(path.getParent())
				.addInput(UrlInput.fromUrl(file.getAbsolutePath())
						.addArgument("-re")
						)
				.addOutput(UrlOutput.toUrl(Config.BROADCASTER.YOUTUBE_STREAM_URL+"/"+Config.BROADCASTER.YOUTUBE_STREAM_KEY)
						.setCodec(StreamType.VIDEO,"copy")
						.setCodec(StreamType.AUDIO,"copy")
						.addArguments("-strict","-2")
						.addArguments("-flags","+global_header")
						.addArguments("-bsf:a","aac_adtstoasc")
						.addArguments("-bufsize","2100k")
						.setFormat("flv")
						)
				.setOutputListener(new OutputListener() {
					@Override
					public void onOutput(String message) {
						BroadcastRunner.message(message);
					}
				})
				.setProgressListener(new ProgressListener() {
					@Override
					public void onProgress(FFmpegProgress progress) {
						BroadcastRunner.progress(progress);
					}
				})
				.setOverwriteOutput(true)
				.executeAsync());
		
		FFmpegResultFuture current=ffmpeg();
		if(current!=null&&!current.isDone()&&!current.isCancelled()) playing(file);
			
	}
	
	public static boolean isStreaming() {
		FFmpegResultFuture current=ffmpeg();
		return current!=null&&!current.isCancelled()&&!current.isDone();
	}
	
	public static void playFile(File file) {
		synchronized(STREAM_LOCK) {
			if(isStreaming()) {
				stopStream();
				createStream(file);
			}
		}
	}
	
	public static void playPosition(int idx) {
		index.set(idx);
		synchronized(STREAM_LOCK) {
			if(isStreaming()) {
				stopStream();
			}
		}
	}
	
	public static void restart() {
		synchronized(STREAM_LOCK) {
			if(isStreaming()) stopStream();
			createStream(playing());
		}
	}
	
	public static void next() {
		synchronized(STREAM_LOCK) {
			if(isStreaming()) {
				stopStream();
			}
		}
	}
	
	public static void previous() {
		synchronized(STREAM_LOCK) {
			if(isStreaming()) {
				File[]files=getFiles();
				index.set((index.get()-2+files.length)%files.length);
				stopStream();
			}
		}
	}
	
	private static FFmpegResult stopStream() {
		FFmpegResultFuture current=ffmpeg();
		if(current!=null&&!current.isCancelled()&&!current.isDone()) {
			current.graceStop();
			try {
				return current.get(20l,TimeUnit.SECONDS);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				ANSI.error("Failed to stop broadcast task.",e);
			}
		}
		return null;
	}
	
	public static File[] getFiles() {
		return files.toArray(File[]::new);
	}
	
	public static Optional<File> getFileByName(String name) {
		File file=null;
		
		int pos=getFilePosition(name);
		if(pos!=-1) {
			file=files.get(pos);
			if(file.exists()&&file.isFile()&&file.canRead()) {	
				return Optional.of(file);
			}
		}
		
		pos=getCustomFilePosition(name);
		if(pos!=-1) {
			file=customs.get(pos);
			if(file.exists()&&file.isFile()&&file.canRead()) {	
				return Optional.of(file);
			}
		}
		
		return Optional.empty();
	}
	
	private static int getFilePosition(String name) {
		if(!name.isEmpty()) {
		    for(int i=0;i<files.size();i++) {
		        if(files.get(i).getName().equalsIgnoreCase(name)) return i;
		    }
		}
		return -1;
	}
	
	private static int getCustomFilePosition(String name) {
		
		if(!name.isEmpty()) {
			for(int i=0;i<customs.size();i++) {
		        if(customs.get(i).getName().equalsIgnoreCase(name)) return i;
			}
		}
		return -1;
	}
	
	public static List<String> getFilesAsList(String r) {
	    String regex=r.contains("*")?r.replaceAll("\\*","(.*)"):"(.*)"+r+"(.*)";
	    List<String>list=new ArrayList<String>();
	    
	    Stream.concat(files.stream(),customs.stream())
        .map(File::getName)
        .filter(name->{
        	
            try {
                return name.toLowerCase().matches(regex);
            } catch(Exception e) {
                if(Config.DEBUG) ANSI.error("getFilelistAsList method failed.",e);
            }
            return false;
            
        }).forEach(list::add);
	    	    
	    return list;
	}

	public static String getFilesAsString(String r) {
	    AtomicInteger count=new AtomicInteger(0);
	    StringBuilder playlist = new StringBuilder();
	    String regex=r.contains("*")?r.replaceAll("\\*","(.*)"):"(.*)"+r+"(.*)";
	    
	    Stream.concat(files.stream(),customs.stream())
    	.map(File::getName)
    	.filter(name-> {
    		
			try {
				return name.toLowerCase().matches(regex);
			} catch (Exception e) {
				if(Config.DEBUG) ANSI.error("getFilelistAsString method failed.",e);
			}
			return false;
    		
    	}).forEach(name->{
    		playlist.append(name+"\n");
    		count.incrementAndGet();
        });
	    
	    playlist.append("\nThere are ").append(count).append(" matches for ").append(regex);
	    return playlist.toString();
	}
	
	public static void shuffleFilelist() {
		Random random=ThreadLocalRandom.current();
		
		int size=files.size();
		for (int i1=size-1;i1>0;i1--) {
			int index=random.nextInt(i1+1);
			File a=files.get(index);
			files.set(index,files.get(i1));
			files.set(i1,a);
		}
		
	}
		
	
	private static File[] getFiles(File directory,FileFilter filter) {
		if(directory.exists()) {
	    	if(directory.isDirectory()) {
	    		return directory.listFiles(filter);
	    	} else if(directory.isFile()) {
	    		return new File[] {directory};
	    	}
		}
		
		return new File[0];
	}
	
	public static void refreshFilelist() {
    	File playlistDir=new File(Config.working_dir,Config.BROADCASTER.PLAYLIST_PATH);
    	File customDir=new File(Config.working_dir,Config.BROADCASTER.PLAYLIST_PATH_CUSTOM);
    	
    	FileFilter filter=pathName->pathName.getAbsolutePath().toLowerCase().endsWith(".mp4");
    	List<File>newFiles=Arrays.asList(getFiles(playlistDir,filter));
		files.clear();
		files.addAll(newFiles);
    	newFiles=Arrays.asList(getFiles(customDir,filter));
		customs.clear();
		customs.addAll(newFiles);
    	
	}
	
}
