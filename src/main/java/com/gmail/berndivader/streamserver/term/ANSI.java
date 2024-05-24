package com.gmail.berndivader.streamserver.term;

import java.io.PrintStream;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public enum ANSI {

	BR("\012"),
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
			if(contains(token)) {
				string=string.replace("[".concat(matcher.group(1).concat("]")),ANSI.valueOf(token).str());
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
	
	public static void printAnsi(String string) {
		console.print(parse(string));
	}
	
	public static void printErr(String string) {
		console.printf("\033[1;91m%s\033[0m\n>\033[34m",parse(string));
	}
	
	public static void println(String string) {
		console.printf("\033[0m%s\n>\033[34m",parse(string));
	}
	
	public static void print(String string) {
		console.printf("\033[0m%s",parse(string));
	}
	
	public static void printReady() {
		console.printf("\033[0m\n%s",">\033[34m");
	}	
	
}

