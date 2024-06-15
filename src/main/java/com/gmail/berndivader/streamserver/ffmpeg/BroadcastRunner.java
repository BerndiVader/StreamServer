package com.gmail.berndivader.streamserver.ffmpeg;

import java.io.File;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.github.kokorin.jaffree.StreamType;
import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.FFmpegProgress;
import com.github.kokorin.jaffree.ffmpeg.FFmpegResultFuture;
import com.github.kokorin.jaffree.ffmpeg.OutputListener;
import com.github.kokorin.jaffree.ffmpeg.ProgressListener;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import com.github.kokorin.jaffree.ffmpeg.UrlOutput;
import com.github.kokorin.jaffree.ffprobe.FFprobe;
import com.github.kokorin.jaffree.ffprobe.FFprobeResult;
import com.github.kokorin.jaffree.ffprobe.Format;
import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.discord.DiscordBot;
import com.gmail.berndivader.streamserver.mysql.GetNextScheduled;
import com.gmail.berndivader.streamserver.mysql.UpdateCurrent;
import com.gmail.berndivader.streamserver.term.ANSI;

public class BroadcastRunner extends TimerTask {
	
	boolean stop;
	
	public static FFmpegProgress currentProgress;
	public static Format currentFormat;
	public static String currentMessage;
	public static File currentPlaying;
	public static FFmpegResultFuture future;
	public static int index;
	
	public static BroadcastRunner instance;
	
	public BroadcastRunner() {
		ANSI.print("Starting BroadcastRunner...");
		
		instance=this;
		stop=false;
		
		Helper.refreshFilelist();
		Helper.shuffleFilelist(Helper.files);
		ANSI.println("DONE!");

		index=0;
		runStream();
		
		Helper.SCHEDULED_EXECUTOR.scheduleAtFixedRate(this, 0l, 1l, TimeUnit.SECONDS);
		
	}
	
	public void stop() throws InterruptedException {
		ANSI.print("Stopping BroadcastRunner...");
		
		stop=true;
    	if(future!=null&&(!future.isCancelled()||!future.isDone())) {
    		ANSI.print("[Stop broadcasting...");
    		future.graceStop();
    		try {
				future.get(30,TimeUnit.SECONDS);
				ANSI.print("DONE!]...");
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				ANSI.printWarn(e.getMessage());
			}
    	}
		
    	ANSI.println("DONE!");
	}
	
	@Override
	public void run() {
		
    	if(!stop) {
    		if(future.isCancelled()||future.isDone()) {
    			runStream();
    		}
		}
		
	}
	
	void runStream() {
		
		GetNextScheduled scheduled=new GetNextScheduled();
		String filename=null;
		try {
			filename=scheduled.future.get(20,TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			ANSI.printErr("Get next scheduled file failed.",e);
		}
		if(filename!=null) {
			int filepos=-1;
			filepos=Helper.getFilePosition(filename.toLowerCase());
			
			if(filepos>-1) {
				future=createStream(Helper.files[filepos]);
				currentPlaying=Helper.files[filepos];
			} else {
				filepos=Helper.getCustomFilePosition(filename.toLowerCase());
				if(filepos>-1) {
    				future=createStream(Helper.customs[filepos]);
    				currentPlaying=Helper.customs[filepos];
				}
			}
		} else {
			future=createStream(Helper.files[index]);
			currentPlaying=Helper.files[index];
			index++;
			if(index>Helper.files.length-1) {
				Helper.refreshFilelist();
				Helper.shuffleFilelist(Helper.files);				
				index=0;
			}
		}
		
	}
	
	private static FFmpegResultFuture createStream(File file) {
		String path=file.getAbsolutePath();
		FFprobeResult probeResult=FFprobe.atPath()
			.setShowFormat(true)
			.setInput(path)
			.setShowStreams(true)
			.execute();
		
		currentFormat=probeResult.getFormat();
		
		String title=file.getName();
		if(title.toLowerCase().endsWith(".mp4")) {
			title=title.substring(0,title.length()-4);
		}
		String info=currentFormat.getTag("artist")+":"+currentFormat.getTag("date")+":"+currentFormat.getTag("comment");
		new UpdateCurrent(title, info);
		
		DiscordBot.instance.updateStatus(title);
		
		ANSI.println("Now playing: "
			+currentFormat.getTag("title")
			+":"+currentFormat.getTag("artist")
			+":"+currentFormat.getTag("date")
			+":"+currentFormat.getTag("comment"));
				
		return FFmpeg.atPath()
				.addInput(UrlInput.fromUrl(path)
						.addArgument("-re")
						)
				.addOutput(UrlOutput.toUrl(Config.STREAM_URL+"/"+Config.STREAM_KEY)
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
						currentMessage=message;
					}
				})
				.setProgressListener(new ProgressListener() {
					@Override
					public void onProgress(FFmpegProgress progress) {
						currentProgress=progress;
					}
				}).setOverwriteOutput(true).executeAsync();
	}
	
	public static boolean isStreaming() {
		return future!=null&&!future.isCancelled()&&!future.isDone();
	}
	
	public static void broadcastFilename(File file) {
		if(future!=null) {
			future.graceStop();
			try {
				future.get(20,TimeUnit.SECONDS);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				ANSI.printErr("Failed to stop broadcast task.",e);
			}
		}
		future=createStream(file);
		ANSI.println(file.getName());
	}
	
	public static void broadcastPlaylistPosition(int idx) {
		index=idx;
		if(future!=null) {
			future.graceStop();
		}
	}
	
	public static void restartStream() {
		if(future!=null) {
			future.forceStop();
		}
		future=createStream(currentPlaying);
	}
	
	public static void playNext() {
		if(future!=null) {
			future.graceStop();
		}
	}
	
	public static void playPrevious() {
		if(future!=null) {
			int idx=index;
			idx-=2;
			if(idx<0) {
				idx=Helper.files.length-1;
			}
			index=idx;
			future.graceStop();
		}
	}
	
}
