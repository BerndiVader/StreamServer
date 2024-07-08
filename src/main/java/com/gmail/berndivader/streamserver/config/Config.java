package com.gmail.berndivader.streamserver.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Scanner;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.StreamServer;
import com.gmail.berndivader.streamserver.term.ANSI;

public class Config {
	
	public static String STREAM_KEY="xxxx-xxxx-xxxx-xxxx-xxxx";
	public static String STREAM_URL="rtmp://a.rtmp.youtube.com/live2";
	
	public static String YOUTUBE_LINK="https://youtu.be/xxxxxxx";
	public static String YOUTUBE_KEY="yt-api-key";
	public static String YOUTUBE_CHANNEL_ID="yt-channel-id";
	
	public static Boolean YOUTUBE_USE_COOKIES=false;
	public static File YOUTUBE_COOKIES;
	
	public static Boolean STREAM_BOT_START=true;
	public static Boolean DISCORD_BOT_START=true;
	
	public static String PLAYLIST_PATH="./playlist";
	public static String PLAYLIST_PATH_CUSTOM="./custom";
	
	public static String DL_MUSIC_PATH="./music";
	public static String DL_TEMP_PATH="./music/temp";
	public static String DL_MEDIA_PATH="./music/media";
	
	public static Long DL_TIMEOUT_SECONDS=1800l;
	public static String DL_URL="https://path.to.php";
	public static String DL_INTERVAL_FORMAT="DAY";
	public static Integer DL_INTERVAL_VALUE=14;
	
	public static String DATABASE_CONNECTION="jdbc:mysql://x.x.xxx.xxx:3306/ytbot";
	public static String DATABASE_USER="default";
	public static String DATABASE_PWD="default";
	
	public static String DISCORD_TOKEN="default";
	public static String DISCORD_CHANNEL="Mett TV";
	public static String DISCORD_ROLE="Schef";
	public static String DISCORD_COMMAND_PREFIX=".mtv";
	public static Boolean DISCORD_RESPONSE_TO_PRIVATE=true;
	
	public static String HELP_TEXT;
	public static String DISCORD_HELP_TEXT;
	
	public static Boolean DEBUG=false;
	
	public static File working_dir,config_dir,config_file;
	public static ConfigData data;
	
	public static Config instance;
	
	static {
        try {
   			HELP_TEXT=inputstreamToString(StreamServer.class.getResourceAsStream("/help.txt"));
   			DISCORD_HELP_TEXT=inputstreamToString(StreamServer.class.getResourceAsStream("/discord_help.txt"));
            URI uri=StreamServer.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            working_dir=new File(uri.getPath().replace(new File(uri).getName(),""));
        } catch (URISyntaxException ex) {
        	ANSI.printErr("URISyntaxException",ex);
        }
		config_dir=new File(working_dir.getAbsolutePath()+"/config");
		config_file=new File(config_dir.getAbsolutePath()+"/config.json");
		if(!config_dir.exists()) {
			config_dir.mkdir();
			createDefault();
		}
		if(!config_file.exists()) {
			createDefault();
		}
		YOUTUBE_COOKIES=new File(config_dir.getAbsolutePath().concat("/cookies.txt"));
	}
	
	public Config() {
		instance=this;
		ANSI.println("[WHITE]Load or create config...");
		if(loadConfig()) {
			ANSI.println("[GREEN]DONE![/GREEN]");
			saveConfig();
		} else {
			ANSI.printWarn("Failed loading or creating config file. Using default values.");
		}
	}
	
	private static void createDefault() {
		config_dir.mkdir();
		data=new ConfigData();
		saveConfig();
	}
	
	public static boolean saveConfig() {
		boolean ok=true;
		try (FileWriter writer=new FileWriter(config_file.getAbsoluteFile())) {
			data.PLAYLIST_PATH=PLAYLIST_PATH;
			data.PLAYLIST_PATH_CUSTOM=PLAYLIST_PATH_CUSTOM;
			data.DL_MUSIC_PATH=DL_MUSIC_PATH;
			data.DL_TEMP_PATH=DL_TEMP_PATH;
			data.DL_MEDIA_PATH=DL_MEDIA_PATH;
			data.DL_TIMEOUT_SECONDS=DL_TIMEOUT_SECONDS;
			data.DL_URL=DL_URL;
			data.DL_INTERVAL_FORMAT=DL_INTERVAL_FORMAT;
			data.DL_INTERVAL_VALUE=DL_INTERVAL_VALUE;
			data.STREAM_KEY=STREAM_KEY;
			data.STREAM_URL=STREAM_URL;
			data.YOUTUBE_LINK=YOUTUBE_LINK;
			data.YOUTUBE_KEY=YOUTUBE_KEY;
			data.YOUTUBE_USE_COOKIES=YOUTUBE_USE_COOKIES;
			data.YOUTUBE_CHANNEL_ID=YOUTUBE_CHANNEL_ID;
			data.STREAM_BOT_START=STREAM_BOT_START;
			data.DATABASE_CONNECTION=DATABASE_CONNECTION;
			data.DATABASE_USER=DATABASE_USER;
			data.DATABASE_PWD=DATABASE_PWD;
			data.DISCORD_TOKEN=DISCORD_TOKEN;
			data.DISCORD_CHANNEL=DISCORD_CHANNEL;
			data.DISCORD_ROLE=DISCORD_ROLE;
			data.DISCORD_COMMAND_PREFIX=DISCORD_COMMAND_PREFIX;
			data.DISCORD_RESPONSE_TO_PRIVATE=DISCORD_RESPONSE_TO_PRIVATE;
			data.DISCORD_BOT_START=DISCORD_BOT_START;
	        Helper.GSON.toJson(data,writer);
		} catch (IOException e) {
			ok=false;
			ANSI.printErr("Error while saving config file.",e);
		}		
		return ok;
	}
	
