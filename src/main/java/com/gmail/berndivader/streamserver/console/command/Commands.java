package com.gmail.berndivader.streamserver.console.command;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import com.gmail.berndivader.streamserver.YAMPB;
import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.discord.DiscordBot;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;
import com.gmail.berndivader.streamserver.mysql.DatabaseConnection;
import com.gmail.berndivader.streamserver.mysql.DatabaseConnection.STATUS;
import com.gmail.berndivader.streamserver.term.ANSI;

public class Commands {
	
	public static Commands instance;
	
	final static String PACKAGE_NAME="com/gmail/berndivader/streamserver/console/command/commands";
	static String fileName;
	public HashMap<String,String>cmds;
	
	static {
		try {
			fileName = URLDecoder.decode(
					YAMPB.class.getProtectionDomain().getCodeSource().getLocation().getPath(),
					StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			try {
				fileName = URLDecoder.decode(
						YAMPB.class.getProtectionDomain().getCodeSource().getLocation().getPath(),
						StandardCharsets.ISO_8859_1.toString());
			} catch (UnsupportedEncodingException e1) {
				ANSI.error("Error, there is no UTF-8 nor a ISO-8859 encoding avaible.",e1);
			}
		}
	}
	
	public Commands() {
		cmds=new HashMap<>();
		try {
			loadCommandClasses();
		} catch (IOException | ClassNotFoundException e) {
			ANSI.error("Failed to instantiate console commands.",e);
		}
	}
	
	void loadCommandClasses() throws IOException, ClassNotFoundException {
		
		try(JarInputStream jarStream=new JarInputStream(new FileInputStream(fileName))) {
			JarEntry entry;
			while(jarStream.available()==1) {
				entry=jarStream.getNextJarEntry();
				if(entry!=null) {
					String clazzName=entry.getName();
					if(clazzName.endsWith(".class")&&clazzName.startsWith(PACKAGE_NAME)) {
						clazzName=clazzName.substring(0,clazzName.length()-6).replace("/",".");
						Class<?>clazz=Class.forName(clazzName);
						ConsoleCommand anno=clazz.getAnnotation(ConsoleCommand.class);
						if(anno!=null) {
							boolean add=true;
							for(int i=0;i<anno.requireds().length;i++) {
								switch(anno.requireds()[i]) {
									case BROADCASTRUNNER:
										add&=BroadcastRunner.instance!=null;
										break;
									case DISCORDBOT:
										add&=DiscordBot.instance!=null;
										break;
									case DATABASE:
										add&=DatabaseConnection.status==STATUS.OK;
										break;
									case DISCORDMUSIC:
										add&=Config.DISCORD_MUSIC_BOT;
										break;
									case FFMPEG:
										add&=Config.FFMPEG_AVAIL;
										break;
									case YTDLP:
										add&=Config.YTDLP_AVAIL;
										break;
									default:
										add=true;
										break;
								}
							}
							if(add) cmds.put(anno.name(),clazzName);
						}
					}
				}
			}
		}
		
	}
	
	public Command getCommand(String name) {
		if(cmds.containsKey(name)) {
			String clazzName=cmds.get(name);
			try {
				Class<?>clazz=Class.forName(clazzName);
				if(clazz!=null) {
					return (Command)clazz.getDeclaredConstructor().newInstance();
				}
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				ANSI.error("Error getting console command class.",e);
			}
		}
		return null;
	}
	
}
