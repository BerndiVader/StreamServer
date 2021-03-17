package com.gmail.berndivader.streamserver;

import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.github.kokorin.jaffree.ffmpeg.FFmpegProgress;
import com.github.kokorin.jaffree.ffprobe.Format;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;

public class ConsoleRunner {
	
    static Scanner keyboard;
    static Thread thread;
    
    static {
    	keyboard=new Scanner(System.in);
    }
	
	public ConsoleRunner() throws InterruptedException, ExecutionException, TimeoutException {
		
		boolean exit=false;
		
		while (!exit) {
	        System.out.print("> "); 
            String input = keyboard.nextLine();
            
            if(input != null) {
            	
            	String[]parse=input.toLowerCase().split(" ",2);
            	if(parse.length==1) {
            		parse=new String[] {parse[0],""};
            	}
            	
            	String command=parse[0];
            	String[]args=parse[1].split(" ");
            	
            	switch(command) {
	            	case ".x":
	            	case ".exit":
	            		exit=true;
	            		break;
	            	case ".f":
	            	case ".format":
	            		streamInfo(args);
	            		break;
	            	case ".p":
	            	case ".progress":
	            		progressInfo(args);
	            		break;
	            	case ".m":
	            	case ".message":
	            		messageInfo(args);
	            		break;
	            	case ".b":
	            	case ".broadcast":
	            		broadcastInfo(args);
	            		break;
	            	case ".restart":
	            		BroadcastRunner.restartStream();
	            		break;
	            	default:
	            		break;
            	}
            }
        }
        keyboard.close();
	}
	
	void broadcastInfo(String[] args) {
		System.out.println("===Broadcast information===");
		for(int i1=0;i1<args.length;i1++) {
			switch(args[i1]) {
				case "status":
					System.out.println("Is broadcasting: "+BroadcastRunner.isStreaming());
					break;
				case "file":
					System.out.println("Current File: "+BroadcastRunner.files[BroadcastRunner.index-1].getName());
					break;
				case "next":
					System.out.println("Next File: "+BroadcastRunner.files[BroadcastRunner.index].getName());
					break;
				default:
					break;
			}
		}
	}
	
	void messageInfo(String[] args) {
		if(BroadcastRunner.isStreaming()) {
			String message=BroadcastRunner.currentMessage;
			if(message!=null) {
				System.out.println("Last ffmpeg output: "+message);
			} else {
				System.out.println("Currently no ffmpeg message output present.");
			}
		} else {
			System.out.println("Currently no stream is running.");
		}
	}
	
	void progressInfo(String[] args) {
		if(BroadcastRunner.isStreaming()) {
			FFmpegProgress progress=BroadcastRunner.currentProgress;
			if(progress!=null) {
				System.out.println("===Progress information===");
				long duration=progress.getTime(TimeUnit.SECONDS);
				for(int i1=0;i1<args.length;i1++) {
					String option=args[i1];
					switch(option) {
						case "time":
							System.out.println("Playtime:"+(long)(duration/60)+":"+(long)(duration%60));
							break;
						case "frames":
						case "frame":
							System.out.println("Frames: "+progress.getFrame());
							break;
						case "bitrate":
						case "bits":
							System.out.println("Bitrate: "+progress.getBitrate());
							break;
						case "quality":
						case "q":
							System.out.println("Quality: "+progress.getQ());
							break;
						case "fps":
							System.out.println("FPS: "+progress.getFps());
							break;
						case "drops":
							System.out.println("Drops: "+progress.getDrop());
							break;
						case "size":
							System.out.println("Size: "+progress.getSize());
							break;
						case "speed":
							System.out.println("Speed: "+progress.getSpeed());
							break;
						default:
							System.out.println("Playtime:"+(long)(duration/60)+":"+(long)(duration%60));
							System.out.println("Frames: "+progress.getFrame());
							System.out.println("Bitrate: "+progress.getBitrate());
							System.out.println("Quality: "+progress.getQ());
							System.out.println("FPS: "+progress.getFps());
							System.out.println("Drops: "+progress.getDrop());
							System.out.println("Size: "+progress.getSize());
							System.out.println("Speed: "+progress.getSpeed());
							break;
					}
				}
			} else {
				System.out.println("No progress available atm.");
			}
		} else {
			System.out.println("Currently no stream is running.");
		}
	}
	
	void streamInfo(String[] args) {
		if(BroadcastRunner.isStreaming()) {
			Format format=BroadcastRunner.currentFormat;
			if(format!=null) {
				float duration=format.getDuration();
				System.out.println("===Current playing===");
				for(int i1=0;i1<args.length;i1++) {
					switch(args[i1]) {
					case "title":
						System.out.println("Title: "+format.getTag("title"));
						break;
					case "artist":
						System.out.println("Artist:"+format.getTag("artist"));
						break;
					case "date":
						System.out.println("Date:"+format.getTag("date"));
						break;
					case "comment":
						System.out.println("Comment:"+format.getTag("comment"));
						break;
					case "playtime":
					case "time":
						System.out.println("Playtime:"+(long)(duration/60)+":"+(long)(duration%60));
						break;
					case "file":
						System.out.println("File:"+format.getFilename());
						break;
					case "bitrate":
					case "bits":
						System.out.println("Bitrate:"+format.getBitRate());
						break;
					case "format":
						System.out.println("Format:"+format.getFormatName());
						break;
					default:
						System.out.println("Title: "+format.getTag("title"));
						System.out.println("Artist:"+format.getTag("artist"));
						System.out.println("Date:"+format.getTag("date"));
						System.out.println("Comment:"+format.getTag("comment"));
						System.out.println("Playtime:"+(long)(duration/60)+":"+(long)(duration%60));
						System.out.println("File:"+format.getFilename());
						System.out.println("Bitrate:"+format.getBitRate());
						System.out.println("Format:"+format.getFormatName());
						break;
					}
				}
			} else {
				System.out.println("No information about stream available.");
			}
		} else {
			System.out.println("Currently no stream is running.");
		}
	}

}
