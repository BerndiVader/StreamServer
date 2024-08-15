package com.gmail.berndivader.streamserver.ffmpeg;

import java.io.File;
import java.io.FileFilter;
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
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.discord.DiscordBot;
import com.gmail.berndivader.streamserver.mysql.GetNextScheduled;
import com.gmail.berndivader.streamserver.mysql.UpdateCurrent;
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
	
	boolean stop;
	public static boolean hold;
	
	private static FFmpegProgress progress;
	private static FFProbePacket playingPacket;
	private static String message;
	private static File playing;
	private static FFmpegResultFuture ffmpeg;
	public static AtomicInteger index=new AtomicInteger();
	public static Packet liveBroadcast=Packet.emtpy();
	public static Packet liveStream=Packet.emtpy();
	
	private static long counter;
	
	private static CopyOnWriteArrayList<File>files;
	private static CopyOnWriteArrayList<File>customs;
	
	public static BroadcastRunner instance;
	
	static {
		files=new CopyOnWriteArrayList<File>();
		customs=new CopyOnWriteArrayList<File>();
	}
	
	public static synchronized FFmpegProgress progress() {
		return progress;
	}
	
	public static synchronized void progress(FFmpegProgress progress) {
		BroadcastRunner.progress=progress;
	}
	
	public static synchronized FFProbePacket playingPacket() {
		return playingPacket;
	}
	
	public static synchronized void playingPacket(FFProbePacket packet) {
		BroadcastRunner.playingPacket=packet;
	}
	
	public static synchronized String message() {
		return message;
	}
	
	public static synchronized void message(String message) {
		BroadcastRunner.message=message;
	}
	
	public static synchronized File playing() {
		return playing;
	}
	
	public static synchronized void playing(File playing) {
		BroadcastRunner.playing=playing;
	}
	
	public static synchronized FFmpegResultFuture ffmpeg() {
		return ffmpeg;
	}
	
	public static synchronized void ffmpeg(FFmpegResultFuture ffmpeg) {
		BroadcastRunner.ffmpeg=ffmpeg;
	}
	
	public BroadcastRunner() {
		ANSI.print("[YELLOW]Starting BroadcastRunner...");

		stop=false;
		hold=false;
		
		refreshFilelist();
		shuffleFilelist();
		
		checkOrReInitiateLiveBroadcast(Config.BROADCAST_DEFAULT_TITLE,Config.BROADCAST_DEFAULT_DESCRIPTION,Config.broadcastPrivacyStatus());

		index.set(0);
		counter=0l;
		startStream();
		
		Helper.SCHEDULED_EXECUTOR.scheduleAtFixedRate(this, 0l, 2l, TimeUnit.SECONDS);
		ANSI.println("[GREEN]DONE![RESET]");
	}
	
	public void stop() throws InterruptedException {
		ANSI.print("[YELLOW]Stopping BroadcastRunner...");
		
		stop=true;
    	if(ffmpeg()!=null&&(!ffmpeg().isCancelled()||!ffmpeg().isDone())) {
    		ANSI.print("[YELLOW][Stop broadcasting...");
    		
    		FFmpegResult result=stopStream();
    		if(result!=null) {
				ANSI.print("[GREEN]DONE!]...[RESET]");
    		} else {
				ANSI.printWarn("Problem occured while stopping the BroadcastRunner.");
    		}
    		
    	}
		
    	ANSI.println("[GREEN]DONE![RESET]");
	}
	
	@Override
	public void run() {

		if(!stop&&!hold) {
			counter++;
    		if(ffmpeg()==null||ffmpeg().isCancelled()||ffmpeg().isDone()) startStream();
			if(counter>1799l) {
				
				checkOrReInitiateLiveBroadcast(Config.BROADCAST_DEFAULT_TITLE,Config.BROADCAST_DEFAULT_DESCRIPTION,Config.broadcastPrivacyStatus());
				counter=0l;
			}
		}
		
	}
	
	public static synchronized void checkOrReInitiateLiveBroadcast(String title,String description,PrivacyStatus privacy) {
		if(Config.DEBUG) ANSI.println("[BLUE]Test if broadcast is still live on Youtube...[RESET]");
		try {
			Packet packet=liveBroadcast=Broadcast.getLiveBroadcastWithTries(BroadcastStatus.active,2);
			if(packet instanceof EmptyPacket) {
				ANSI.println("[ORANGE]Try to reinstall livebroadcast on Youtube...");
				
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
							if(Config.DEBUG) ANSI.println(broadcast.source().toString());
							
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
					ANSI.printWarn("Received EmptyPacket!");
					if(Config.DEBUG) ANSI.println(packet.source().toString());
				} else if(packet instanceof UnknownPacket){
					ANSI.println("[RED]FAILED!");
					ANSI.printWarn("Unknown packet received!");
					if(Config.DEBUG) ANSI.println(packet.source().toString());
				}
				
			} else if(packet instanceof ErrorPacket) {
				ErrorPacket error=(ErrorPacket)packet;
				error.printSimple();
			} else if(packet instanceof LiveBroadcastPacket) {
				LiveBroadcastPacket broadcast=(LiveBroadcastPacket)packet;
				ANSI.println("Broadcast is live on Youtube.");
				if(Config.DEBUG) ANSI.println(broadcast.source().toString());
			}
		} catch(Exception e) {
			ANSI.printErr("Failed to restart live broadcast on Youtube.",e);
		}
	}
		
	private static synchronized void startStream() {
		
		GetNextScheduled scheduled=new GetNextScheduled();
		try {
			String name=scheduled.future.get(20,TimeUnit.SECONDS);
			if(name!=null) {
				getFileByName(name.toLowerCase()).ifPresent(BroadcastRunner::createStream);
				return;
			}
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			ANSI.printErr("Get next scheduled file failed.",e);
		}
		
		File[]files=getFiles();
		if(files.length>0) {
			createStream(getFiles()[index.get()]);
			index.set((index.get()+1)%getFiles().length);
		} else {
			BroadcastRunner.hold=true;
		}
	}
	
	private static synchronized void createStream(File file) {
		String path=file.getAbsolutePath();
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
				
		String info=playingPacket().tags.artist+":"+Helper.stringFloatToTime(playingPacket().duration)+":"+playingPacket().tags.description;
		new UpdateCurrent(title, info);
		
		if(Config.DISCORD_BOT_START&&DiscordBot.instance!=null) DiscordBot.instance.updateStatus(title);
		
		ANSI.println("[BLUE]Now playing: "
			+playingPacket().tags.title
			+":"+playingPacket().tags.artist
			+":"+playingPacket().tags.date
			+":"+playingPacket().tags.comment+"[RESET]");
		ANSI.prompt();
				
		ffmpeg(FFmpeg.atPath()
				.addInput(UrlInput.fromUrl(path)
						.addArgument("-re")
						)
				.addOutput(UrlOutput.toUrl(Config.YOUTUBE_STREAM_URL+"/"+Config.YOUTUBE_STREAM_KEY)
						.setCodec(StreamType.VIDEO,"copy")
						.addArguments("-b:v","2M")
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
				}).setOverwriteOutput(true).executeAsync());
		if(ffmpeg()!=null&&!ffmpeg().isDone()&&!ffmpeg().isCancelled()) playing(file);
	}
	
	public static boolean isStreaming() {
		return ffmpeg()!=null&&!ffmpeg().isCancelled()&&!ffmpeg().isDone();
	}
	
	public static void playFile(File file) {
		if(isStreaming()) {
			stopStream();
			createStream(file);
		}
	}
	
	public static void playPosition(int idx) {
		index.set(idx);
		if(isStreaming()) stopStream();
	}
	
	public static void restart() {
		if(isStreaming()) stopStream();
		createStream(playing());
	}
	
	public static void next() {
		if(isStreaming()) stopStream();		
	}
	
	public static void previous() {
		if(isStreaming()) {
			index.set((index.get()-2+getFiles().length)%getFiles().length);
			stopStream();
		}
	}
	
	private static FFmpegResult stopStream() {
		if(ffmpeg()!=null&&!ffmpeg().isCancelled()&&!ffmpeg().isDone()) {
			ffmpeg().graceStop();
			try {
				return ffmpeg().get(20l,TimeUnit.SECONDS);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				ANSI.printErr("Failed to stop broadcast task.",e);
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
		} else {
			pos=getCustomFilePosition(name);
			if(pos!=-1) file=customs.get(pos);
		}
		if(file!=null&&file.exists()&&file.isFile()&&file.canRead()) return Optional.of(file);
		return Optional.empty();
	}	
	
	public static int getFilePosition(String name) {
		if(!name.isEmpty()) {
			int size=files.size();
		    for(int i=0;i<size;i++) {
		        if(files.get(i).getName().equalsIgnoreCase(name)) return i;
		    }
		}
		return -1;
	}
	
	public static int getCustomFilePosition(String name) {
		if(!name.isEmpty()) {
			int size=customs.size();
			for(int i=0;i<size;i++) {
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
	                if(Config.DEBUG) ANSI.printErr("getFilelistAsList method failed.",e);
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
					if(Config.DEBUG) ANSI.printErr("getFilelistAsString method failed.",e);
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
		synchronized(files) {
			int size=files.size();
			for (int i1=size-1;i1>0;i1--) {
				int index=random.nextInt(i1+1);
				File a=files.get(index);
				files.set(index,files.get(i1));
				files.set(i1,a);
			}
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
    	File playlistDir=new File(Config.working_dir,Config.PLAYLIST_PATH);
    	File customDir=new File(Config.working_dir,Config.PLAYLIST_PATH_CUSTOM);
    	
    	FileFilter filter=pathName->pathName.getAbsolutePath().toLowerCase().endsWith(".mp4");
    	synchronized(files) {
        	files=new CopyOnWriteArrayList<File>(Arrays.asList(getFiles(playlistDir,filter)));
		}
    	synchronized(customDir) {
        	customs=new CopyOnWriteArrayList<File>(Arrays.asList(getFiles(customDir,filter)));
		}
    	
	}
	
	
}
