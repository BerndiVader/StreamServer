package com.gmail.berndivader.streamserver.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Scanner;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.StreamServer;
import com.gmail.berndivader.streamserver.discord.permission.Guild;
import com.gmail.berndivader.streamserver.discord.permission.User;
import com.gmail.berndivader.streamserver.term.ANSI;
import com.gmail.berndivader.streamserver.youtube.PrivacyStatus;

public class Config {
			
	public static HashMap<Long,Guild>DISCORD_PERMITTED_GUILDS=new HashMap<Long,Guild>();
	public static HashMap<Long,User>DISCORD_PERMITTED_USERS=new HashMap<Long,User>();
	
	public static String BROADCAST_DEFAULT_TITLE="Lorem ipsum";
	public static String BROADCAST_DEFAULT_DESCRIPTION="Lorem ipsum dolor sit amet, consectetur adipiscing elit.";
	public static String BROADCAST_DEFAULT_PRIVACY="private";
	public static Long BROADCAST_REPEAT_TIMER_INTERVAL=1l;
	
	public static String YOUTUBE_STREAM_KEY="xxxx-xxxx-xxxx-xxxx-xxxx";
	public static String YOUTUBE_STREAM_URL="rtmp://a.rtmp.youtube.com/live2";
	
	public static String YOUTUBE_API_KEY="yt-api-key";
	public static String YOUTUBE_CLIENT_ID="yt-client-id";
	public static String YOUTUBE_CLIENT_SECRET="yt-client-secret";
	public static String YOUTUBE_AUTH_REDIRECT="https://your.redirect.page";
	public static String YOUTUBE_ACCESS_TOKEN="";
	public static String YOUTUBE_REFRESH_TOKEN="";
	public static Long YOUTUBE_TOKEN_TIMESTAMP=0l;
	public static final Long YOUTUBE_TOKEN_EXPIRE_TIME=3599l;
	
	public static Boolean YOUTUBE_USE_COOKIES=false;
	public static File YOUTUBE_COOKIES;
	
	public static Boolean STREAM_BOT_START=true;
	public static Boolean DISCORD_BOT_START=true;
	
	public static String PLAYLIST_PATH="./playlist";
	public static String PLAYLIST_PATH_CUSTOM="./custom";
	
	public static String DL_ROOT_PATH="./library";
	private static String DL_MUSIC_PATH="/music";
	private static String DL_TEMP_PATH="/temp";
	private static String DL_MEDIA_PATH="/media";
	public static String DL_WWW_THUMBNAIL_PATH="/absolute/path/to/thumbnails";
	
	public static Long DL_TIMEOUT_SECONDS=1800l;
	public static String DL_URL="https://path.to.php";
	public static String DL_INTERVAL_FORMAT="DAY";
	public static Integer DL_INTERVAL_VALUE=14;
	
	public static String DATABASE_PREFIX="jdbc:mysql://";
	public static String DATABASE_HOST="x.x.xxx.xxx";
	public static String DATABASE_PORT="3306";
	public static String DATABASE_NAME="ytbot";
	public static String DATABASE_USER="default";
	public static String DATABASE_PWD="default";
	
	public static String DISCORD_TOKEN="default";
	public static String DISCORD_VOICE_CHANNEL_NAME="Voice Channel Name";
	public static Boolean DISCORD_MUSIC_BOT=false;
	public static Long DISCORD_ROLE_ID=0l;
	public static Long DISCORD_CHANNEL_ID=0l;
	
	public static String HELP_TEXT;
	public static String DISCORD_HELP_TEXT;
	
	public static Boolean DEBUG=false;
	
	public static String working_dir;
	public static File config_dir,config_file;
	public static ConfigData data;
	
	public static Config instance;
	