	public static boolean loadConfig() {
		try (FileReader reader=new FileReader(config_file.getAbsoluteFile())) {
			data=Helper.GSON.fromJson(reader,ConfigData.class);
			if(data.PLAYLIST_PATH!=null) PLAYLIST_PATH=data.PLAYLIST_PATH;
			if(data.PLAYLIST_PATH_CUSTOM!=null) PLAYLIST_PATH_CUSTOM=data.PLAYLIST_PATH_CUSTOM;
			if(data.DL_MUSIC_PATH!=null) DL_MUSIC_PATH=data.DL_MUSIC_PATH;
			if(data.DL_TEMP_PATH!=null) DL_TEMP_PATH=data.DL_TEMP_PATH;
			if(data.DL_MEDIA_PATH!=null) DL_MEDIA_PATH=data.DL_MEDIA_PATH;
			if(data.DL_TIMEOUT_SECONDS!=null) DL_TIMEOUT_SECONDS=data.DL_TIMEOUT_SECONDS;
			if(data.DL_URL!=null) DL_URL=data.DL_URL;
			if(data.DL_INTERVAL_FORMAT!=null) DL_INTERVAL_FORMAT=data.DL_INTERVAL_FORMAT;
			if(data.DL_INTERVAL_VALUE!=null) DL_INTERVAL_VALUE=data.DL_INTERVAL_VALUE;
			if(data.STREAM_KEY!=null) STREAM_KEY=data.STREAM_KEY;
			if(data.STREAM_URL!=null) STREAM_URL=data.STREAM_URL;
			if(data.YOUTUBE_LINK!=null) YOUTUBE_LINK=data.YOUTUBE_LINK;
			if(data.YOUTUBE_KEY!=null) YOUTUBE_KEY=data.YOUTUBE_KEY;
			if(data.YOUTUBE_USE_COOKIES!=null) YOUTUBE_USE_COOKIES=data.YOUTUBE_USE_COOKIES;
			if(data.YOUTUBE_CHANNEL_ID!=null) YOUTUBE_CHANNEL_ID=data.YOUTUBE_CHANNEL_ID;
			if(data.STREAM_BOT_START!=null) STREAM_BOT_START=data.STREAM_BOT_START;
			if(data.DATABASE_CONNECTION!=null) DATABASE_CONNECTION=data.DATABASE_CONNECTION;
			if(data.DATABASE_USER!=null) DATABASE_USER=data.DATABASE_USER;
			if(data.DATABASE_PWD!=null) DATABASE_PWD=data.DATABASE_PWD;
			if(data.DISCORD_TOKEN!=null) DISCORD_TOKEN=data.DISCORD_TOKEN;
			if(data.DISCORD_CHANNEL!=null) DISCORD_CHANNEL=data.DISCORD_CHANNEL;
			if(data.DISCORD_ROLE!=null) DISCORD_ROLE=data.DISCORD_ROLE;
			if(data.DISCORD_COMMAND_PREFIX!=null) DISCORD_COMMAND_PREFIX=data.DISCORD_COMMAND_PREFIX;
			if(data.DISCORD_RESPONSE_TO_PRIVATE!=null) DISCORD_RESPONSE_TO_PRIVATE=data.DISCORD_RESPONSE_TO_PRIVATE;
			if(data.DISCORD_BOT_START!=null) DISCORD_BOT_START=data.DISCORD_BOT_START;
		} catch (IOException e) {
			ANSI.printErr("Error while loading config file.",e);
			return false;
		}
		return true;
	}
	
	static String inputstreamToString(InputStream is) {
		String output=null;
		try (Scanner s=new Scanner(is)){
			s.useDelimiter("\\A");
			output=s.hasNext()?s.next():"";
			try {
				is.close();
			} catch (IOException e1) {
				ANSI.printErr("Error while reading from an InputStream",e1);
			}
		};
		return output;
	}
	
	
}
