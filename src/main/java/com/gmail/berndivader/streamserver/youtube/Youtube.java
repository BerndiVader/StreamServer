package com.gmail.berndivader.streamserver.youtube;

import com.gmail.berndivader.streamserver.Helper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Youtube {
	
	private static String url="https://www.googleapis.com/youtube/v3/";
	
	
	public static void livestreamsByChannelId(String id) {
		
		String query=url.concat("search?part=snippet&channelId=").concat(id).concat("&type=video&eventType=live&key=").concat("AIzaSyC73-UO5JfFHzPoTPL8iNlRjDURNvtA4es");
		
		Helper.executor.submit(new GetHttpCallable(query) {
			
			@Override
			protected boolean handle(JsonObject json) {
				if(json.get("items").isJsonArray()) {
					JsonArray array=json.get("items").getAsJsonArray();
					int size=array.size();
					for(int i=0;i<size;i++) {
						JsonObject object=array.get(i).getAsJsonObject();
						String videoId=object.getAsJsonObject("id").getAsJsonObject("videoId").getAsString();
						JsonObject snippet=object.getAsJsonObject("snipped");
						String startDate=snippet.getAsJsonObject("publishedAt").getAsString();
						
					}
				}
				return true;
			}
		});
		
	}
		
}
