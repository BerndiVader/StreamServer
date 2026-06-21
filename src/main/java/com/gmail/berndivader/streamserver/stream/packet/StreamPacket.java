package com.gmail.berndivader.streamserver.stream.packet;

import java.time.Instant;

public class StreamPacket extends Packet {
	
	public String ip;
	public String user;
	public String password;
	public String token;
	public String action;
	public String path;
	public String protocol;
	public String id;
	public String query;

	public transient Boolean online;
	public transient Instant startedAt;
	public transient Instant lastSeen;
	
	@Override
	public void validate() {
		if(ip==null) ip="";
		if(user==null) user="";
		if(password==null) password="";
		if(token==null) token="";
		if(action==null) action="";
		if(path==null) path="";
		if(protocol==null) protocol="";
		if(id==null) id="";
		if(query==null) query="";
		
		if(path.startsWith("live")) {
			token=path.replace("live/","");
		} else {
			token="";
		}
		
		online=false;
		
	}
	
}
