package com.gmail.berndivader.streamserver.youtube;

import java.util.concurrent.Future;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.youtube.packets.ErrorPacket;
import com.gmail.berndivader.streamserver.youtube.packets.LiveStreamPacket;
import com.gmail.berndivader.streamserver.youtube.packets.Packet;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Youtube {
	
	private static final String URL="https://youtube.googleapis.com/youtube/v3/";
	
	public static Future<Packet> livestreamsByChannelId(String id) {
		
		String query=URL.concat("search?part=snippet&eventType=live&maxResults=1&type=video&prettyPrint=true&channelId=").concat(id).concat("&key=").concat(Config.YOUTUBE_KEY);
		System.out.println(query);
		
		return Helper.executor.submit(new Response<Packet>(query) {
			
			@Override
			protected Packet handle(JsonObject json) {
				LiveStreamPacket packet=new Gson().fromJson(json,LiveStreamPacket.class);
				packet.source=json;
				return packet;
			}

			@Override
			protected Packet handleErr(JsonObject json) {
				ErrorPacket error=new Gson().fromJson(json,ErrorPacket.class);
				error.printSimple();
				return error;
			}
			
		});
		
	}

}

