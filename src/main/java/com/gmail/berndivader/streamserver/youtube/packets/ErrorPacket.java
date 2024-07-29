package com.gmail.berndivader.streamserver.youtube.packets;

import java.util.List;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.term.ANSI;

public class ErrorPacket extends Packet {
	
	public class Error {
		public String message;
		public String domain;
		public String reason;
	}
	
	public int code;
	public String message;
	public List<Error>errors;
	public String status;
	
	@Override
	public String toString() {
		return Helper.GSON.toJson(this);
	}
	
	public void printSimple() {
		ANSI.printWarn("YT Request Error:"+code+" - "+message);
		if(Config.DEBUG) ANSI.println(source.toString());
	}
	
	public void printDetails() {
		printSimple();
		errors.forEach(e->ANSI.printWarn(e.reason+" - "+e.domain+" - "+e.message));
	}	
		
}
