package com.gmail.berndivader.streamserver.youtube.packets;

import java.util.List;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.term.ANSI;

public class ErrorPacket extends Packet {

	private static final String JSON_ERROR="{\"code\":-1,\"message\":\"%s\",\"errors\":[{\"message\":\"%s\",\"domain\":\"global\",\"%s\":\"badRequest\"}],\"status\":\"%s\"}";

	public class Error {
		public String message;
		public String domain;
		public String reason;
		public String locationType;
		public String location;
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
	
	public static ErrorPacket buildError(String message,String reason,String status) {
		return Helper.GSON.fromJson(String.format(JSON_ERROR,message,message,reason,status),ErrorPacket.class);
	}	
		
}
