package com.gmail.berndivader.streamserver.youtube.packets;

import com.google.gson.JsonObject;

public abstract class Packet {
	
	public JsonObject source;
	
	public abstract void printSimple();
	public abstract void printDetails();
	
	public JsonObject getByPath(String name) {
		if(source.has(name)) {
			return source.getAsJsonObject(name);
		} 
		return new JsonObject();
	}
	
}
