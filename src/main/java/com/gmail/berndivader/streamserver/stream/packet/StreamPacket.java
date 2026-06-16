package com.gmail.berndivader.streamserver.stream.packet;

import java.time.Instant;

public class StreamPacket extends Packet {
	
	public String id;
	public String remote;
	public String query;
	public Instant lastSeen;
	public Instant startedAt;

	@Override
	public void validate() {
		if(id==null) id="";
		if(remote==null) remote="";
		if(query==null) query="";
		if(lastSeen==null) lastSeen=Instant.EPOCH;
		if(startedAt==null) startedAt=Instant.EPOCH;
	}
	
	public static void parseQuery(String query) {
		
	}

}
