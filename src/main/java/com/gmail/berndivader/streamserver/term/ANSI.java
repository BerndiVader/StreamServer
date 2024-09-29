package com.gmail.berndivader.streamserver.term;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.Date;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.gmail.berndivader.streamserver.config.Config;

public enum ANSI {

	STORE("\033[?47h"),
	RESTORE("\033[?47l"),
	CLR("\033[H\033[0J"),
	BR("\012"),
	CR("\015"),
	DL("\033[2K"),
	BELL("\007"),
	BOLD("\033[1m"),
	BOLDOFF("\033[22m"),
	BLINK("\033[5m"),
	BLINKOFF("\033[25m"),
	
	BLACK("\033[30m"),
	BLACKOFF("\033[39m"),
	RED("\033[31m"),
	REDOFF("\033[39m"),
	GREEN("\033[32m"),
	GREENOFF("\033[39m"),
	YELLOW("\033[33m"),
	YELLOWOFF("\033[39m"),
	BLUE("\033[34m"),
	BLUEOFF("\033[39m"),
	MAGENTA("\033[35m"),
	MAGENTAOFF("\033[39m"),
	CYAN("\033[36m"),
	CYANOFF("\033[39m"),
	WHITE("\033[37m"),
	WHITEOFF("\033[39m"),
	SYSTEM("\033[39m"),
		
	PROMPT("\033[0m\012>"),
	ERROR("\033[0m\007\033[1m\033[31m[ERROR]"),
	WARNING("\033[1m\033[33m[WARNING]"),
	
	RESET("\033[0m");
	
	
    public static Scanner keyboard;
    public static PrintStream console;
	
	private static Pattern pattern;
	private final String code;
	
	static {
		pattern=Pattern.compile("\\[(.*?)\\]");
    	keyboard=new Scanner(System.in);
    	console=System.out;	}
		
	ANSI(String code) {
		this.code=code;
	}

	@Override
	public String toString() {
		return code;
	}
	public String str() {
		return code;
	}
	
	public static String parse(String string) {
		Matcher matcher=pattern.matcher(string);
		
		while(matcher.find()) {
			String token=matcher.group(1);
			if(token.charAt(0)=='/') token=token.substring(1).concat("OFF");
			try {
				if(contains(token)) {
					string=string.replace("[".concat(matcher.group(1).concat("]")),ANSI.valueOf(token).str());
				}
			} catch(Exception e) {
				//
			}
		}
		return string;
	}
	
	private static boolean contains(String name) {
		String search=name.toUpperCase();
		return Stream.of(ANSI.values()).anyMatch(value->{
			return value.name().equals(search);
		});
	}
	
	public static String merge(ANSI...ansis) {
		StringBuilder out=new StringBuilder();
		Stream.of(ansis).forEach(ansi->{
			out.append(ansi.str());
		});
		return out.toString();
	}
	
	public static void raw(String string) {
		console.print(parse(string));
	}
	
	public static void info(String string) {
		if(!string.isBlank()) {
			console.printf("\033[36m[INFO]%s",parse(string));
		} else {
			console.print(parse(string));
		}
		console.print("\n");
		if(Config.DEBUG) log(string,null);
	}
	
	public static void warn(String string) {
		if(!string.isBlank()) {
			console.printf("%s%s",ANSI.WARNING.str(),parse(string));
		} else {
			console.print(parse(string));
		}
		console.print("\n");
		
		if(Config.DEBUG) log(string,null);
	}
	
	public static void error(String string, Throwable error) {
		if(error==null) error=new Throwable("Unknown");
		console.printf("%s%s%s",ANSI.ERROR.str(),parse(string),ANSI.BOLDOFF.str());
		if(Config.DEBUG) {
			console.println();
			error.printStackTrace();
			log(string,error);
		}
		console.print("\n");
	}
		
	public static void println(String string) {
		console.printf("%s%s%s",ANSI.RESET.str(),parse(string),ANSI.BR.str());
	}
	
	public static void print(String string) {
		console.printf("%s%s",ANSI.RESET.str(),parse(string));
	}
	
	public static void prompt() {
		console.print(ANSI.PROMPT.str());
	}
	
	private static void log(String string,Throwable throwable) {
		File file=new File(Config.config_dir,"debug.log");
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(file.exists()) {
			try(FileWriter writer=new FileWriter(file,true)) {
				writer.append(Date.from(Instant.now())+": "+string+"\n");
				if(throwable!=null) {
					writer.append(throwable.getMessage()+"\n");
					throwable.printStackTrace(new PrintWriter(writer));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}

