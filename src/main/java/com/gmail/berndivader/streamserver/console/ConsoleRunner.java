package com.gmail.berndivader.streamserver.console;

import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.console.command.Commands;
import com.gmail.berndivader.streamserver.term.ANSI;


public class ConsoleRunner {
	
    public static boolean forceExit,exit;
    public static ConsoleRunner instance;
    	
	public ConsoleRunner() {
		
		Commands.instance=new Commands();
		exit=false;
		
		while (!exit) {
	        ANSI.prompt();
            String input=ANSI.keyboard.nextLine();
            
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
        ANSI.keyboard.close();		
	}
}
