package com.gmail.berndivader.streamserver.youtube.packets;

import java.util.List;

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
	public void printSimple() {
		System.err.println("ERROR: "+code+" - "+message);
	}
	
	@Override
	public void printDetails() {
		printSimple();
		errors.forEach(e->{
			System.err.println(e.reason+" - "+e.domain+" - "+e.message);
		});
	}
	
}
