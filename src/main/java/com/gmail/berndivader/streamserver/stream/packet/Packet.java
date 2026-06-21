package com.gmail.berndivader.streamserver.stream.packet;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.term.ANSI;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public abstract class Packet {
	
	public JsonObject source;
	
	protected Packet() {}
	
	public abstract void validate();
	
	public String print() {
		return Helper.GSON.toJson(this);
	}
	
	public static <T extends Packet>T build(String source,Class<T> clazz) {
		T packet=null;
		try {
			JsonObject json=JsonParser.parseString(source).getAsJsonObject();
			packet=Helper.GSON.fromJson(source,clazz);
			packet.source=json;
			packet.validate();
		} catch(Exception e) {
			ANSI.error(e.getMessage(),e);
		}
		return packet;
	}
	
}
