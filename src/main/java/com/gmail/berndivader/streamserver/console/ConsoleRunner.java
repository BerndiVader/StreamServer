package com.gmail.berndivader.streamserver.console;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.console.command.Commands;
import com.gmail.berndivader.streamserver.discord.DiscordBot;

public class ConsoleRunner {
	
    static Scanner keyboard;
    static PrintStream console;
    static Commands commands;
    
    public static boolean forceExit,exit;
    
    public static ConsoleRunner instance;
    
    static {
    	try {
			commands=new Commands();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
    	keyboard=new Scanner(System.in);
    	console=System.out;
    }
	
	public ConsoleRunner() {
		
		instance=this;
		exit=false;
		
		while (!exit) {
	        printReady();
            String input = keyboard.nextLine();
            
            if(input!=null&&input.startsWith(".")) {
            	
            	String[]parse=input.toLowerCase().split(" ",2);
            	if(parse.length==1) {
            		parse=new String[] {parse[0],""};
            	}
            	
            	String command=parse[0];
            	String[]args=new String[] {parse[1]};
            	
            	Command cmd=commands.getCommand(command.substring(1));
            	if(cmd!=null) cmd.execute(args);
            	
            	switch(command) {
	            	case ".discord":
	            		executeDiscordCommand(new String[] {parse[1]});
	            		break;
	            	default:
	            		break;
            	}
            }
        }
        keyboard.close();
	}
	
	void executeDiscordCommand(String[] args) {
		switch(args[0]) {
			case "voice connect":
			case "voiceconnect":
			case "vconnect":
				DiscordBot.instance.connectStream();
				break;
		}
	}
	
	public static void println(String string) {
		console.printf("%s\n>",string);
	}
	
	public static void print(String string) {
		console.printf("%s",string);
	}
	
	public static void printReady() {
		console.printf("\n%s",">");
	}

}
