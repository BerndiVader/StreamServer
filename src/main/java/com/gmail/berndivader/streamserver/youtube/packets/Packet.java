package com.gmail.berndivader.streamserver.youtube.packets;

import com.gmail.berndivader.streamserver.Helper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public abstract class Packet {
	
	public class Thumbnail {
		public String url;
		public int width;
		public int height;
	}	
	
	protected JsonObject source;
	protected Packet() {}
	
	public static String stringFromPath(JsonElement json,String path) {
		String[]names=path.split("\\.");
		JsonElement current=json;
		for(String name:names) {
			if(current.getAsJsonObject().has(name)) {
	            current=current.getAsJsonObject().get(name);
			} else {
				return "";
			}
		}
		return current!=null?current.getAsString():"";
	}
	
	public static JsonElement elementFromPath(JsonElement json,String path) {
		String[]names=path.split("\\.");
		JsonElement current=json;
		for(String name:names) {
			if(current.isJsonObject()&&current.getAsJsonObject().has(name)) {
	            current=current.getAsJsonObject().get(name);
			} else {
				return new JsonObject();
			}
		}
		return current!=null?current:new JsonObject();
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