	static {
        try {
   			HELP_TEXT=inputstreamToString(StreamServer.class.getResourceAsStream("/help.txt"));
   			DISCORD_HELP_TEXT=inputstreamToString(StreamServer.class.getResourceAsStream("/discord_help.txt"));
            working_dir=new File(StreamServer.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
        } catch (URISyntaxException ex) {
        	ANSI.printErr("URISyntaxException",ex);
        }
		config_dir=new File(working_dir,"config");
		config_file=new File(config_dir,"config.json");
		if(!config_dir.exists()) {
			config_dir.mkdir();
			createDefault();
		}
		if(!config_file.exists()) {
			createDefault();
		}
		YOUTUBE_COOKIES=new File(config_dir,"cookies.txt");
	}
	
	public Config() {
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
			data.DL_ROOT_PATH=DL_ROOT_PATH;
			data.DL_MUSIC_PATH=DL_MUSIC_PATH;
			data.DL_TEMP_PATH=DL_TEMP_PATH;
			data.DL_MEDIA_PATH=DL_MEDIA_PATH;
			data.DL_WWW_THUMBNAIL_PATH=DL_WWW_THUMBNAIL_PATH;
			data.DL_TIMEOUT_SECONDS=DL_TIMEOUT_SECONDS;
			data.DL_URL=DL_URL;
			data.DL_INTERVAL_FORMAT=DL_INTERVAL_FORMAT;
			data.DL_INTERVAL_VALUE=DL_INTERVAL_VALUE;
			data.YOUTUBE_STREAM_KEY=YOUTUBE_STREAM_KEY;
			data.YOUTUBE_STREAM_URL=YOUTUBE_STREAM_URL;
			data.BROADCAST_DEFAULT_TITLE=BROADCAST_DEFAULT_TITLE;
			data.BROADCAST_DEFAULT_DESCRIPTION=BROADCAST_DEFAULT_DESCRIPTION;
			data.BROADCAST_DEFAULT_PRIVACY=BROADCAST_DEFAULT_PRIVACY;
			data.BROADCAST_REPEAT_TIMER_INTERVAL=BROADCAST_REPEAT_TIMER_INTERVAL;
			data.YOUTUBE_API_KEY=YOUTUBE_API_KEY;
			data.YOUTUBE_USE_COOKIES=YOUTUBE_USE_COOKIES;
			data.YOUTUBE_CLIENT_ID=YOUTUBE_CLIENT_ID;
			data.YOUTUBE_CLIENT_SECRET=YOUTUBE_CLIENT_SECRET;
			data.YOUTUBE_AUTH_REDIRECT=YOUTUBE_AUTH_REDIRECT;
			data.YOUTUBE_ACCESS_TOKEN=YOUTUBE_ACCESS_TOKEN;
			data.YOUTUBE_REFRESH_TOKEN=YOUTUBE_REFRESH_TOKEN;
			data.YOUTUBE_TOKEN_TIMESTAMP=YOUTUBE_TOKEN_TIMESTAMP;
			data.STREAM_BOT_START=STREAM_BOT_START;
			data.DATABASE_HOST=DATABASE_HOST;
			data.DATABASE_PORT=DATABASE_PORT;
			data.DATABASE_NAME=DATABASE_NAME;
			data.DATABASE_USER=DATABASE_USER;
			data.DATABASE_PWD=DATABASE_PWD;
			data.DISCORD_TOKEN=DISCORD_TOKEN;
			data.DISCORD_VOICE_CHANNEL_NAME=DISCORD_VOICE_CHANNEL_NAME;
			data.DISCORD_MUSIC_BOT=DISCORD_MUSIC_BOT;
			data.DISCORD_ROLE_ID=DISCORD_ROLE_ID;
			data.DISCORD_BOT_START=DISCORD_BOT_START;
			data.DISCORD_PERMITTED_GUILDS=DISCORD_PERMITTED_GUILDS;
			data.DISCORD_PERMITTED_USERS=DISCORD_PERMITTED_USERS;
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
			if(data.DL_ROOT_PATH!=null) DL_ROOT_PATH=data.DL_ROOT_PATH;
			if(data.DL_MUSIC_PATH!=null) DL_MUSIC_PATH=data.DL_MUSIC_PATH;
			if(data.DL_TEMP_PATH!=null) DL_TEMP_PATH=data.DL_TEMP_PATH;
			if(data.DL_MEDIA_PATH!=null) DL_MEDIA_PATH=data.DL_MEDIA_PATH;
			if(data.DL_WWW_THUMBNAIL_PATH!=null) DL_WWW_THUMBNAIL_PATH=data.DL_WWW_THUMBNAIL_PATH;
			if(data.DL_TIMEOUT_SECONDS!=null) DL_TIMEOUT_SECONDS=data.DL_TIMEOUT_SECONDS;
			if(data.DL_URL!=null) DL_URL=data.DL_URL;
			if(data.DL_INTERVAL_FORMAT!=null) DL_INTERVAL_FORMAT=data.DL_INTERVAL_FORMAT;
			if(data.DL_INTERVAL_VALUE!=null) DL_INTERVAL_VALUE=data.DL_INTERVAL_VALUE;
			if(data.YOUTUBE_STREAM_KEY!=null) YOUTUBE_STREAM_KEY=data.YOUTUBE_STREAM_KEY;
			if(data.YOUTUBE_STREAM_URL!=null) YOUTUBE_STREAM_URL=data.YOUTUBE_STREAM_URL;
			if(data.BROADCAST_DEFAULT_TITLE!=null) BROADCAST_DEFAULT_TITLE=data.BROADCAST_DEFAULT_TITLE;
			if(data.BROADCAST_DEFAULT_DESCRIPTION!=null) BROADCAST_DEFAULT_DESCRIPTION=data.BROADCAST_DEFAULT_DESCRIPTION;
			if(data.BROADCAST_DEFAULT_PRIVACY!=null) BROADCAST_DEFAULT_PRIVACY=data.BROADCAST_DEFAULT_PRIVACY;
			if(data.BROADCAST_REPEAT_TIMER_INTERVAL!=null) BROADCAST_REPEAT_TIMER_INTERVAL=data.BROADCAST_REPEAT_TIMER_INTERVAL;
			if(data.YOUTUBE_API_KEY!=null) YOUTUBE_API_KEY=data.YOUTUBE_API_KEY;
			if(data.YOUTUBE_USE_COOKIES!=null) YOUTUBE_USE_COOKIES=data.YOUTUBE_USE_COOKIES;
			if(data.YOUTUBE_CLIENT_ID!=null) YOUTUBE_CLIENT_ID=data.YOUTUBE_CLIENT_ID;
			if(data.YOUTUBE_CLIENT_SECRET!=null) YOUTUBE_CLIENT_SECRET=data.YOUTUBE_CLIENT_SECRET;			
			if(data.YOUTUBE_AUTH_REDIRECT!=null) YOUTUBE_AUTH_REDIRECT=data.YOUTUBE_AUTH_REDIRECT;			
			if(data.YOUTUBE_ACCESS_TOKEN!=null) YOUTUBE_ACCESS_TOKEN=data.YOUTUBE_ACCESS_TOKEN;			
			if(data.YOUTUBE_REFRESH_TOKEN!=null) YOUTUBE_REFRESH_TOKEN=data.YOUTUBE_REFRESH_TOKEN;			
			if(data.YOUTUBE_TOKEN_TIMESTAMP!=null) YOUTUBE_TOKEN_TIMESTAMP=data.YOUTUBE_TOKEN_TIMESTAMP;			
			if(data.STREAM_BOT_START!=null) STREAM_BOT_START=data.STREAM_BOT_START;
			if(data.DATABASE_HOST!=null) DATABASE_HOST=data.DATABASE_HOST;
			if(data.DATABASE_PORT!=null) DATABASE_PORT=data.DATABASE_PORT;
			if(data.DATABASE_NAME!=null) DATABASE_NAME=data.DATABASE_NAME;
			if(data.DATABASE_USER!=null) DATABASE_USER=data.DATABASE_USER;
			if(data.DATABASE_PWD!=null) DATABASE_PWD=data.DATABASE_PWD;
			if(data.DISCORD_TOKEN!=null) DISCORD_TOKEN=data.DISCORD_TOKEN;
			if(data.DISCORD_VOICE_CHANNEL_NAME!=null) DISCORD_VOICE_CHANNEL_NAME=data.DISCORD_VOICE_CHANNEL_NAME;
			if(data.DISCORD_MUSIC_BOT!=null) DISCORD_MUSIC_BOT=data.DISCORD_MUSIC_BOT;
			if(data.DISCORD_ROLE_ID!=null) DISCORD_ROLE_ID=data.DISCORD_ROLE_ID;
			if(data.DISCORD_BOT_START!=null) DISCORD_BOT_START=data.DISCORD_BOT_START;
			if(data.DISCORD_PERMITTED_GUILDS!=null) DISCORD_PERMITTED_GUILDS=data.DISCORD_PERMITTED_GUILDS;
			if(data.DISCORD_PERMITTED_USERS!=null) DISCORD_PERMITTED_USERS=data.DISCORD_PERMITTED_USERS;
			
		} catch (IOException e) {
			ANSI.printErr("Error while loading config file.",e);
			return false;
		}
		return true;
	}
	
	public static String musicPath() {
		return DL_ROOT_PATH+DL_MUSIC_PATH;
	}
	
	public static String mediaPath() {
		return DL_ROOT_PATH+DL_MEDIA_PATH;
	}
	
	public static String tempPath() {
		return DL_ROOT_PATH+DL_TEMP_PATH;
	}
	
	public static String connectionString() {
		return DATABASE_PREFIX+DATABASE_HOST+":"+DATABASE_PORT+"/"+DATABASE_NAME;
	}
	
	public static PrivacyStatus broadcastPrivacyStatus() {
		String priv=BROADCAST_DEFAULT_PRIVACY.toUpperCase();
		return PrivacyStatus.isEnum(priv)?PrivacyStatus.valueOf(priv):PrivacyStatus.UNLISTED;
	}
	
	private static String inputstreamToString(InputStream is) {
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
