package com.gmail.berndivader.streamserver.stream.packet;

import java.time.Instant;

public class StatusPacket extends Packet {
	
	public enum STATUS {
		ONLINE,
		OFFLINE,
		READ,
		UNREAD,
		UNKOWN
	}
	
	public String path;
	public String query;
	public String type;
	public String id;
	public String port;
	public String token;
	
	public transient STATUS status;
	public transient Instant lastSeen;
	public transient Instant startedAt;

	@Override
	public void validate() {
		if(id==null) id="";
		if(query==null) query="";
		if(type==null) type="";
		if(path==null) path="";
		if(port==null) port="";
				
		if(lastSeen==null) lastSeen=Instant.EPOCH;
		if(startedAt==null) startedAt=Instant.EPOCH;
		
		if(status==null) status=STATUS.UNKOWN;
		if(path.startsWith("live")) {
			token=path.replace("live/","");
		} else {
			token="";
		}
	}
	
}
