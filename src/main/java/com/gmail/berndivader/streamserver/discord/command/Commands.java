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
import com.gmail.berndivader.streamserver.console.ConsoleRunner;

public class Commands {

	private final static String PACKAGE_NAME="com/gmail/berndivader/streamserver/discord/command/commands";
	private static String fileName;
	private HashMap<String,Class<Command<?>>>commands;

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
				e1.printStackTrace();
			}
		}
	}
	
	public Commands() {
		commands=new HashMap<>();
		loadCommandClasses();
	}
	
	@SuppressWarnings("unchecked")
	private void loadCommandClasses() {
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
							commands.put(anno.name(),(Class<Command<?>>)clazz);
						}
					}
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public Command<?> getCommand(String name) {
		if(name.isEmpty()) name="help";
		Class<Command<?>>command=commands.get(name);
		if(command==null) return null;
		try {
			return command.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			ConsoleRunner.println(e.getMessage());
			return null;
		}
	}
	
}
