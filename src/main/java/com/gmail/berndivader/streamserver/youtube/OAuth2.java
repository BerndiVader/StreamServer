package com.gmail.berndivader.streamserver.youtube;

import java.util.AbstractMap;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.mysql.VerifyOAuth2;
import com.gmail.berndivader.streamserver.term.ANSI;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class OAuth2 {
	
	private static final String OAUTH_URL="https://accounts.google.com/o/oauth2/v2/auth?client_id=%s&redirect_uri=%s&response_type=code&scope=https://www.googleapis.com/auth/youtube&state=%s&access_type=offline&prompt=consent";
	
	private OAuth2() {}

	public static boolean build() {
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
				if(!verify.future.get(10l,TimeUnit.SECONDS)) throw new RuntimeException("Failed to verify OAuth2 request.");
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				throw new Exception("Failed to verify OAuth2 request.",e);
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

			Entry<String,String>pair=Youtube.HTTP_CLIENT.execute(post,response->{
				String accessToken="";
				String refreshToken="";
				JsonObject json=JsonParser.parseString(EntityUtils.toString(response.getEntity())).getAsJsonObject();
				if(json.has("refresh_token")) refreshToken=json.get("refresh_token").getAsString();
				if(json.has("access_token")) accessToken=json.get("access_token").getAsString();
				if(json.has("error")) {
					ANSI.println("[RED]failed!");
					ANSI.printErr(json.get("error").getAsString()+" Reason: "+json.get("error_description").getAsString(),new RuntimeException("OAuth2 registration failed."));
				}
				ANSI.println(json.toString());
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

	public static boolean refresh() {
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

			String token=Youtube.HTTP_CLIENT.execute(post,response->{

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

	public static boolean isExpired() {
		return System.currentTimeMillis()/1000l-Config.YOUTUBE_TOKEN_TIMESTAMP>Config.YOUTUBE_TOKEN_EXPIRE_TIME;
	}		
}
