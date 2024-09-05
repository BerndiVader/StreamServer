package com.gmail.berndivader.streamserver.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
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
	
	public static Boolean STREAM_BOT_START=false;
	public static Boolean DISCORD_BOT_START=false;
	
	public static String PLAYLIST_PATH="./playlist";
	public static String PLAYLIST_PATH_CUSTOM="./custom";
	
	public static String DL_ROOT_PATH="./library";
	public static String DL_MUSIC_PATH="/music";
	public static String DL_TEMP_PATH="/temp";
	public static String DL_MEDIA_PATH="/media";
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
	public static Long DATABASE_TIMEOUT_SECONDS=10l;
	
	public static String DISCORD_TOKEN="default";
	public static String DISCORD_VOICE_CHANNEL_NAME="Voice Channel Name";
	public static Boolean DISCORD_MUSIC_BOT=false;
	public static Boolean DISCORD_MUSIC_AUTOPLAY=false;
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
		try(FileWriter writer=new FileWriter(config_file.getAbsoluteFile())) {
			Field[]fields=ConfigData.class.getDeclaredFields();
			for(Field field:fields) {
				try {
					Field config=Config.class.getField(field.getName());
					field.set(data,config.get(null));
				} catch (NoSuchFieldException | IllegalAccessException e) {
					ANSI.printErr(e.getMessage(),e);
				}
			}
	        Helper.GSON.toJson(data,writer);
		} catch (IOException e) {
			ANSI.printErr("Error while saving config file.",e);
			return false;
		}
		return true;
	}

	public static boolean loadConfig() {
		try(FileReader reader=new FileReader(config_file.getAbsoluteFile())) {
			data=Helper.GSON.fromJson(reader,ConfigData.class);
			Field[]fields=ConfigData.class.getDeclaredFields();
			for(Field field:fields) {
				try {
					Object value=field.get(data);
					if(value!=null) {
						Field staticField=Config.class.getField(field.getName());
						staticField.set(null,value);
					}
				} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
					ANSI.printErr(e.getMessage(),e);
				}
			}
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
