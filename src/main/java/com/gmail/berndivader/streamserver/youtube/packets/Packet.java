package com.gmail.berndivader.streamserver.youtube.packets;

import com.gmail.berndivader.streamserver.Helper;
import com.google.gson.JsonObject;

public abstract class Packet {
	
	protected JsonObject source;
	protected Packet() {}
	
	public JsonObject getByPath(String name) {
		if(source.has(name)) {
			return source.getAsJsonObject(name);
		} 
		return new JsonObject();
	}
	
	@Override
	public String toString() {
		return source.toString();
	}
	
	public JsonObject source() {
		return this.source;
	}
	
	public static Packet build(JsonObject source,Class<? extends Packet> clazz) {
		Packet packet=Helper.GSON.fromJson(source,clazz);
		packet.source=source;
		return packet;
	}
		
	
}
