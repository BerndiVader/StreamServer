package com.gmail.berndivader.streamserver;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.console.ConsoleRunner;

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
				ConsoleRunner.println(e.getMessage());
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
				ConsoleRunner.println(e.getMessage());
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

}
