package com.gmail.berndivader.streamserver.ffmpeg;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
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
import java.util.concurrent.locks.ReentrantLock;
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
import com.github.kokorin.jaffree.ffprobe.FFprobe;
import com.github.kokorin.jaffree.ffprobe.FFprobeResult;
import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.config.Broadcaster;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.discord.DiscordBot;
import com.gmail.berndivader.streamserver.mysql.GetNextScheduled;
import com.gmail.berndivader.streamserver.mysql.GetPlaylist;
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
	
	public static final Object YOUTUBE_LOCK=new Object();
	public static final Object STREAM_LOCK=new Object();
	public static final ReentrantLock PLAYLIST_LOCK=new ReentrantLock();
	
	private static AtomicBoolean stop=new AtomicBoolean(false);
	public static AtomicBoolean hold=new AtomicBoolean(false);
		
	private static volatile FFmpegProgress progress;
	private static volatile FFProbePacket playingPacket;
	private static volatile String message;
	private static volatile File playing;
	private static volatile FFmpegResultFuture ffmpeg;
	
	public static final AtomicInteger index=new AtomicInteger(0);
	
	private static long expiredCounter=0l;
	private static long refreshTimer=0l;
	private static long period=2l;
	
	private static final CopyOnWriteArrayList<File>files=new CopyOnWriteArrayList<File>();
	private static final CopyOnWriteArrayList<File>customs=new CopyOnWriteArrayList<File>();
			
	public static BroadcastRunner instance;
	
	private record GopInfo(int gop, double durationSec) {}	
	
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
		
		try {
			new UpdatePlaylist(true);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			ANSI.error(e.getMessage(),e);
			refreshFilelist();
		}
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
				Packet packet=Broadcast.getLiveBroadcastWithTries(BroadcastStatus.active,2);
				if(packet instanceof EmptyPacket) {
					ANSI.println("[YELLOW]Try to reinstall livebroadcast on Youtube...");
					
					packet=Broadcast.getDefaultLiveStream().get(15l,TimeUnit.SECONDS);
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
					Packet candit=Broadcast.getLiveStreamById(broadcast.contentDetails.boundStreamId).get(15l,TimeUnit.SECONDS);
					if(candit instanceof LiveStreamPacket) {
						LiveStreamPacket live=(LiveStreamPacket)candit;
						ANSI.info("Broadcast is live and livestream is active on Youtube.");
						if(Config.DEBUG) {
							ANSI.info(broadcast.toString());
							ANSI.info(live.toString());
						}
					} else {
						ANSI.error("No useable livestream resource found on YT.",null);
					}
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
    	List<File>playlistNewFiles=Arrays.asList(getFiles(playlistDir,filter));
    	List<File>customNewFiles=Arrays.asList(getFiles(customDir,filter));
		
		PLAYLIST_LOCK.lock();
		try {
	    	
	    	managePlaylist(playlistNewFiles);
	    } finally {
			PLAYLIST_LOCK.unlock();
		}
		
		files.clear();
		files.addAll(playlistNewFiles);
    	
		customs.clear();
		customs.addAll(customNewFiles);
	}
	
	private static void managePlaylist(List<File>files) {
		
		ANSI.info("Check playlist for new files...[BR]");
		
		GetPlaylist playlist=new GetPlaylist();
		try {
			ArrayList<String>list=playlist.future.get(15l,TimeUnit.SECONDS);
			ArrayList<File>converts=new ArrayList<File>();
			
			for(File candit:files) {
				if(!list.contains(candit.getCanonicalPath())) {
					if(!isKeyframeFixed(candit)) {
						converts.add(candit);
					}
				}
			}
			
			if(converts.size()>0) {
				convertFile(converts);
			}
			
		} catch (IOException| InterruptedException | ExecutionException | TimeoutException e) {
			ANSI.error(e.getMessage(),e);
		}
		
	}
	
	private static void convertFile(ArrayList<File>files) {
		Iterator<File>iterator=files.iterator();
		int size=files.size();
		AtomicInteger count=new AtomicInteger(0);
		
		while(iterator.hasNext()) {
			File file=iterator.next();
			count.incrementAndGet();

			if(!file.exists()) {
				iterator.remove();
				continue;
			}

			try {
				GopInfo info=getGOP(file);
				if(info.gop==-1) {
					iterator.remove();
					continue;
				}

				double durationMS=info.durationSec*1000d;
				Path outputPath=Paths.get(file.getParent(),"fixed_"+file.getName());

				FFmpeg.atPath(Paths.get(Config.DOWNLOADER.FFMPEG_PATH).getParent())
					.addInput(UrlInput.fromPath(file.toPath()))
					.setOverwriteOutput(true)
					.addArguments("-c:v","libx264")
					.addArguments("-preset","fast")
					.addArguments("-crf","20")
					.addArguments("-g",String.valueOf(info.gop))
					.addArguments("-keyint_min",String.valueOf(info.gop))
					.addArguments("-sc_threshold","0")
					.addArguments("-c:a","aac")
					.addArguments("-b:a","160k")
					.addArguments("-ar","44100")
					.addArguments("-movflags","+faststart")
					.addOutput(UrlOutput.toPath(outputPath))
					.setProgressListener(prog-> {
						double percent=durationMS>0d?100d*prog.getTimeMillis()/durationMS:0d;
						ANSI.print("[CR][DL]("+count.get()+" of "+size+") Convert file "+file.getName()+" - "+String.format("%.1f%% done.",percent));
				})
				.execute();

				File converted=outputPath.toFile();
				if(converted.exists()&&converted.length()>0) {
					Files.move(outputPath,file.toPath(),StandardCopyOption.REPLACE_EXISTING);
				} else {
					if(converted.exists()) converted.delete();
					iterator.remove();
				}

			} catch(Exception e) {
				ANSI.error("Error at "+file.getName()+":"+e.getMessage(),e);
				iterator.remove();
			}
			ANSI.print("[BR]");
			
		}
	}	
	
	private static GopInfo getGOP(File file) {
		int gop=-1;
		double duration=-1;
		
		String path=file.getAbsolutePath();
		try {
			path=file.getCanonicalPath();
		} catch (IOException e) {
			ANSI.error(e.getMessage(),e);
		}
		
		FFprobeResult result=FFprobe.atPath(Paths.get(Config.DOWNLOADER.FFPROBE_PATH).getParent())
				.setShowStreams(true)
				.setShowFormat(true)
				.setInput(path)
				.execute();
		
		double rFrameRate=-1;
		for(com.github.kokorin.jaffree.ffprobe.Stream stream:result.getStreams()) {
			if(stream.getCodecType()==StreamType.VIDEO) {
				rFrameRate=stream.getRFrameRate().doubleValue();
				break;
			}
		}
		
		if(rFrameRate!=-1) gop=(int)Math.round(rFrameRate*2);
		duration=result.getFormat().getDuration();
		
		return new GopInfo(gop,duration);
	}
	
	private static boolean isKeyframeFixed(File file) {
		try {
			FFprobeResult result=
					FFprobe.atPath(Paths.get(Config.DOWNLOADER.FFPROBE_PATH).getParent())
						.setShowPackets(true)
						.setSelectStreams(StreamType.VIDEO)
						.setShowEntries("packet=pts_time,flags")
						.setInput(file.getAbsolutePath())
						.execute();
			
			List<com.github.kokorin.jaffree.ffprobe.Packet>packets=result.getPackets();
			if(packets==null||packets.isEmpty()) return false;
			
			List<Float>keyTimes=new ArrayList<Float>();
			for(com.github.kokorin.jaffree.ffprobe.Packet packet:packets) {
				String flags=packet.getFlags();
				if(flags!=null&&flags.contains("K")) {
					Float pts=packet.getPtsTime();
					if(pts!=null) keyTimes.add(pts);
				}
			}
			
			if(keyTimes.size()<3) return false;
			
			int bad=0;
			int size=keyTimes.size();
			for(int i=1;i<size;i++) {
				double diff=keyTimes.get(i)-keyTimes.get(i-1);
				if(diff>2.5d) bad++;
			}
			
			return bad<=size*0.1d;
		
		} catch(Exception e) {
			ANSI.error(e.getMessage(),e);
		}
		return false;
	}
	
	
}
