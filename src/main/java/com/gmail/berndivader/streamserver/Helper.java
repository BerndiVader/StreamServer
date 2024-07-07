package com.gmail.berndivader.streamserver;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
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
import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class Helper {
	
	public final static ExecutorService EXECUTOR;
	public final static ScheduledExecutorService SCHEDULED_EXECUTOR;
	public final static CloseableHttpClient HTTP_CLIENT;
	public final static Gson GSON;
	public static File[] files;
	public static File[] customs;
	
	private Helper() {}
	
	static {
		files=new File[0];
		customs=new File[0];
		EXECUTOR=Executors.newFixedThreadPool(10);
		SCHEDULED_EXECUTOR=Executors.newScheduledThreadPool(5);
		HTTP_CLIENT=HttpClients.createMinimal();
		GSON=new GsonBuilder().setPrettyPrinting().setFieldNamingStrategy(new FieldNamingStrategy() {
			
			@Override
			public String translateName(Field f) {
				return f.getName().toLowerCase();
			}
			
		}).disableHtmlEscaping().create();
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
			return new String(bytes).substring(0,size-1);
		} catch (IOException e) {
			ANSI.printErr("getStringFromStream method failed.",e);
			return "";
		}
	}
	
	public static FFProbePacket getProbePacket(File file) {
		FFProbePacket packet=null;
		if(file.exists()) {
			ProcessBuilder builder=new ProcessBuilder();
			builder.directory(new File("./"));
			builder.command("ffprobe","-v","quiet","-print_format","json","-show_format",file.getAbsolutePath());
			try {
				Process process=builder.start();
				InputStream input=process.getInputStream();
				StringBuilder out=new StringBuilder();
				long time=System.currentTimeMillis();
				while(process.isAlive()) {
					int avail=input.available();
					if(avail>0) out.append(new String(input.readNBytes(avail)));
					if(System.currentTimeMillis()-time>10000l) {
						ANSI.printWarn("FFProbePacket timeout.");
						process.destroyForcibly();
					}
				}
				process.inputReader().lines().forEach(line->out.append(line));
				if(!out.isEmpty()) {
					JsonObject o=JsonParser.parseString(out.toString()).getAsJsonObject();
					if(o.has("format")) packet=GSON.fromJson(o.get("format"),FFProbePacket.class);
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
			Process process=builder.start();
			InputStream reader=process.getInputStream();
			StringBuilder out=new StringBuilder();
			long start=System.currentTimeMillis();
			while(process.isAlive()) {
				int avail=reader.available();
				if(avail>0) out.append(new String(reader.readNBytes(avail)));
				if(System.currentTimeMillis()-start>30000l) {
					ANSI.printWarn("InfoPacket timeout.");
					process.destroyForcibly();
				}
			}
			if(!out.isEmpty()) info=GSON.fromJson(out.toString(),InfoPacket.class);
			
		} catch (Exception e) {
			ANSI.printErr("getinfoPacket method failed.",e);
		}
		return info==null?new InfoPacket():info;
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
					,"--output","%(title).64s.%(ext)s"
			);
		} else if(args.contains("--auto ")) {
			args=args.replace("--auto","");
			builder.command("yt-dlp"
					,"--progress-delta","2"
					,"--restrict-filenames"
					,"--embed-metadata"
					,"--embed-thumbnail"
					,"--output","%(title).64s.%(ext)s"
			);
			if(Config.YOUTUBE_USE_COOKIES&&Config.YOUTUBE_COOKIES.exists()) {
				builder.command().add("--cookies");
				builder.command().add(Config.YOUTUBE_COOKIES.getAbsolutePath());
			}
			File dir=getOrCreateMediaDir("temp");
			if(dir!=null) builder.directory(dir);
			downloadable=true;
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
					,"--output","%(title).64s.%(ext)s"
					,"--restrict-filenames"
					,"--no-playlist"
			);
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
					case("dir"):
						if(parse.length==2) {
							File dir=getOrCreateMediaDir(parse[1]);
							parse[1]="";
							if(dir!=null) {
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
		
		InfoPacket infoPacket=Helper.getInfoPacket(url);
		if(!url.isEmpty()) builder.command().add(url);
		infoPacket.downloadable=downloadable;
		
		return Map.entry(builder,Optional.ofNullable(infoPacket));
	}
	
	public static File getOrCreateMediaDir(String name) {
		File dir=new File(Config.DL_MUSIC_PATH.concat("/").concat(name));
		if(!dir.exists()) dir.mkdir();
		if(dir.isDirectory()) return dir;
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
