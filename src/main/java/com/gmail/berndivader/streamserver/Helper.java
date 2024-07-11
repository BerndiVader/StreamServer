package com.gmail.berndivader.streamserver;

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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.ffmpeg.FFProbePacket;
import com.gmail.berndivader.streamserver.ffmpeg.InfoPacket;
import com.gmail.berndivader.streamserver.term.ANSI;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class Helper {
	
	public final static ExecutorService EXECUTOR;
	public final static ScheduledExecutorService SCHEDULED_EXECUTOR;
	public final static CloseableHttpClient HTTP_CLIENT;
	public final static Gson LGSON;
	public final static Gson GSON;
	public static File[] files;
	public static File[] customs;
	
	private Helper() {}
	
	static {
		files=new File[0];
		customs=new File[0];
		EXECUTOR=Executors.newCachedThreadPool();
		SCHEDULED_EXECUTOR=Executors.newSingleThreadScheduledExecutor();
		HTTP_CLIENT=HttpClients.createMinimal();
		GSON=new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		LGSON=new GsonBuilder().setPrettyPrinting().setFieldNamingStrategy(s->s.getName().toLowerCase()).disableHtmlEscaping().create();
	}
	
	public static int getFilePosition(String name) {
		if(!name.isEmpty()) {
			File[]files=Helper.files.clone();
		    for(int i=0;i<Helper.files.length;i++) {
		        if(files[i].getName().equalsIgnoreCase(name)) return i;
		    }
		}
		return -1;
	}
	
	public static int getCustomFilePosition(String name) {
		if(!name.isEmpty()) {
			File[]files=Helper.customs.clone();
			for(int i=0;i<files.length;i++) {
		        if(files[i].getName().equalsIgnoreCase(name)) return i;

			}
		}
		return -1;
	}
	
	public static List<String> getFilelistAsList(String r) {
	    String regex=r.contains("*")?r.replaceAll("\\*","(.*)"):"(.*)"+r+"(.*)";
	    List<String>list=new ArrayList<String>();
	    Stream.of(Helper.files,Helper.customs)
	        .flatMap(Arrays::stream)
	        .map(File::getName)
	        .filter(name->{
	        	try {
		            return name.toLowerCase().matches(regex);	
	        	} catch (Exception e) {
					if(Config.DEBUG) ANSI.printErr("getFilelistAsString method failed.",e);
	        	}
	        	return false;
	        })
	        .forEach(list::add);
	    return list;
	}

	public static String getFilelistAsString(String r) {
	    AtomicInteger count=new AtomicInteger(0);
	    StringBuilder playlist = new StringBuilder();
	    String regex=r.contains("*")?r.replaceAll("\\*","(.*)"):"(.*)"+r+"(.*)";
	
	    Stream.of(Helper.files, Helper.customs)
	        .flatMap(Arrays::stream)
	        .map(File::getName)
	        .filter(name->{
				try {
					return !name.isEmpty()&&name.matches(regex);
				} catch (Exception e) {
					if(Config.DEBUG) ANSI.printErr("getFilelistAsString method failed.",e);
				}
				return false;
	        })
	        .forEach(name->{
	            playlist.append(name).append("\n");
	            count.incrementAndGet();
	        });
	
	    playlist.append("\nThere are ").append(count).append(" matches for ").append(regex);
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
	
	private static File[] getFiles(File file,FileFilter filter) {
		if(file.exists()) {
	    	if(file.isDirectory()) {
	    		return file.listFiles(filter);
	    	} else if(file.isFile()) {
	    		return new File[] {file};
	    	}
		}
		return new File[0];
	}
	
	public static void refreshFilelist() {
    	File file=new File(Config.PLAYLIST_PATH);
    	File custom=new File(Config.PLAYLIST_PATH_CUSTOM);
    	
    	FileFilter filter=pathName->pathName.getAbsolutePath().toLowerCase().endsWith(".mp4");
    	Helper.files=getFiles(file,filter);
    	Helper.customs=getFiles(custom,filter);
	}
	
	public static String getStringFromStream(InputStream stream,int length) {
		byte[]bytes=new byte[length];
		try {
			int size=stream.read(bytes,0,length);
			return new String(bytes,0,size-1);
		} catch (IOException e) {
			ANSI.printErr("getStringFromStream method failed.",e);
			return "";
		}
	}
	
	private static String startProcessAndWait(ProcessBuilder builder,long timeout) throws Exception {
		long start=System.currentTimeMillis();
		timeout*=1000l;
		Process process=builder.start();
		InputStream input=process.getInputStream();
		InputStream error=process.getErrorStream();
		StringBuilder out=new StringBuilder();
		
		while(process.isAlive()) {
			int avail=input.available();
			if(avail>0) {
				out.append(new String(input.readAllBytes()));
				start=System.currentTimeMillis();
			}
			
			if(System.currentTimeMillis()-start>timeout) {
				process.destroy();
				throw new Exception("Process timed out.");
			}
		}
		
		if(error.available()>0) ANSI.printErr(new String(error.readAllBytes()),null);
		return out.toString();
	}
	
	public static FFProbePacket getProbePacket(File file) {
		FFProbePacket packet=null;
		if(file.exists()) {
			ProcessBuilder builder=new ProcessBuilder();
			builder.directory(new File("./"));
			builder.command("ffprobe","-v","quiet","-print_format","json","-show_format",file.getAbsolutePath());
			try {
				String out=startProcessAndWait(builder,10l);
				if(!out.isEmpty()) {
					JsonObject o=JsonParser.parseString(out.toString()).getAsJsonObject();
					if(o.has("format")) packet=LGSON.fromJson(o.get("format"),FFProbePacket.class);
				}
			} catch (Exception e) {
				ANSI.printErr("getProbePacket method failed.", e);
			}
		}
		return packet==null?new FFProbePacket():packet;
	}
	
	public static InfoPacket getInfoPacket(String url) {
		//yt-dlp --quiet --no-warnings --dump-single-json
		ProcessBuilder builder=new ProcessBuilder();
		builder.directory(new File("./"));
		builder.command("yt-dlp"
			,"--quiet"
			,"--no-warnings"
			,"--dump-json"
		);
		if(Config.YOUTUBE_USE_COOKIES&&Config.YOUTUBE_COOKIES.exists()) {
			builder.command().add("--cookies");
			builder.command().add(Config.YOUTUBE_COOKIES.getAbsolutePath());
		}
		builder.command().add(url);
		
		InfoPacket info=null;
		try {
			String out=startProcessAndWait(builder,10l);
			if(!out.isEmpty()) info=LGSON.fromJson(out.toString(),InfoPacket.class);
		} catch (Exception e) {
			ANSI.printErr("getinfoPacket method failed.",e);
		}
		return info==null?new InfoPacket():info;
	}
	
	public static Entry<ProcessBuilder, Optional<InfoPacket>> prepareDownloadBuilder(File defaultDirectory,String args) {
		ProcessBuilder builder=new ProcessBuilder();
		builder.directory(defaultDirectory);
		boolean downloadable=args.contains("--link ");
		if(downloadable) args=args.replace("--link","");
		
		if(args.contains("--music ")) {
			args=args.replace("--music","");
			builder.command("yt-dlp"
					,"--progress-delta","2"
					,"--embed-metadata"
					,"--embed-thumbnail"
					,"--ignore-errors"
					,"--extract-audio"
					,"--format","bestaudio"
					,"--audio-format","mp3"
					,"--audio-quality","160K"
					,"--output","%(title).64s.%(ext)s"
					,"--restrict-filenames"
					,"--no-playlist"
			);
			File dir=getOrCreateMediaDir(Config.DL_MUSIC_PATH);
			if(dir!=null) builder.directory(dir);
			
		} else if(args.contains("--temp ")) {
			args=args.replace("--temp","");
			builder.command("yt-dlp"
					,"--progress-delta","2"
					,"--restrict-filenames"
					,"--embed-metadata"
					,"--embed-thumbnail"
					,"--output","%(title).64s.%(ext)s"
			);
			File dir=getOrCreateMediaDir(Config.DL_TEMP_PATH);
			if(dir!=null) builder.directory(dir);
			downloadable=true;
		} else {
			builder.command("yt-dlp"
					,"--progress-delta","2"
					,"--restrict-filenames"
					,"--embed-metadata"
					,"--embed-thumbnail"
					,"--output","%(title).64s.%(ext)s"
			);
			File dir=getOrCreateMediaDir(Config.DL_MEDIA_PATH);
			if(dir!=null) builder.directory(dir);
		}
		
		if(Config.YOUTUBE_USE_COOKIES&&Config.YOUTUBE_COOKIES.exists()) {
			builder.command().add("--cookies");
			builder.command().add(Config.YOUTUBE_COOKIES.getAbsolutePath());
		}
		
		String[]temp=args.split("--");
		String url="";
		
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
					case("cookies"):
						if(!Config.YOUTUBE_USE_COOKIES&&Config.YOUTUBE_COOKIES.exists()) {
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
		
		InfoPacket infoPacket=Helper.getInfoPacket(url);
		if(!url.isEmpty()) builder.command().add(url);
		infoPacket.downloadable=downloadable;
		
		return Map.entry(builder,Optional.ofNullable(infoPacket));
	}
	
	public static File getOrCreateMediaDir(String name) {
		File dir=new File(name);
		if(!dir.exists()) dir.mkdir();
		if(dir.isDirectory()) return dir;
		if(Config.DEBUG) ANSI.printWarn("Failed to create directory: "+name);
		return null;
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
