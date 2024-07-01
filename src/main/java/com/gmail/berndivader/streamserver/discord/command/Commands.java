package com.gmail.berndivader.streamserver.discord.command;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import com.gmail.berndivader.streamserver.StreamServer;
import com.gmail.berndivader.streamserver.annotation.DiscordCommand;
import com.gmail.berndivader.streamserver.discord.DiscordBot;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;
import com.gmail.berndivader.streamserver.mysql.DatabaseConnection;
import com.gmail.berndivader.streamserver.term.ANSI;

public class Commands {
	
	public static Commands instance;
	private final static String PACKAGE_NAME="com/gmail/berndivader/streamserver/discord/command/commands";
	private static String fileName;
	public HashMap<String,Class<Command<?>>>commands;

	static {
		try {
			fileName=URLDecoder.decode(
					StreamServer.class.getProtectionDomain().getCodeSource().getLocation().getPath(),
					StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			try {
				fileName=URLDecoder.decode(
						StreamServer.class.getProtectionDomain().getCodeSource().getLocation().getPath(),
						StandardCharsets.ISO_8859_1.toString());
			} catch (UnsupportedEncodingException e1) {
				ANSI.printErr("Error, there is no UTF-8 nor a ISO-8859 encoding avaible.",e1);
			}
		}
	}
	
	public Commands() {
		instance=this;
		commands=new HashMap<>();
		try {
			loadCommandClasses();
		} catch (ClassNotFoundException | IOException e) {
			ANSI.printErr("Failed to instantiate console commands.",e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void loadCommandClasses() throws IOException, ClassNotFoundException {
		try(JarInputStream jarStream=new JarInputStream(new FileInputStream(fileName))) {
			JarEntry entry;
			while(jarStream.available()==1) {
				entry=jarStream.getNextJarEntry();
				if(entry!=null) {
					String clazzName=entry.getName();
					if(clazzName.endsWith(".class")&&clazzName.startsWith(PACKAGE_NAME)) {
						clazzName=clazzName.substring(0,clazzName.length()-6).replace("/",".");
						Class<?>clazz=Class.forName(clazzName);
						DiscordCommand anno=clazz.getAnnotation(DiscordCommand.class);
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
										add&=DatabaseConnection.INIT;
										break;
									default:
										add=true;
										break;
								}
							}
							if(add) commands.put(anno.name(),(Class<Command<?>>)clazz);
						}
					}
				}
			}
		}
	}
	
	public Command<?> newCommandInstance(String name) {
		if(name.isEmpty()) name="help";
		Class<Command<?>>command=commands.get(name);
		if(command==null) return null;
		try {
			return command.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			ANSI.printErr("Error while collect discord commands.",e);
			return null;
		}
	}
		
}
