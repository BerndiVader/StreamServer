package com.gmail.berndivader.streamserver.youtube;

import java.util.concurrent.Future;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.config.Config;
import com.google.gson.JsonObject;

public class Youtube {
	
	private static final String URL="https://youtube.googleapis.com/youtube/v3/";
	
	public static Future<JsonObject> livestreamsByChannelId(String id) {
		
		String query=URL.concat("search?part=snippet&eventType=live&maxResults=1&type=video&prettyPrint=true&channelId=").concat(id).concat("&key=").concat(Config.YOUTUBE_KEY);
		System.err.println(query);
		
		return Helper.executor.submit(new GetHttpCallable<JsonObject>(query) {
			
			@Override
			protected JsonObject handle(JsonObject json) {
				System.err.println(json.toString());
				return json;
			}

			@Override
			protected JsonObject handleErr(JsonObject json) {
				System.err.println(json.toString());
				return json;
			}
			
		});
		
	}

}

/*
 * liveBroadcasts?broadcastStatus=active&id=YOUR_BROADCAST_ID&key=[YOUR_API_KEY]
 *  
 * search?part=snippet&eventType=live&maxResults=1&q=news&type=video&prettyPrint=true&key=[YOUR_API_KEY]
 * search?part=snippet&channelId=ID&eventType=live&maxResults=1&type=video&prettyPrint=true&key=[YOUR_API_KEY]
 * 
 * 
*/
