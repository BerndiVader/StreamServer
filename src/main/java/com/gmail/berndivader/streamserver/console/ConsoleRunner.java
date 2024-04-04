package com.gmail.berndivader.streamserver.console;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.console.command.Commands;

public class ConsoleRunner {
	
    static Scanner keyboard;
    static PrintStream console;
    
    public static boolean forceExit,exit;
    public static ConsoleRunner instance;
    
    static {
    	keyboard=new Scanner(System.in);
    	console=System.out;
    }
	
	public ConsoleRunner() {
		
		instance=this;
		exit=false;
		try {
			new Commands();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		
		while (!exit) {
	        printReady();
            String input=keyboard.nextLine();
            
            if(input!=null&&input.startsWith(".")) {
            	
            	String[]parse=input.split(" ",2);
            	if(parse.length==1) {
            		parse=new String[] {parse[0],""};
            	}
            	
            	String command=parse[0].toLowerCase();
            	String[]args=new String[] {parse[1]};
            	
            	Command cmd=Commands.instance.getCommand(command.substring(1));
            	if(cmd!=null) cmd.execute(args);
            }
        }
        keyboard.close();
	}
	
	public static void printErr(String string) {
		console.printf("\033[0;1m%s\n>",string);
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
