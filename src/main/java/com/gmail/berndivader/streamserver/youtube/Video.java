package com.gmail.berndivader.streamserver.youtube;

import java.util.concurrent.Future;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.term.ANSI;
import com.gmail.berndivader.streamserver.youtube.packets.ErrorPacket;
import com.gmail.berndivader.streamserver.youtube.packets.Packet;
import com.gmail.berndivader.streamserver.youtube.packets.UnknownPacket;
import com.gmail.berndivader.streamserver.youtube.packets.VideoSnippetPacket;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class Video {
	
	private Video() {}
	
	public static Future<Packet> getVideoById(String id) {
		return Helper.EXECUTOR.submit(()->{
			if(OAuth2.isExpired()&&!OAuth2.refresh()) return ErrorPacket.buildError("Token expired.","Access token is expired and refresh failed.","CUSTOM");

			String url=Youtube.URL.concat(String.format("videos?id=%s&part=snippet&key=%s",id,Config.BROADCASTER.YOUTUBE_API_KEY));
			HttpGet get=new HttpGet(url);
			get.setHeader("Authorization","Bearer ".concat(Config.BROADCASTER.YOUTUBE_ACCESS_TOKEN));
			get.addHeader("Accept","application/json");

			try {
				return Youtube.HTTP_CLIENT.execute(get,response->{
					JsonObject json = JsonParser.parseString(EntityUtils.toString(response.getEntity())).getAsJsonObject();
					if(json==null||json.isJsonNull()) {
						return ErrorPacket.buildError("Request to get video resource by id failed.","Nulljson was returned.","CUSTOM");
					} else if(json.has("error")) {
						return Packet.build(json,ErrorPacket.class);
					} else if(json.has("kind")&&json.get("kind").getAsString().equals("youtube#videoListResponse")) {
						JsonArray array=json.getAsJsonArray("items");
						if(array!=null&&!array.isJsonNull()&&array.size()>0) {
							return Packet.build(array.get(0).getAsJsonObject(),VideoSnippetPacket.class);
						}
						return Packet.emtpy();
					} else {
						return Packet.build(json,UnknownPacket.class);
					}
				});
			} catch(Exception e) {
				if (Config.DEBUG) ANSI.error(e.getMessage(),e);
				return ErrorPacket.buildError("Request to get video resource by id failed.",e.getMessage(),"CUSTOM");
			}
		});
	}
	
}
