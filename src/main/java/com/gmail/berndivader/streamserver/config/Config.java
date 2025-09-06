package com.gmail.berndivader.streamserver.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.Scanner;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.YAMPB;
import com.gmail.berndivader.streamserver.term.ANSI;
import com.gmail.berndivader.streamserver.youtube.PrivacyStatus;

public class Config {
			
	public static String HELP_TEXT;
	public static String DISCORD_HELP_TEXT;
	public static String DISCORD_HELP_TEXT_TITLE="YAMPB Discord help";
	public static String YAMPB_ANSI;
	
	public static Broadcaster BROADCASTER=new Broadcaster();
	public static String DATABASE_PREFIX="jdbc:mysql://";
	public static MySql MYSQL=new MySql();
	public static Downloader DOWNLOADER=new Downloader();
	public static Discord DISCORD=new Discord(); 
	public static WebSocket WEBSOCKET=new WebSocket();
	
	
	
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
		return DOWNLOADER.ROOT_PATH+DOWNLOADER.MUSIC_PATH;
	}
	
	public static String mediaPath() {
		return DOWNLOADER.ROOT_PATH+DOWNLOADER.MEDIA_PATH;
	}
	
	public static String tempPath() {
		return DOWNLOADER.ROOT_PATH+DOWNLOADER.TEMP_PATH;
	}
	
	public static String connectionString() {
		return DATABASE_PREFIX+MYSQL.HOST+":"+MYSQL.PORT+"/"+MYSQL.NAME;
	}
	
	public static PrivacyStatus broadcastPrivacyStatus() {
		String priv=BROADCASTER.BROADCAST_DEFAULT_PRIVACY.toUpperCase();
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
	
	public static boolean cookiesExists() {
		return getCookies().exists();
	}
	
	public static File getCookies() {
		return new File(config_dir,"cookies.txt");
	}
	
	public static boolean torAccessible() {
		try(Socket socket=new Socket()) {
			socket.connect(new InetSocketAddress(DOWNLOADER.TOR_HOST,DOWNLOADER.TOR_PORT),0);
		} catch(Exception e) {
			return false;
		}
		return true;
	}
	
}
