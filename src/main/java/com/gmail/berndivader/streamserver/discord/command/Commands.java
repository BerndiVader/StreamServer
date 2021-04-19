package com.gmail.berndivader.streamserver.discord.command;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import com.gmail.berndivader.streamserver.ConsoleRunner;
import com.gmail.berndivader.streamserver.StreamServer;
import com.gmail.berndivader.streamserver.annotation.DiscordCommand;

public class Commands {
	
	final static String PACKAGE_NAME="com.gmail.berndivader.streamserver.command.commands";
	static String fileName;
	HashMap<String,String>commands;
	
	static {
		try {
			fileName = URLDecoder.decode(
					StreamServer.class.getProtectionDomain().getCodeSource().getLocation().getPath(),
					StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			try {
				fileName = URLDecoder.decode(
						StreamServer.class.getProtectionDomain().getCodeSource().getLocation().getPath(),
						StandardCharsets.ISO_8859_1.toString());
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	public Commands() throws FileNotFoundException, IOException, ClassNotFoundException {
		
		commands=new HashMap<>();
		loadClasses();
		
	}
	
	void loadClasses() throws FileNotFoundException, IOException, ClassNotFoundException {
		
		try(JarInputStream jarStream=new JarInputStream(new FileInputStream(fileName))) {
			JarEntry entry;
			while(jarStream.available()==1) {
				entry=jarStream.getNextJarEntry();
				if(entry!=null) {
					String clazzName=entry.getName();
					if(clazzName.endsWith(".class")&&clazzName.startsWith("com/gmail/berndivader/streamserver/discord/command/commands")) {
						clazzName=clazzName.substring(0,clazzName.length()-6).replace("/",".");
						Class<?>clazz=Class.forName(clazzName);
						DiscordCommand anno=clazz.getAnnotation(DiscordCommand.class);
						if(anno!=null) {
							commands.put(anno.name(),clazzName);
						}
					}
				}
			}
		}
		
	}
	
	public Command<?> getCommand(String name) {
		String className=commands.get(name);
		if(className!=null) {
			try {
				return (Command<?>) Class.forName(className).newInstance();
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				ConsoleRunner.println(e.getMessage());
			}
		}
		
		return null;
	}
	
}
