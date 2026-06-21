package com.gmail.berndivader.streamserver.stream;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.stream.packet.StreamPacket;
import com.google.gson.JsonObject;

public class Live {
	
	public static Server server;
	public static Clients clients;
	
	private static final ConcurrentHashMap<String,StreamPacket> candits=new ConcurrentHashMap<String,StreamPacket>();
	private static final ConcurrentHashMap<String,StreamPacket> actives=new ConcurrentHashMap<String,StreamPacket>();
	private static final ConcurrentHashMap<String,StreamPacket> watchers=new ConcurrentHashMap<String,StreamPacket>();
	
	public static long STREAM_TIMEOUT_SECONDS=90l;
	
	public static void start() throws IOException {
		server=Server.build();
		server.start();
		clients=Clients.build();
	}
	
	public static boolean registerWatcher(StreamPacket watcher) {
		if(watcher!=null) {
			watcher.startedAt=Instant.now();
			StreamPacket dub=watchers.putIfAbsent(watcher.id,watcher);
			if(dub!=null&&dub.startedAt.isAfter(Instant.now().plusSeconds(Integer.parseInt(Config.LIVESTREAM.WATCHER_TIMEOUT)))) {
				watchers.remove(dub.id);
			}
			return watchers.putIfAbsent(watcher.id,watcher)==null;
		}
		return false;
	}
	
	public static void activateWatcher(String id) {
		StreamPacket watcher=watchers.get(id);
		if(watcher!=null) watcher.online=true;
	}
	
	public static void removeWatcher(String id) {
		watchers.remove(id);
	}
	
	public static boolean registerStreamer(StreamPacket candit) {
		if(candit!=null) {
			candit.startedAt=Instant.now();
			StreamPacket dub=candits.putIfAbsent(candit.token,candit);
			if(dub!=null&&dub.startedAt.isAfter(Instant.now().plusSeconds(Integer.parseInt(Config.LIVESTREAM.LIVE_TIMEOUT)))) {
				candits.remove(dub.token);
			}
			return candits.putIfAbsent(candit.token,candit)==null;
		}
		return false;
	}
	
	public static boolean activateStreamer(String token) {
		StreamPacket candit=candits.remove(token);
		if(candit!=null) {
			candit.startedAt=Instant.now();
			candit.lastSeen=Instant.now();
			candit.online=true;
			StreamPacket active=actives.putIfAbsent(token,candit);
			if(active!=null&&active.lastSeen.isAfter(Instant.now().plusSeconds(STREAM_TIMEOUT_SECONDS))) {
				actives.remove(active.token);
			}
			return actives.putIfAbsent(token,candit)==null;
		}
		return false;
	}
	
	public static void removeStreamer(String token) {
		actives.remove(token);
	}
	
	public static boolean isLive(String token) {
		return actives.containsKey(token);
	}
	
	public static boolean isServer() {
		return server!=null;
	}
	
	public static boolean isClient() {
		return clients!=null;
	}
	
	public static void stop() {
		if(server!=null) server.stop();
		if(clients!=null) clients.stop();
	}
	
	public static String query2jsonString(String query) {
		
		if(query==null||query.isEmpty()) return "{}";
		
		JsonObject json=new JsonObject();
		String[]pairs=query.split("\\?");
		
		for(String pair:pairs) {
			int i=pair.indexOf("=");
			if(i>=0) {
				String k=pair.substring(0,i);
				String v=pair.substring(i+1);
				json.addProperty(k,v);
			} else {
				json.addProperty(pair,"");
			}
		}
		
		return Helper.GSON.toJson(json);
	}
	
	
}
