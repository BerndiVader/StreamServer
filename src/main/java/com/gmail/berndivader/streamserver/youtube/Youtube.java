package com.gmail.berndivader.streamserver.youtube;

import java.io.IOException;
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
    private static final String OAUTH_URL="https://accounts.google.com/o/oauth2/v2/auth?client_id=%s&redirect_uri=%s&response_type=code&scope=https://www.googleapis.com/auth/youtube&state=%s";

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
			
        	String token=HTTP_CLIENT.execute(post,response->{
				JsonObject json=JsonParser.parseString(EntityUtils.toString(response.getEntity())).getAsJsonObject();
				if(json.has("access_token")) {
					return json.get("access_token").getAsString();
				} else if(json.has("error")) {
					ANSI.println("[RED]failed!");
					ANSI.printErr(json.get("error").getAsString()+" Reason: "+json.get("error_description").getAsString(),new Throwable("OAuth2 registration failed."));
				}
				return "";
			});
        	
        	if(token.isEmpty()) throw(new Exception("Failed to receive token."));
        	ANSI.println("[GREEN]done!");
        	Config.YOUTUBE_ACCESS_TOKEN=token;
        	Config.saveConfig();
        	
		} catch (Exception e) {
			ANSI.printErr(e.getMessage(),e);
			return false;
		}
		return true;
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

