package com.gmail.berndivader.streamserver;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;

public class Utils {
	
	public static int getFilePosition(String name) {
		if(!name.isEmpty()) {
			for(int i1=0;i1<BroadcastRunner.files.length;i1++) {
				String file=BroadcastRunner.files[i1].getName().toLowerCase();
				if(file.equals(name)) {
					return i1;
				}
			}
		}
		return -1;
	}
	
	public static ArrayList<String> getPlaylistAsList(String regex) {
		ArrayList<String>list=new ArrayList<>();
		for(int i1=0;i1<BroadcastRunner.files.length;i1++) {
			String name=BroadcastRunner.files[i1].getName().toLowerCase();
			if(name.matches(regex)) {
				list.add(name);
			}
		}
		return list;
	}
	
	public static String getPlaylistAsString(String regex) {
		String playlist="";
		for(int i1=0;i1<BroadcastRunner.files.length;i1++) {
			String name=BroadcastRunner.files[i1].getName().toLowerCase();
			if(name.matches(regex)) {
				playlist+=name+"\n";
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
