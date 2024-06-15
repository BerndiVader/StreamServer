package com.gmail.berndivader.streamserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.ffmpeg.InfoPacket;
import com.gmail.berndivader.streamserver.term.ANSI;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Helper {
	
	public final static ExecutorService EXECUTOR;
	public final static ScheduledExecutorService SCHEDULED_EXECUTOR;
	public final static CloseableHttpClient HTTP_CLIENT;
	public final static Gson gson;
	public static File[] files;
	public static File[] customs;
	
	static {
		files=new File[0];
		customs=new File[0];
		EXECUTOR=Executors.newFixedThreadPool(10);
		SCHEDULED_EXECUTOR=Executors.newScheduledThreadPool(5);
		HTTP_CLIENT=HttpClients.createMinimal();
		gson=new GsonBuilder().create();
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
	
	public static InfoPacket getDLPinfoPacket(String url,File directory) {
		//yt-dlp --quiet --no-warnings --dump-single-json
		ProcessBuilder infoBuilder=new ProcessBuilder();
		infoBuilder.directory(directory);
		infoBuilder.command("yt-dlp","--quiet","--no-warnings","--dump-single-json",url);
		
		InfoPacket info=null;
		try {
			Process infoProc=infoBuilder.start();
			BufferedReader reader=infoProc.inputReader();
			while(infoProc.isAlive()) {
				if(reader.ready()) {
					info=new GsonBuilder().create().fromJson(infoProc.inputReader().readLine(),InfoPacket.class);
				}
			}
		} catch (IOException e) {
			ANSI.printErr("getDLPinfoPacket method failed.",e);
		}
		return info;
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
