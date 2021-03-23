package com.gmail.berndivader.streamserver;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import com.gmail.berndivader.streamserver.config.Config;

public class Utils {
	
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
	
	public static ArrayList<String> getPlaylistAsList(String regex) {
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
				ConsoleRunner.println(e.getMessage());
			}
		}
		return list;
	}
	
	public static String getPlaylistAsString(String regex) {
		if(regex.contains("*")) {
			regex=regex.replaceAll("*","(.*)");
		} else {
			regex="(.*)"+regex+("(.*)");
		}
		String playlist="";
		File[]files=Helper.files.clone();
		for(int i1=0;i1<files.length;i1++) {
			String name=files[i1].getName().toLowerCase();
			try {
				if(name.matches(regex)) {
					playlist+=name+"\n";
				}
			} catch (Exception e) {
				ConsoleRunner.println(e.getMessage());
			}
		}
		return playlist;
	}
	
	public static File[] shufflePlaylist(File[] files) {
		Random random=ThreadLocalRandom.current();
		for (int i1=files.length-1;i1>0;i1--) {
			int index=random.nextInt(i1+1);
			File a=files[index];
			files[index]=files[i1];
			files[i1]=a;
		}
		return files;
	}
	
	public static File[] refreshPlaylist() {
    	File file=new File(Config.PLAYLIST_PATH);
    	File[]files;
    	
    	if(file.isDirectory()) {
    		FileFilter filter=new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.getAbsolutePath().toLowerCase().endsWith(".mp4");
				}
			};
    		files=file.listFiles(filter);
    	} else if(file.isFile()) {
    		files=new File[] {file};
    	} else {
    		files=new File[0];
    	}
    	return files;
	}

}
