package com.gmail.berndivader.streamserver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	public final static Gson LGSON;
	public final static Gson GSON;
	
	static final Pattern EXTRACT_URL=Pattern.compile("\\b(https?)://[-A-Z0-9+&@#/%?=~_|!:,.;]*[-A-Z0-9+&@#/%=~_|]",Pattern.CASE_INSENSITIVE);

	private Helper() {}
	
	static {
		EXECUTOR=Executors.newCachedThreadPool();
		SCHEDULED_EXECUTOR=Executors.newSingleThreadScheduledExecutor();
		GSON=new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		LGSON=new GsonBuilder().setPrettyPrinting().setFieldNamingStrategy(s->s.getName().toLowerCase()).disableHtmlEscaping().create();
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
	
	private static String startAndWaitForProcess(ProcessBuilder builder,long timeout) throws Exception {
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
	
	public static FFProbePacket createProbePacket(File file) {
		FFProbePacket packet=null;
		if(file.exists()) {
			ProcessBuilder builder=new ProcessBuilder();
			builder.directory(new File("./"));
			builder.command("ffprobe","-v","quiet","-print_format","json","-show_format",file.getAbsolutePath());
			try {
				String out=startAndWaitForProcess(builder,10l);
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
	
	public static InfoPacket createInfoPacket(String url) {
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
			String out=startAndWaitForProcess(builder,10l);
			if(!out.isEmpty()) info=LGSON.fromJson(out.toString(),InfoPacket.class);
		} catch (Exception e) {
			ANSI.printErr("getinfoPacket method failed.",e);
		}
		return info!=null?info:new InfoPacket();
	}
	
	public static Entry<ProcessBuilder,InfoPacket> createDownloadBuilder(File defaultDirectory,String args) {
		ProcessBuilder builder=new ProcessBuilder();
		getOrCreateMediaDir(Config.DL_MEDIA_PATH).ifPresentOrElse(dir->builder.directory(dir),()->builder.directory(new File("./")));
		boolean downloadable=false;
		
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
		
		String url="";
		Matcher matcher=EXTRACT_URL.matcher(args);
		if(matcher.find()) {
			url=matcher.group();
			args=matcher.replaceAll("");
		}
		
		String[]commands=args.split("--");
		
		for(String command:commands) {
			if(command.isEmpty()) continue;
						
			String[]parse=command.trim().split(" ",2);
			if(parse.length>0) {
				switch(parse[0]) {
					case("temp"):
						getOrCreateMediaDir(Config.DL_MUSIC_PATH).ifPresent(dir->builder.directory(dir));;
						downloadable=true;
						break;
					case("music"):
						builder.command().addAll(Arrays.asList("--ignore-errors"
								,"--extract-audio"
								,"--format","bestaudio"
								,"--audio-format","mp3"
								,"--audio-quality","160K"
								,"--output","%(title).64s.%(ext)s"
								,"--no-playlist"
						));
						getOrCreateMediaDir(Config.DL_MUSIC_PATH).ifPresent(dir->builder.directory(dir));;
						break;
					case("link"):
						downloadable=true;
						break;
					case("url"):
						if(parse.length==2&&!parse[1].isBlank()) {
							url=parse[1];
						}
						break;
					case("cookies"):
						if(!Config.YOUTUBE_USE_COOKIES&&Config.YOUTUBE_COOKIES.exists()) {
							builder.command().add("--cookies");
							builder.command().add(Config.YOUTUBE_COOKIES.getAbsolutePath());
						}
						break;
					default:
						if(!parse[0].isEmpty()) builder.command().add("--"+parse[0]);
						if(parse.length==2&&!parse[1].isEmpty()) builder.command().add(parse[1]);
						break;
				}
			}
		}
		
		InfoPacket infoPacket=Helper.createInfoPacket(url);
		if(!url.isEmpty()) builder.command().add(url);
		infoPacket.downloadable=downloadable;
		
		return Map.entry(builder,infoPacket);
	}
	
	public static Optional<File> getOrCreateMediaDir(String name) {
		File dir=new File(name);
		if(!dir.exists()) dir.mkdir();
		if(dir.isDirectory()) return Optional.of(dir);
		if(Config.DEBUG) ANSI.printWarn("Failed to create directory: "+name);
		return Optional.empty();
	}
		
	public static String stringFloatToTime(String time) {
		float duration=0f;
		try {
			duration=Float.parseFloat(time);
		} catch (Exception e) {
			ANSI.printErr(e.getMessage(),e);
		}
	    int h=(int)(duration/3600);
	    int m=(int)((duration%3600)/60);
	    int s=(int)(duration%60);		
	    return String.format("%02d:%02d:%02d",h,m,s);
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
