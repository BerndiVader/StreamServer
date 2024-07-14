package com.gmail.berndivader.streamserver.ffmpeg;

import java.io.File;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
import com.github.kokorin.jaffree.ffprobe.Format;
import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.discord.DiscordBot;
import com.gmail.berndivader.streamserver.mysql.GetNextScheduled;
import com.gmail.berndivader.streamserver.mysql.UpdateCurrent;
import com.gmail.berndivader.streamserver.term.ANSI;

public final class BroadcastRunner extends TimerTask {
	
	boolean stop;
	
	public static FFmpegProgress progress;
	public static Format format;
	public static String message;
	public static File playling;
	private static FFmpegResultFuture ffmpeg;
	public static int index;
	
	public static BroadcastRunner instance;
	
	public BroadcastRunner() {
		ANSI.print("[YELLOW]Starting BroadcastRunner...");

		stop=false;
		
		Helper.refreshFilelist();
		Helper.shuffleFilelist(Helper.files);

		index=0;
		startStream();
		
		Helper.SCHEDULED_EXECUTOR.scheduleAtFixedRate(this, 0l, 1l, TimeUnit.SECONDS);
		ANSI.println("[GREEN]DONE![RESET]");
	}
	
	public void stop() throws InterruptedException {
		ANSI.print("[YELLOW]Stopping BroadcastRunner...");
		
		stop=true;
    	if(ffmpeg!=null&&(!ffmpeg.isCancelled()||!ffmpeg.isDone())) {
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
		
    	if(!stop) {
    		if(ffmpeg==null||ffmpeg.isCancelled()||ffmpeg.isDone()) startStream();
		}
		
	}
	
	private void startStream() {
		
		GetNextScheduled scheduled=new GetNextScheduled();
		try {
			String name=scheduled.future.get(20,TimeUnit.SECONDS);
			if(name!=null) {
				File file=Helper.getFileByName(name.toLowerCase());
				if(file!=null) {
					createStream(file);
					return;
				}
			}
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			ANSI.printErr("Get next scheduled file failed.",e);
		}
		
		createStream(Helper.files[index]);
		index=(index+1)%Helper.files.length;
	}
	
	private static void createStream(File file) {
		String path=file.getAbsolutePath();
		FFprobeResult probeResult=FFprobe.atPath()
			.setShowFormat(true)
			.setInput(path)
			.setShowStreams(true)
			.execute();
		
		format=probeResult.getFormat();
		String title=file.getName();
		if(title.toLowerCase().endsWith(".mp4")) {
			title=title.substring(0,title.length()-4);
		}
		
		String info=format.getTag("artist")+":"+format.getTag("date")+":"+format.getTag("comment");
		new UpdateCurrent(title, info);
		
		if(Config.DISCORD_BOT_START&&DiscordBot.instance!=null) DiscordBot.instance.updateStatus(title);
		
		ANSI.println("[BLUE]Now playing: "
			+format.getTag("title")
			+":"+format.getTag("artist")
			+":"+format.getTag("date")
			+":"+format.getTag("comment")+"[RESET]");
		ANSI.prompt();
				
		ffmpeg=FFmpeg.atPath()
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
						BroadcastRunner.message=message;
					}
				})
				.setProgressListener(new ProgressListener() {
					@Override
					public void onProgress(FFmpegProgress progress) {
						BroadcastRunner.progress=progress;
					}
				}).setOverwriteOutput(true).executeAsync();
		if(ffmpeg!=null&&!ffmpeg.isDone()&&!ffmpeg.isCancelled()) playling=file;
	}
	
	public static boolean isStreaming() {
		return ffmpeg!=null&&!ffmpeg.isCancelled()&&!ffmpeg.isDone();
	}
	
	public static void playFile(File file) {
		if(isStreaming()) {
			stopStream();
			createStream(file);
		}
	}
	
	public static void playPosition(int idx) {
		index=idx;
		if(isStreaming()) stopStream();
	}
	
	public static void restart() {
		if(isStreaming()) stopStream();
		createStream(playling);
	}
	
	public static void next() {
		if(isStreaming()) stopStream();		
	}
	
	public static void previous() {
		if(isStreaming()) {
			index=index-2<0?Helper.files.length-1:index-2;
			stopStream();
		}
	}
	
	private static FFmpegResult stopStream() {
		ffmpeg.graceStop();
		try {
			return ffmpeg.get(20,TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			ANSI.printErr("Failed to stop broadcast task.",e);
		}
		return null;
	}
	
}
