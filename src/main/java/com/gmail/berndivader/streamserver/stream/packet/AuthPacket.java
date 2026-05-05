package com.gmail.berndivader.streamserver.stream.packet;

public class AuthPacket extends Packet {
	
	public String ip;
	public String user;
	public String password;
	public String token;
	public String action;
	public String path;
	public String protocol;
	public String id;
	public String query;
	
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
	}
	
}
