package com.gmail.berndivader.streamserver.config;

public class WebSocket {
	public boolean USE;
	public Integer PORT;
	public String HOST;
	public String[] ORIGINS;
	
	public WebSocket() {
		USE=false;
		PORT=3232;
		HOST="localhost";
		ORIGINS=new String[0];
	}
}
