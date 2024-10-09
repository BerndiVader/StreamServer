package com.gmail.berndivader.streamserver.config;

import java.awt.Point;
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
import com.gmail.berndivader.streamserver.YAMPB;
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
	
	public static String YOUTUBE_API_KEY="YT-API-KEY";
	public static String YOUTUBE_CLIENT_ID="YT-CLIENT-ID";
	public static String YOUTUBE_CLIENT_SECRET="YT-CLIENT-SECRET";
	public static String YOUTUBE_AUTH_REDIRECT="https://YOUR.OAUTH2-REDIRECT.PAGE";
	public static String YOUTUBE_ACCESS_TOKEN="YT-OAUTH2-ACCESS-TOKEN";
	public static String YOUTUBE_REFRESH_TOKEN="YT-OUATH2-REFRESH-TOKEN";
	public static Long YOUTUBE_TOKEN_TIMESTAMP=0l;
	public static final Long YOUTUBE_TOKEN_EXPIRE_TIME=3599l;
	public static final long BROADCAST_PLAYLIST_REFRESH_INTERVAL=60l;
	
	public static Boolean YOUTUBE_USE_COOKIES=false;
	public static File YOUTUBE_COOKIES;
	
	public static Boolean DISCORD_BOT_START=false;
	public static Boolean STREAM_BOT_START=false;
	public static Boolean DISCORD_MUSIC_BOT=false;
	
	public static String PLAYLIST_PATH="./playlist";
	public static String PLAYLIST_PATH_CUSTOM="./custom";
	
	public static String DL_ROOT_PATH="./library";
	public static String DL_MUSIC_PATH="/music";
	public static String DL_TEMP_PATH="/temp";
	public static String DL_MEDIA_PATH="/media";
	public static String DL_WWW_THUMBNAIL_PATH="/ABSOLUTE/PATH/TO/THUMBNAILS";
	public static Point DL_THUMBNAIL_SIZE=new Point(640,480);
	
	public static Long DL_TIMEOUT_SECONDS=1800l;
	public static String DL_URL="https://PATH.TO.PHP";
	public static String DL_INTERVAL_FORMAT="DAY";
	public static Integer DL_INTERVAL_VALUE=14;
	
	public static Boolean DATABASE_USE=false;
	public static String DATABASE_PREFIX="jdbc:mysql://";
	public static String DATABASE_HOST="MYSQL.HOST.NAME";
	public static String DATABASE_PORT="3306";
	public static String DATABASE_NAME="yampb";
	public static String DATABASE_USER="MYSQL-USER-NAME";
	public static String DATABASE_PWD="MYSQL-PASSWORD";
	public static Long DATABASE_TIMEOUT_SECONDS=10l;
	
	public static String DISCORD_CMD_PREFIX=".";
	public static String DISCORD_TOKEN="BOT-TOKEN";
	public static Boolean DISCORD_DELETE_CMD_MESSAGE=false;
	public static String DISCORD_VOICE_CHANNEL_NAME="VOICE-CHANNEL-NAME";
	public static Boolean DISCORD_MUSIC_AUTOPLAY=false;
	public static Long DISCORD_ROLE_ID=0l;
	
	public static String HELP_TEXT;
	public static String DISCORD_HELP_TEXT;
	public static String DISCORD_HELP_TEXT_TITLE="YAMPB Discord help";
	public static String YAMPB_ANSI;
	
	public static Boolean DEBUG=false;
	
	public static String working_dir;
	public static File config_dir,config_file;
	public static Data data;
	
	public static Config instance;
	public static boolean FRESH_INSTALL=false;
	
	public static boolean YTDLP_AVAIL;
	public static boolean FFMPEG_AVAIL;
	
	static {
		
		YTDLP_AVAIL=Helper.ytdlpAvail();
		FFMPEG_AVAIL=Helper.ffmpegAvail();
				
        try {
   			HELP_TEXT=inputstreamToString(YAMPB.class.getResourceAsStream("/help.txt"));
   			DISCORD_HELP_TEXT=inputstreamToString(YAMPB.class.getResourceAsStream("/discord_help.txt"));
   			YAMPB_ANSI=inputstreamToString(YAMPB.class.getResourceAsStream("/yampb.ansi"));
            working_dir=new File(YAMPB.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
        } catch (URISyntaxException ex) {
        	ANSI.error("URISyntaxException",ex);
        }
		config_dir=new File(working_dir,"config");
		config_file=new File(config_dir,"config.json");
		if(!config_dir.exists()) {
			FRESH_INSTALL=true;
			config_dir.mkdir();
			createDefault();
		}
		if(!config_file.exists()) {
			FRESH_INSTALL=true;
			createDefault();
		}
		YOUTUBE_COOKIES=new File(config_dir,"cookies.txt");
	}
	
	public Config() {
		ANSI.println("[CYAN]Loading config...");
		if(loadConfig()) {
			ANSI.println("[GREEN]DONE![/GREEN]");
			saveConfig();
		} else {
			ANSI.warn("Failed loading or creating config file. Using default values.");
		}
	}
	
	private static void createDefault() {
		config_dir.mkdir();
		data=new Data();
		saveConfig();
	}
	
	public static boolean saveConfig() {
		try(FileWriter writer=new FileWriter(config_file.getAbsoluteFile())) {
			Field[]fields=Data.class.getDeclaredFields();
			for(Field field:fields) {
				try {
					Field config=Config.class.getField(field.getName());
					field.set(data,config.get(null));
				} catch (NoSuchFieldException | IllegalAccessException e) {
					ANSI.error(e.getMessage(),e);
				}
			}
	        Helper.GSON.toJson(data,writer);
		} catch (IOException e) {
			ANSI.error("Error while saving config file.",e);
			return false;
		}
		return true;
	}

	public static boolean loadConfig() {
		try(FileReader reader=new FileReader(config_file.getAbsoluteFile())) {
			data=Helper.GSON.fromJson(reader,Data.class);
			Field[]fields=Data.class.getDeclaredFields();
			for(Field field:fields) {
				try {
					Object value=field.get(data);
					if(value!=null) {
						Field staticField=Config.class.getField(field.getName());
						staticField.set(null,value);
					}
				} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
					ANSI.error(e.getMessage(),e);
				}
			}
		} catch (IOException e) {
			ANSI.error("Error while loading config file.",e);
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
				ANSI.error("Error while reading from an InputStream",e1);
			}
		};
		return output;
	}
	
	
}
