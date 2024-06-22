package com.gmail.berndivader.streamserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.ffmpeg.FFProbePacket;
import com.gmail.berndivader.streamserver.ffmpeg.InfoPacket;
import com.gmail.berndivader.streamserver.term.ANSI;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Helper {
	
	public final static ExecutorService EXECUTOR;
	public final static ScheduledExecutorService SCHEDULED_EXECUTOR;
	public final static CloseableHttpClient HTTP_CLIENT;
	public final static Gson GSON;
	public static File[] files;
	public static File[] customs;
	
	static {
		files=new File[0];
		customs=new File[0];
		EXECUTOR=Executors.newFixedThreadPool(10);
		SCHEDULED_EXECUTOR=Executors.newScheduledThreadPool(5);
		HTTP_CLIENT=HttpClients.createMinimal();
		GSON=new GsonBuilder().setPrettyPrinting().create();
	}	
	
	public static int getFilePosition(String name) {
		if(!name.isEmpty()) {
			File[]files=Helper.files.clone();
			for(int i1=0;i1<files.length;i1++) {
				String file=files[i1].getName().toLowerCase();
				if(file.equals(name)) {
					return i1;
				}
			}
		}
		return -1;
	}
	
	public static int getCustomFilePosition(String name) {
		if(!name.isEmpty()) {
			File[]files=Helper.customs.clone();
			for(int i1=0;i1<files.length;i1++) {
				String file=files[i1].getName().toLowerCase();
				if(file.equals(name)) {
					return i1;
				}
			}
		}
		return -1;
	}
	
	public static ArrayList<String> getFilelistAsList(String regex) {
		if(regex.contains("*")) {
			regex=regex.replaceAll("*","(.*)");
		} else {
			regex="(.*)"+regex+("(.*)");
		}
		ArrayList<String>list=new ArrayList<>();
		File[]files=Helper.files.clone();
		for(int i1=0;i1<files.length;i1++) {
			String name=files[i1].getName().toLowerCase();
			try {
				if(name.matches(regex)) {
					list.add(name);
				}
			} catch (Exception e) {
				ANSI.printErr("getFilelistAsList method failed.",e);
			}
		}
		files=Helper.customs.clone();
		for(int i1=0;i1<files.length;i1++) {
			String name=files[i1].getName().toLowerCase();
			try {
				if(name.matches(regex)) {
					list.add(name);
				}
			} catch (Exception e) {
				ANSI.println(e.getMessage());
			}
		}
		return list;
	}
	
	public static String getFilelistAsString(String regex) {
		int count=0;
		StringBuilder playlist=new StringBuilder();
		if(regex.contains("*")) {
			regex=regex.replaceAll("*","(.*)");
		} else {
			regex="(.*)"+regex+("(.*)");
		}
		File[]files=Helper.files.clone();
		for(int i1=0;i1<files.length;i1++) {
			String name=files[i1].getName().toLowerCase();
			if(name!=null&&!name.isEmpty()&&name.matches(regex)) {
				playlist.append(name+"\n");
				count++;
			}
		}
		files=Helper.customs.clone();
		for(int i1=0;i1<files.length;i1++) {
			String name=files[i1].getName().toLowerCase();
			if(name!=null&&!name.isEmpty()&&name.matches(regex)) {
				playlist.append(name+"\n");
				count++;
			}
		}
		playlist.append("\nThere are "+count+" matches for "+regex);
		return playlist.toString();
	}
	
	public static void shuffleFilelist(File[] files) {
		Random random=ThreadLocalRandom.current();
		for (int i1=files.length-1;i1>0;i1--) {
			int index=random.nextInt(i1+1);
			File a=files[index];
			files[index]=files[i1];
			files[i1]=a;
		}
	}
	
	public static void refreshFilelist() {
    	File file=new File(Config.PLAYLIST_PATH);
    	File custom=new File(Config.PLAYLIST_PATH_CUSTOM);
    	
		FileFilter filter=new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getAbsolutePath().toLowerCase().endsWith(".mp4");
			}
		};
    	
		if(file.exists()) {
	    	if(file.isDirectory()) {
	    		Helper.files=file.listFiles(filter);
	    	} else if(file.isFile()) {
	    		Helper.files=new File[] {file};
	    	} else {
	    		Helper.files=new File[0];
	    	}
		} else {
			Helper.files=new File[0];
		}
    	
    	if(custom.exists()) {
        	if(custom.isDirectory()) {
        		Helper.customs=custom.listFiles(filter);
        	} else if(custom.isFile()) {
        		Helper.customs=new File[] {custom};
        	} else {
        		Helper.customs=new File[0];
        	}
    	} else {
    		Helper.customs=new File[0];
    	}
    	
	}
	
	public static String getStringFromStream(InputStream stream,int length) {
		byte[]bytes=new byte[length];
		try {
			int size=stream.read(bytes,0,length);
			return new String(bytes).substring(0,size);
		} catch (IOException e) {
			ANSI.printErr("getStringFromStream method failed.",e);
			return "";
		}
	}
	
	public static String createDownloadLink(File file) {

		ANSI.println(file.getAbsolutePath());
		
		ProcessBuilder builder=new ProcessBuilder();
		builder.directory(new File("./"));
		builder.command("ffprobe","-v","quiet","-print_format","json","-show_format",file.getAbsolutePath());
		
		FFProbePacket packet=new FFProbePacket();
		try {
			Process process=builder.start();
			BufferedReader reader=process.inputReader();
			while(process.isAlive()) {
				if(reader.ready()) {
					packet=GSON.fromJson(process.inputReader().readLine(),FFProbePacket.class);
				}
			}
		} catch (Exception e) {
			ANSI.printErr("createDownloadlink method failed.", e);
		}
		
		ANSI.println(packet.toString());
		return null;
	}
	
	public static InfoPacket getDLPinfoPacket(List<String>commands,File directory,String url) {
		//yt-dlp --quiet --no-warnings --dump-single-json
		ProcessBuilder infoBuilder=new ProcessBuilder();
		infoBuilder.directory(directory);
		infoBuilder.command(commands);
		infoBuilder.command().addAll(Arrays.asList("--quiet","--no-warnings","--dump-json",url));
		InfoPacket info=null;
		try {
			Process infoProc=infoBuilder.start();
			BufferedReader reader=infoProc.inputReader();
			while(infoProc.isAlive()) {
				if(reader.ready()) {
					info=GSON.fromJson(infoProc.inputReader().readLine(),InfoPacket.class);
				}
			}
		} catch (IOException e) {
			ANSI.printErr("getDLPinfoPacket method failed.",e);
		}
		return info;
	}
	
	public static Entry<ProcessBuilder, Optional<InfoPacket>> prepareDownloadBuilder(File directory,String args) {
		ProcessBuilder builder=new ProcessBuilder();
		builder.directory(directory);
		boolean downloadable=false;
		
		if(args.contains("--downloadable")) {
			args=args.replace("--downloadable","");
			downloadable=true;
		}
		
		if(args.contains("--no-default")) {
			args=args.replace("--no-default","");
			builder.command("yt-dlp"
					,"--progress-delta","2"
					,"--restrict-filenames"
					,"--embed-metadata"
					,"--embed-thumbnail"
					,"--output","%(title).200s.%(ext)s"
			);
		} else {
			builder.command("yt-dlp"
					,"--progress-delta","2"
					,"--embed-metadata"
					,"--embed-thumbnail"
					,"--ignore-errors"
					,"--extract-audio"
					,"--format","bestaudio"
					,"--audio-format","mp3"
					,"--audio-quality","160K"
					,"--output","%(title).200s.%(ext)s"
					,"--restrict-filenames"
					,"--no-playlist"
			);
		}
		
		String[]temp=args.split("--");
		String url=null;
		
		for(int i=0;i<temp.length;i++) {
			if(temp[i].isEmpty()) continue;
						
			String[]parse=temp[i].trim().split(" ",2);
			if(parse.length>0) {
				switch(parse[0]) {
					case("url"):
						if(parse.length==2) {
							url=parse[1];
							parse[1]="";
						}
						break;
					case("dir"):
						if(parse.length==2) {
							File dir=new File(Config.DL_MUSIC_PATH.concat("/").concat(parse[1]));
							parse[1]="";
							if(!dir.exists()) dir.mkdir();
							if(dir.isDirectory()) {
								builder.directory(dir);
							} else {
								ANSI.printWarn("Warning! Download directory is a file, using default.");
							}
						}
						break;
					case("cookies"):
						if(Config.YOUTUBE_USE_COOKIES&&Config.YOUTUBE_COOKIES.exists()) {
							builder.command().add("--cookies");
							builder.command().add(Config.YOUTUBE_COOKIES.getAbsolutePath());
						}
						break;
					default:
						if(!parse[0].isEmpty()) builder.command().add(parse[0]);
						break;
				}
			}			
		}
		
		InfoPacket infoPacket=null;
		if(url!=null&&!url.isEmpty()) {
			infoPacket=Helper.getDLPinfoPacket(new ArrayList<String>(builder.command()),builder.directory(),url);
			builder.command().add(url);
			infoPacket.downloadable=downloadable;
		}
		
		return Map.entry(builder,Optional.ofNullable(infoPacket));
	}
	
	public static void close() {
		ANSI.print("[WHITE]Shutdown task executor...");
		EXECUTOR.shutdown();
		ANSI.println("[GREEN]DONE!");
		ANSI.print("[WHITE]Shutdown scheduled task executor...");
		SCHEDULED_EXECUTOR.shutdown();
		ANSI.println("[GREEN]DONE![/GREEN]");
	}

}
