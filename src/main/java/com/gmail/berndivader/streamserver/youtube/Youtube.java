package com.gmail.berndivader.streamserver.youtube;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;
import com.gmail.berndivader.streamserver.mysql.VerifyOAuth2;
import com.gmail.berndivader.streamserver.term.ANSI;
import com.gmail.berndivader.streamserver.youtube.packets.EmptyPacket;
import com.gmail.berndivader.streamserver.youtube.packets.ErrorPacket;
import com.gmail.berndivader.streamserver.youtube.packets.LiveStreamPacket;
import com.gmail.berndivader.streamserver.youtube.packets.Packet;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class Youtube {
	public static final CloseableHttpClient HTTP_CLIENT=HttpClients.createSystem();
	private static final String URL="https://youtube.googleapis.com/youtube/v3/";
	private static final String OAUTH_URL="https://accounts.google.com/o/oauth2/v2/auth?client_id=%s&redirect_uri=%s&response_type=code&scope=https://www.googleapis.com/auth/youtube&state=%s&access_type=offline&prompt=consent";
	
	private Youtube() {}

	public static boolean OAuth2Flow() {
		String state=UUID.randomUUID().toString();
		ANSI.println("Visit the URL and authorize the bot:[BR][GREEN]".concat(String.format(OAUTH_URL,
				Config.YOUTUBE_CLIENT_ID,
				Config.YOUTUBE_AUTH_REDIRECT,
				state
			)));
		ANSI.print("[YELLOW]Enter the retrieved code: [CYAN]");
		try {
            String code=ANSI.keyboard.nextLine();
            ANSI.print("[RESET][YELLOW]Try to authorisize your request...");
            
            VerifyOAuth2 verify=new VerifyOAuth2(code,state);
            try {
				if(!verify.future.get(10l,TimeUnit.SECONDS)) throw(new Exception("Failed to verify OAuth2 request."));
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				throw(new Exception("Failed to verify OAuth2 request.",e));
			}
            
			HttpPost post=new HttpPost("https://oauth2.googleapis.com/token");
			StringEntity parameter=new StringEntity(String.format("code=%s&client_id=%s&client_secret=%s&redirect_uri=%s&grant_type=authorization_code",
					code,
					Config.YOUTUBE_CLIENT_ID,
					Config.YOUTUBE_CLIENT_SECRET,
					Config.YOUTUBE_AUTH_REDIRECT
				));
			post.setEntity(parameter);
			post.setHeader("Content-Type","application/x-www-form-urlencoded");
			
			Entry<String,String>pair=HTTP_CLIENT.execute(post,response->{
				String accessToken="";
				String refreshToken="";
				JsonObject json=JsonParser.parseString(EntityUtils.toString(response.getEntity())).getAsJsonObject();
				ANSI.println(json.toString());
				if(json.has("refresh_token")) refreshToken=json.get("refresh_token").getAsString();
				if(json.has("access_token")) accessToken=json.get("access_token").getAsString();
				if(json.has("error")) {
					ANSI.println("[RED]failed!");
					ANSI.printErr(json.get("error").getAsString()+" Reason: "+json.get("error_description").getAsString(),new Throwable("OAuth2 registration failed."));
				}
				return new AbstractMap.SimpleEntry<String,String>(accessToken,refreshToken);
			});
			
			String token=pair.getKey();
			String refreshToken=pair.getValue();
        	
        	if(token.isEmpty()) throw(new Exception("Failed to receive token."));
        	if(refreshToken.isEmpty()) throw(new Exception("Failed to receive refresh token."));
        	ANSI.println("[GREEN]done!");
        	Config.YOUTUBE_ACCESS_TOKEN=token;
        	Config.YOUTUBE_REFRESH_TOKEN=refreshToken;
        	Config.YOUTUBE_TOKEN_TIMESTAMP=System.currentTimeMillis()/1000;
        	Config.saveConfig();
        	
		} catch (Exception e) {
			ANSI.printErr(e.getMessage(),e);
			return false;
		}
		return true;
	}
	
	private static boolean isExpired() {
		return System.currentTimeMillis()/1000l-Config.YOUTUBE_TOKEN_TIMESTAMP>3599l;
	}
	
	public static boolean refreshToken() {
		ANSI.print("[WHITE]Try to refresh access token...");
		HttpPost post=new HttpPost("https://oauth2.googleapis.com/token");
	    try {
	    	StringEntity parameter=new StringEntity(String.format("grant_type=refresh_token&client_id=%s&client_secret=%s&refresh_token=%s",
    			Config.YOUTUBE_CLIENT_ID,
    			Config.YOUTUBE_CLIENT_SECRET,
    			Config.YOUTUBE_REFRESH_TOKEN
		    ));
	    	
	    	post.setEntity(parameter);
	    	post.setHeader("Content-Type","application/x-www-form-urlencoded");
	    	
	    	String token=HTTP_CLIENT.execute(post,response->{
	    		
	    		JsonObject json=JsonParser.parseString(EntityUtils.toString(response.getEntity())).getAsJsonObject();
	    		if(json.has("access_token")) {
	    			return json.get("access_token").getAsString();
	    		} else {
	    			throw new RuntimeException("Failed to refresh access token.");
	    		}
	    	});
	    	
	    	if(token.isEmpty()) throw new RuntimeException("Failed to refresh access token.");
	    	
	    	Config.YOUTUBE_ACCESS_TOKEN=token;
	    	Config.YOUTUBE_TOKEN_TIMESTAMP=System.currentTimeMillis()/1000l;
	    	Config.saveConfig();
	    	
			ANSI.println("[GREEN]done![PROMPT]");
	    	return true;
	    	
		} catch (Exception e) {
			ANSI.println("[RED]failed![RESET]");
			ANSI.printErr(e.getMessage(),e);
			return false;
		}

	}
		
	public static Future<Packet> livestreamsByChannelId(String id) {
		String query=URL+"search?part=snippet&eventType=live&maxResults=1&type=video&prettyPrint=true&channelId=".concat(id).concat("&key=").concat(Config.YOUTUBE_KEY);
		
		return Helper.EXECUTOR.submit(new Response<Packet>(query) {
			
			@Override
			protected Packet handle(JsonObject json) {
				JsonArray array=json.getAsJsonArray("items");
				if(array.size()>0) {
					return Packet.build(array.get(0).getAsJsonObject(),LiveStreamPacket.class);
				}
				return Packet.build(new JsonObject(),EmptyPacket.class);
			}

			@Override
			protected Packet handleErr(JsonObject json) {
				return Packet.build(json,ErrorPacket.class);
			}
			
		});
		
	}
	
	public static Future<Packet> createLiveStream(String title,String description,String privacy) {
						
		return Helper.EXECUTOR.submit(()->{
			if(isExpired()&&!refreshToken()) return Packet.build(new JsonObject(),EmptyPacket.class);
			
			String url=URL.concat("liveStreams?part=snippet,cdn,contentDetails,status&key=").concat(Config.YOUTUBE_KEY);
			
			HttpPost post=new HttpPost(url);
			post.setHeader("Authorization","Bearer ".concat(Config.YOUTUBE_ACCESS_TOKEN));
			post.addHeader("Accept","application/json");
			post.addHeader("Content-Type","application/json");
			
			ANSI.println("Bearer ".concat(Config.YOUTUBE_ACCESS_TOKEN));
			
			JsonObject snippet=new JsonObject();
			snippet.addProperty("title",title);
			snippet.addProperty("description", description);
			snippet.addProperty("isDefaultStream",true);
			
			JsonObject cdn=new JsonObject();
			cdn.addProperty("frameRate","60fps");
			cdn.addProperty("ingestionType","rtmp");
			cdn.addProperty("resolution","1080p");
			
			JsonObject details=new JsonObject();
			details.addProperty("isReusable",true);
			
			JsonObject status=new JsonObject();
			status.addProperty("privacyStatus",privacy);
			
			JsonObject live=new JsonObject();
			live.add("snippet",snippet);
			live.add("cdn",cdn);
			live.add("contentDetails",details);
			live.add("status",status);
			
			post.setEntity(new StringEntity(live.toString(),"UTF-8"));
			
			try {
				return HTTP_CLIENT.execute(post,response->{
					BroadcastRunner.stopAndHold();
					JsonObject json=JsonParser.parseString(EntityUtils.toString(response.getEntity())).getAsJsonObject();
					if(json.has("streamName")) {
						ANSI.println("hihi");
						Config.STREAM_KEY=json.get("streamName").getAsString();
						Config.saveConfig();
					}
					BroadcastRunner.unhold();
					ANSI.println(json.toString());
					return Packet.build(new JsonObject(),EmptyPacket.class); 
				});
			} catch (IOException e) {
				ANSI.printErr(e.getMessage(),e);
				return Packet.build(new JsonObject(),EmptyPacket.class);
			}
		});		
	}
	
	public static void close() {
		ANSI.println("Close YouTube httpclient.");
		if(HTTP_CLIENT!=null) {
			try {
				HTTP_CLIENT.close();
			} catch (IOException e) {
				ANSI.printErr(e.getMessage(),e);
			}
		}
	}
	
}

