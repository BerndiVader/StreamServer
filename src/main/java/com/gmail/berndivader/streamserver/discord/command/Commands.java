package com.gmail.berndivader.streamserver.discord.command;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import com.gmail.berndivader.streamserver.StreamServer;
import com.gmail.berndivader.streamserver.annotation.DiscordCommand;
import com.gmail.berndivader.streamserver.discord.DiscordBot;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;
import com.gmail.berndivader.streamserver.mysql.DatabaseConnection;
import com.gmail.berndivader.streamserver.mysql.DatabaseConnection.STATUS;
import com.gmail.berndivader.streamserver.term.ANSI;

public final class Commands {
	
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
				ANSI.error("Error, there is no UTF-8 nor a ISO-8859 encoding avaible.",e1);
			}
		}
	}
	
	public Commands() {
		commands=new HashMap<>();
		try {
			loadCommandClasses();
		} catch (Exception e) {
			ANSI.error("Failed to instantiate console commands.",e);
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
										add&=DatabaseConnection.status==STATUS.OK;
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
	
	public Optional<Command<?>> build(String name) {
		if(name.isEmpty()) name="help";
		Class<Command<?>>command=commands.get(name);
		if(command!=null) {
			try {
				return Optional.of(command.getDeclaredConstructor().newInstance());
			} catch (Exception e) {
				ANSI.error("Error while collect discord commands.",e);
			}
		}
		return Optional.empty();
	}
		
}
