package com.gmail.berndivader.streamserver.youtube;

import java.util.concurrent.Future;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.term.ANSI;
import com.gmail.berndivader.streamserver.youtube.packets.EmptyPacket;
import com.gmail.berndivader.streamserver.youtube.packets.ErrorPacket;
import com.gmail.berndivader.streamserver.youtube.packets.LiveStreamPacket;
import com.gmail.berndivader.streamserver.youtube.packets.Packet;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public final class Youtube {
	
	private Youtube() {};
	
	public final static CloseableHttpClient HTTP_CLIENT;	
	private static final String URL="https://youtube.googleapis.com/youtube/v3/";
	
	static {
		HTTP_CLIENT=HttpClients.createMinimal();
	}
	
	public static Future<Packet> livestreamsByChannelId(String id) {
		
		String query=URL.concat("search?part=snippet&eventType=live&maxResults=1&type=video&prettyPrint=true&channelId=").concat(id).concat("&key=").concat(Config.YOUTUBE_KEY);
		
		return Helper.EXECUTOR.submit(new Response<Packet>(query) {
			
			@Override
			protected Packet handle(JsonObject json) {
				LiveStreamPacket packet=new LiveStreamPacket();
				JsonArray array=json.getAsJsonArray("items");
				if(array.size()>0) {
					packet=Helper.GSON.fromJson(array.get(0).getAsJsonObject(),LiveStreamPacket.class);
					packet.source=json;
				} else {
					return new EmptyPacket();
				}
				return packet;
			}

			@Override
			protected Packet handleErr(JsonObject json) {
				ANSI.println(json.toString());
				ErrorPacket error=Helper.GSON.fromJson(json,ErrorPacket.class);
				return error;
			}
			
		});
		
	}

}

