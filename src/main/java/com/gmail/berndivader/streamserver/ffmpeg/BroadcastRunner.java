package com.gmail.berndivader.streamserver.ffmpeg;

import java.io.File;
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
import com.gmail.berndivader.streamserver.Utils;
import com.gmail.berndivader.streamserver.config.Config;

public class BroadcastRunner {
	static Thread THREAD;
	
	boolean quit;
	
	public static FFmpegProgress currentProgress;
	public static Format currentFormat;
	public static String currentMessage;
	public static FFmpegResultFuture future;
	public static int index;
	public static File[] files;
	
	public BroadcastRunner() {
		System.out.print("Staring BroadcastRunner...");
		THREAD=create();
		THREAD.start();
		System.out.println("DONE!");
	}
	
	public void stop() throws InterruptedException {
		System.out.print("Stopping BroadcastRunner...");
		quit=true;
		THREAD.join();
		System.out.println("DONE!");
	}
	
	Thread create() {
		quit=false;
		return new Thread(() -> {
			files=Utils.shufflePlaylist(Utils.refreshPlaylist());
			future=startNewStream(files[0].getAbsolutePath());
			index=1;
			
	    	while(!quit) {
	    		if(future.isCancelled()||future.isDone()){
	    			future=startNewStream(files[index].getAbsolutePath());
	    			index++;
	    			if(index>files.length-1) {
	    				files=Utils.shufflePlaylist(Utils.refreshPlaylist());
	    				index=0;
	    			}
	    		}
	    		try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
    		}
	    	if(future!=null&&!future.isCancelled()||!future.isDone()) {
	    		System.out.print("[Stop broadcasting...");
	    		future.graceStop();
	    		try {
					future.get(30,TimeUnit.SECONDS);
		    		System.out.print("DONE!]...");
				} catch (InterruptedException | ExecutionException | TimeoutException e) {
					e.printStackTrace();
					System.out.print("FAILED!]...");
				}
	    	}
		});
	}
	
	static FFmpegResultFuture startNewStream(String path) {
		FFprobeResult probeResult=FFprobe.atPath()
			.setShowFormat(true)
			.setInput(path)
			.setShowStreams(true)
			.execute();
		
		currentFormat=probeResult.getFormat();
		System.out.println("Now playing: "
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
	
	public static void broadcastPlaylistPosition(int idx) {
		index=idx;
		if(future!=null) {
			future.graceStop();
		}
	}
	
	public static void restartStream() {
		if(future!=null) {
			if(index!=0) {
				index--;
			} else {
				index=files.length-1;
			}
			future.graceStop();
		}
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
				idx=files.length-1;
			}
			index=idx;
			future.graceStop();
		}
	}
	
	***REMOVED***		
	***REMOVED***
	***REMOVED***
	
}
