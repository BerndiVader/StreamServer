package com.gmail.berndivader.streamserver;

import java.io.Console;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.github.kokorin.jaffree.ffmpeg.FFmpegProgress;
import com.github.kokorin.jaffree.ffprobe.Format;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;
import com.gmail.berndivader.streamserver.mysql.AddScheduled;
import com.gmail.berndivader.streamserver.mysql.UpdatePlaylist;

public class ConsoleRunner {
	
    static Scanner keyboard;
    static Thread thread;
    static Console console;
    
    public static boolean forceExit;
    
    static {
    	keyboard=new Scanner(System.console().reader());
    	console=System.console();
    }
	
	public ConsoleRunner() throws InterruptedException, ExecutionException, TimeoutException {
		
		boolean exit=false;
		
		while (!exit) {
	        printReady();
            String input = keyboard.nextLine();
            
            if(input != null) {
            	
            	String[]parse=input.toLowerCase().split(" ",2);
            	if(parse.length==1) {
            		parse=new String[] {parse[0],""};
            	}
            	
            	String command=parse[0];
            	String[]args=parse[1].split(" ",2);
            	
            	switch(command) {
	            	case ".x":
	            	case ".exit":
	            		forceExit=args[0].equals("force")?true:false;
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
	            	case ".next":
	            	case ".stop":
	            		BroadcastRunner.playNext();
	            		break;
	            	case ".prev":
	            		BroadcastRunner.playPrevious();
	            		break;
	            	case ".playlist":
	            		playlistInfo(new String[] {parse[1]});
	            		break;
	            	case ".play":
	            		streamFile(new String[] {parse[1]});
	            		break;
	            	case ".help":
	            	case ".h":
	            		println(Config.HELP_TEXT);
	            		break;
	            	case ".mysql":
	            		executeSqlCommand(args);
	            		break;
	            	case ".config":
	            		executeConfigCommand(new String[] {parse[1]});
	            	default:
	            		break;
            	}
            }
        }
        keyboard.close();
	}
	
	void executeConfigCommand(String[] args) {
		switch(args[0]) {
		case "save":
			if(!Config.saveConfig()) {
				println("Configuration saved");
			} else {
				println("Failed to save configuration");
			}
			break;
		case "load":
			if(Config.loadConfig()) {
				println("Configuration reloaded");
			} else {
				println("Failed to load configuration");
			}
			break;
		}
	}
	
	void executeSqlCommand(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
		switch(args[0]) {
			case "playlist":
				if(args.length>0) {
					switch(args[1]) {
						case "refresh":
							new UpdatePlaylist(true);
							break;
					}
				}
				break;
			case "schedule":
				if(args.length>0) {
					String filename=args[1];
					if(!filename.endsWith(".mp4")) {
						filename=filename+".mp4";
					}
					int index=Utils.getFilePosition(filename);
					if(index>-1) {
						filename=Helper.files[index].getName();
						new AddScheduled(filename);
					}
				}
				break;
		}
		
	}
	
	void streamFile(String[] args) {
		String file=args[0];
		int index=Utils.getFilePosition(file);
		if(index>-1) {
			BroadcastRunner.broadcastPlaylistPosition(index);
		}
	}
	
	void playlistInfo(String[] args) {
		String regex=args[0];
		println(Utils.getPlaylistAsString(regex));
	}
	
	void broadcastInfo(String[] args) {
		println("===Broadcast information===");
		for(int i1=0;i1<args.length;i1++) {
			switch(args[i1]) {
				case "status":
					println("Is broadcasting: "+BroadcastRunner.isStreaming());
					break;
				case "file":
					println("Current File: "+Helper.files[BroadcastRunner.index-1].getName());
					break;
				case "next":
					println("Next File: "+Helper.files[BroadcastRunner.index].getName());
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
				println("Last ffmpeg output: "+message);
			} else {
				println("Currently no ffmpeg message output present.");
			}
		} else {
			println("Currently no stream is running.");
		}
	}
	
	void progressInfo(String[] args) {
		if(BroadcastRunner.isStreaming()) {
			FFmpegProgress progress=BroadcastRunner.currentProgress;
			if(progress!=null) {
				println("===Progress information===");
				long duration=progress.getTime(TimeUnit.SECONDS);
				for(int i1=0;i1<args.length;i1++) {
					String option=args[i1];
					switch(option) {
						case "time":
							println("Playtime:"+(long)(duration/60)+":"+(long)(duration%60));
							break;
						case "frames":
						case "frame":
							println("Frames: "+progress.getFrame());
							break;
						case "bitrate":
						case "bits":
							println("Bitrate: "+progress.getBitrate());
							break;
						case "quality":
						case "q":
							println("Quality: "+progress.getQ());
							break;
						case "fps":
							println("FPS: "+progress.getFps());
							break;
						case "drops":
							println("Drops: "+progress.getDrop());
							break;
						case "size":
							println("Size: "+progress.getSize());
							break;
						case "speed":
							println("Speed: "+progress.getSpeed());
							break;
						default:
							println("Playtime:"+(long)(duration/60)+":"+(long)(duration%60));
							println("Frames: "+progress.getFrame());
							println("Bitrate: "+progress.getBitrate());
							println("Quality: "+progress.getQ());
							println("FPS: "+progress.getFps());
							println("Drops: "+progress.getDrop());
							println("Size: "+progress.getSize());
							println("Speed: "+progress.getSpeed());
							break;
					}
				}
			} else {
				println("No progress available atm.");
			}
		} else {
			println("Currently no stream is running.");
		}
	}
	
	void streamInfo(String[] args) {
		if(BroadcastRunner.isStreaming()) {
			Format format=BroadcastRunner.currentFormat;
			if(format!=null) {
				float duration=format.getDuration();
				println("===Current playing===");
				for(int i1=0;i1<args.length;i1++) {
					switch(args[i1]) {
					case "title":
						println("Title: "+format.getTag("title"));
						break;
					case "artist":
						println("Artist:"+format.getTag("artist"));
						break;
					case "date":
						println("Date:"+format.getTag("date"));
						break;
					case "comment":
						println("Comment:"+format.getTag("comment"));
						break;
					case "playtime":
					case "time":
						println("Playtime:"+(long)(duration/60)+":"+(long)(duration%60));
						break;
					case "file":
						println("File:"+format.getFilename());
						break;
					case "bitrate":
					case "bits":
						println("Bitrate:"+format.getBitRate());
						break;
					case "format":
						println("Format:"+format.getFormatName());
						break;
					default:
						println("Title: "+format.getTag("title"));
						println("Artist:"+format.getTag("artist"));
						println("Date:"+format.getTag("date"));
						println("Comment:"+format.getTag("comment"));
						println("Playtime:"+(long)(duration/60)+":"+(long)(duration%60));
						println("File:"+format.getFilename());
						println("Bitrate:"+format.getBitRate());
						println("Format:"+format.getFormatName());
						break;
					}
				}
			} else {
				println("No information about stream available.");
			}
		} else {
			println("Currently no stream is running.");
		}
	}
	
	public static void println(String string) {
		console.printf("%s\n>",string);
	}
	
	public static void print(String string) {
		console.printf("%s",string);
	}
	
	public static void printReady() {
		console.printf("\n%s",">");
	}

}
