package com.gmail.berndivader.streamserver.console.command;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import com.gmail.berndivader.streamserver.StreamServer;
import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.console.ConsoleRunner;

public class Commands {
	
	public static Commands instance;
	
	final static String PACKAGE_NAME="com/gmail/berndivader/streamserver/console/command/commands";
	static String fileName;
	public HashMap<String,Class<Command>>commands;
	
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
		instance=this;
		commands=new HashMap<>();
		loadCommandClasses();
		
	}
	
	@SuppressWarnings("unchecked")
	void loadCommandClasses() throws FileNotFoundException, IOException, ClassNotFoundException {
		
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
							commands.put(anno.name(),(Class<Command>)clazz);
						}
					}
				}
			}
		}
		
	}
	
	public Command getCommand(String name) {
		Class<Command>clazz=commands.get(name);
		if(clazz==null) return null;
		
		try {
			return clazz.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			ConsoleRunner.println(e.getMessage());
			return null;
		}
		
	}
	
}
