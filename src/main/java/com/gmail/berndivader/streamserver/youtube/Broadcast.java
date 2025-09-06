package com.gmail.berndivader.streamserver.youtube;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.term.ANSI;
import com.gmail.berndivader.streamserver.youtube.packets.ErrorPacket;
import com.gmail.berndivader.streamserver.youtube.packets.LiveBroadcastPacket;
import com.gmail.berndivader.streamserver.youtube.packets.LiveStreamPacket;
import com.gmail.berndivader.streamserver.youtube.packets.Packet;
import com.gmail.berndivader.streamserver.youtube.packets.UnknownPacket;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class Broadcast {
		
	private Broadcast() {}

	public static Packet getLiveBroadcastWithTries(BroadcastStatus status,int tries) throws InterruptedException, ExecutionException, TimeoutException {
		Packet packet=null;
		do {
			packet=getLiveBroadcast(status).get(15l,TimeUnit.SECONDS);
		} while(!(packet instanceof LiveBroadcastPacket)&&--tries>0);
		return packet;
	}
	
	public static Future<Packet>getLiveBroadcast(BroadcastStatus broadcastStatus) {
		return Helper.EXECUTOR.submit(()->{
			BroadcastStatus status=broadcastStatus;
			if (OAuth2.isExpired()&&!OAuth2.refresh()) return ErrorPacket.buildError("Token expired.","Access token is expired and refresh failed.","CUSTOM");

			String url=Youtube.URL.concat("liveBroadcasts?part=id,snippet,contentDetails,monetizationDetails,status&broadcastStatus="+status.name()+"&key=").concat(Config.BROADCASTER.YOUTUBE_API_KEY);
			HttpGet get=new HttpGet(url);
			get.setHeader("Authorization","Bearer ".concat(Config.BROADCASTER.YOUTUBE_ACCESS_TOKEN));
			get.addHeader("Accept","application/json");

			try {
				return Youtube.HTTP_CLIENT.execute(get,response->{
					JsonObject json=JsonParser.parseString(EntityUtils.toString(response.getEntity())).getAsJsonObject();
					if(json==null||json.isJsonNull()) {
						return ErrorPacket.buildError("Request liveBroadcast status failed.","Nulljson was returned.","CUSTOM");
					} else if(json.has("error")) {
						return Packet.build(json,ErrorPacket.class);
					} else if(json.has("kind")&&json.get("kind").getAsString().equals("youtube#liveBroadcastListResponse")) {
						JsonArray array=json.getAsJsonArray("items");
						if(array!=null&&!array.isJsonNull()&&array.size()>0) {
							return Packet.build(array.get(0).getAsJsonObject(),LiveBroadcastPacket.class);
						}
						return Packet.emtpy();
					} else {
						return Packet.build(json,UnknownPacket.class);
					}
				});
			} catch (Exception e) {
				if(Config.DEBUG) ANSI.error(e.getMessage(),e);
				return ErrorPacket.buildError("Request liveBroadcast status failed.",e.getMessage(),"CUSTOM");
			}

		});
	}
	
	public static Future<Packet>insertLiveBroadcast(String title,String description,PrivacyStatus privacy) {
		return Helper.EXECUTOR.submit(()->{
			if(OAuth2.isExpired()&&!OAuth2.refresh()) return ErrorPacket.buildError("Token expired.","Access token is expired and refresh failed.","CUSTOM");

			String url=Youtube.URL.concat("liveBroadcasts?part=id,snippet,contentDetails,status&key=").concat(Config.BROADCASTER.YOUTUBE_API_KEY);

			HttpPost post=new HttpPost(url);
			post.setHeader("Authorization","Bearer ".concat(Config.BROADCASTER.YOUTUBE_ACCESS_TOKEN));
			post.addHeader("Accept","application/json");
			post.addHeader("Content-Type","application/json");

			JsonObject snippet=new JsonObject();
			snippet.addProperty("title",title);
			snippet.addProperty("description",description);
			ZonedDateTime now=ZonedDateTime.now().plusMinutes(1l);
			DateTimeFormatter formatter=DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
			String scheduledStartTime=now.format(formatter);	        
			snippet.addProperty("scheduledStartTime",scheduledStartTime);

			JsonObject status=new JsonObject();
			status.addProperty("privacyStatus",privacy.getName());
			status.addProperty("selfDeclaredMadeForKids",false);
			
			JsonObject contentDetails=new JsonObject();
			contentDetails.addProperty("enableAutoStart",true);
			contentDetails.addProperty("enableEmbed",true);

			JsonObject liveBroadcast=new JsonObject();
			liveBroadcast.add("snippet",snippet);
			liveBroadcast.add("status",status);
			liveBroadcast.add("contentDetails",contentDetails);

			post.setEntity(new StringEntity(liveBroadcast.toString(),"UTF-8"));

			try {
				return Youtube.HTTP_CLIENT.execute(post,response->{
					JsonObject json=JsonParser.parseString(EntityUtils.toString(response.getEntity())).getAsJsonObject();
					if(json.isJsonNull()) {
						return ErrorPacket.buildError("Request to insert liveBroadcast failed.","Nulljson was returned.","CUSTOM");
					} else if(json.has("error")) {
						return Packet.build(json,ErrorPacket.class);
					} else if(json.has("kind")&&json.get("kind").getAsString().equals("youtube#liveBroadcast")) {
						return Packet.build(json,LiveBroadcastPacket.class);
					} else {
						return Packet.build(json,UnknownPacket.class);
					}
				});
			} catch(Exception e) {
				if(Config.DEBUG) ANSI.error(e.getMessage(),e);
				return ErrorPacket.buildError("Request to insert liveBroadcast failed.",e.getMessage(),"CUSTOM");
			}
		});
	}

	public static Future<Packet>insertLivestream(String title,String description,String privacy) {

		return Helper.EXECUTOR.submit(()->{
			if(OAuth2.isExpired()&&!OAuth2.refresh()) return ErrorPacket.buildError("Token expired.","Access token is expired and refresh failed.","CUSTOM");

			String url=Youtube.URL.concat("liveStreams?part=snippet,cdn,contentDetails,status&key=").concat(Config.BROADCASTER.YOUTUBE_API_KEY);

			HttpPost post=new HttpPost(url);
			post.setHeader("Authorization","Bearer ".concat(Config.BROADCASTER.YOUTUBE_ACCESS_TOKEN));
			post.addHeader("Accept","application/json");
			post.addHeader("Content-Type","application/json");

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
				return Youtube.HTTP_CLIENT.execute(post,response->{
					JsonObject json=JsonParser.parseString(EntityUtils.toString(response.getEntity())).getAsJsonObject();
					if(json==null||json.isJsonNull()||json.isEmpty()) {
						return Packet.emtpy();
					} else if(json.has("kind")&&json.get("kind").getAsString().equals("youtube#liveStream")) {
						return Packet.build(json,LiveStreamPacket.class);
					} else if(json.has("error")) {
						return Packet.build(json,ErrorPacket.class);
					} else {
						return Packet.build(json,UnknownPacket.class);
					}
				});
			} catch (IOException e) {
				if(Config.DEBUG) ANSI.error(e.getMessage(),e);
				return ErrorPacket.buildError("Request to insert a new liveStream failed.", e.getMessage(),"CUSTOM");
			}
		});		
	}

	public static Future<Packet> bindBroadcastToStream(String broadcastId,String streamId) {
		return Helper.EXECUTOR.submit(()->{
			if (OAuth2.isExpired()&&!OAuth2.refresh()) return ErrorPacket.buildError("Token expired.","Access token is expired and refresh failed.","CUSTOM");

			String url=Youtube.URL.concat("liveBroadcasts/bind?id="+broadcastId+"&streamId="+streamId+"&part=id,snippet,contentDetails,status&key=").concat(Config.BROADCASTER.YOUTUBE_API_KEY);
			HttpPost post=new HttpPost(url);
			post.setHeader("Authorization","Bearer ".concat(Config.BROADCASTER.YOUTUBE_ACCESS_TOKEN));
			post.addHeader("Accept","application/json");
			post.addHeader("Content-Type","application/json");			

			try {
				return Youtube.HTTP_CLIENT.execute(post,response-> {
					JsonObject json=JsonParser.parseString(EntityUtils.toString(response.getEntity())).getAsJsonObject();
					if(json==null||json.isJsonNull()||json.isEmpty()) {
						return ErrorPacket.buildError("Request to bind liveBroadcast to liveStream failed.","Nulljson was returned.","CUSTOM");
					} else if(json.has("error")) {
						return Packet.build(json, ErrorPacket.class);
					} else if(json.has("kind")&&json.get("kind").getAsString().equals("youtube#liveBroadcast")) {
						return Packet.build(json,LiveBroadcastPacket.class);
					} else {
						return Packet.build(json,UnknownPacket.class);
					}
				});
			} catch (Exception e) {
				if(Config.DEBUG) ANSI.error(e.getMessage(),e);
				return ErrorPacket.buildError("Request to bind liveBroadcast to liveStream failed.", e.getMessage(),"CUSTOM");
			}
		});
	}

	public static Future<Packet> transitionBroadcastStatus(String broadcastId,String status) {
		return Helper.EXECUTOR.submit(()->{
			if (OAuth2.isExpired()&&!OAuth2.refresh()) return ErrorPacket.buildError("Token expired.","Access token is expired and refresh failed.","CUSTOM");

			String url=Youtube.URL.concat("liveBroadcasts/transition?broadcastStatus="+status+"&id="+broadcastId+"&part=id,snippet,contentDetails,status&key=").concat(Config.BROADCASTER.YOUTUBE_API_KEY);
			HttpPost post=new HttpPost(url);
			post.setHeader("Authorization","Bearer ".concat(Config.BROADCASTER.YOUTUBE_ACCESS_TOKEN));
			post.addHeader("Accept","application/json");
			post.addHeader("Content-Type","application/json");

			try {
				return Youtube.HTTP_CLIENT.execute(post,response-> {
					JsonObject json=JsonParser.parseString(EntityUtils.toString(response.getEntity())).getAsJsonObject();
					if(json==null||json.isJsonNull()) {
						return ErrorPacket.buildError("Request to transition liveBroadcast status failed.","Nulljson was returned.","CUSTOM");
					} else if(json.has("error")) {
						return Packet.build(json,ErrorPacket.class);
					} else if(json.has("kind")&&json.get("kind").getAsString().equals("youtube#liveBroadcast")) {
						return Packet.build(json,LiveBroadcastPacket.class);
					} else {
						return Packet.build(json,UnknownPacket.class);
					}
				});
			} catch(Exception e) {
				if(Config.DEBUG) ANSI.error(e.getMessage(),e);
				return ErrorPacket.buildError("Request to transition liveBroadcast status failed.",e.getMessage(),"CUSTOM");
			}
		});
	}
	
	public static Future<Packet> getLiveStreamById(String id) {
		return Helper.EXECUTOR.submit(()->{
			
			if (OAuth2.isExpired()&&!OAuth2.refresh()) return ErrorPacket.buildError("Token expired.","Access token is expired and refresh failed.","CUSTOM");

			String url=Youtube.URL.concat(String.format("liveStreams?id=%s&part=id,snippet,cdn,status&key=%s",id,Config.BROADCASTER.YOUTUBE_API_KEY));
			HttpGet get=new HttpGet(url);
			get.setHeader("Authorization","Bearer ".concat(Config.BROADCASTER.YOUTUBE_ACCESS_TOKEN));
			get.addHeader("Accept","application/json");
			
			try {
				
				return Youtube.HTTP_CLIENT.execute(get,response-> {
					JsonObject json=JsonParser.parseString(EntityUtils.toString(response.getEntity())).getAsJsonObject();
					if(json==null||json.isJsonNull()) {
						return ErrorPacket.buildError("Request to get livestream resource by id failed.","Nulljson was returned.","CUSTOM");
					} else if(json.has("error")) {
						return Packet.build(json,ErrorPacket.class);
					} else if(json.has("kind")&&json.get("kind").getAsString().equals("youtube#liveStreamListResponse")) {
						if(json.has("items")&&!json.getAsJsonArray("items").isEmpty()) {
							JsonArray array=json.getAsJsonArray("items");
							if(array!=null&&!array.isJsonNull()&&array.size()>0) {
								return Packet.build(array.get(0).getAsJsonObject(),LiveStreamPacket.class);
							}
					}
						return Packet.emtpy();
					} else {
						return Packet.build(json,UnknownPacket.class);
					}
				});
				
			} catch(Exception e) {
				if(Config.DEBUG) ANSI.error(e.getMessage(),e);
				return ErrorPacket.buildError("Request to get livestream resource by id failed.",e.getMessage(),"CUSTOM");
			}

		});
	}
	
	public static Future<Packet> getDefaultLiveStream() {
		return Helper.EXECUTOR.submit(()->{
			if (OAuth2.isExpired()&&!OAuth2.refresh()) return ErrorPacket.buildError("Token expired.","Access token is expired and refresh failed.","CUSTOM");

			String url=Youtube.URL.concat("liveStreams?part=id,snippet,cdn,status&mine=true&key=").concat(Config.BROADCASTER.YOUTUBE_API_KEY);
			HttpGet get=new HttpGet(url);
			get.setHeader("Authorization","Bearer ".concat(Config.BROADCASTER.YOUTUBE_ACCESS_TOKEN));
			get.addHeader("Accept","application/json");

			try {
				return Youtube.HTTP_CLIENT.execute(get,response-> {
					JsonObject json=JsonParser.parseString(EntityUtils.toString(response.getEntity())).getAsJsonObject();
					if(json==null||json.isJsonNull()) {
						return ErrorPacket.buildError("Request to get default livestream resource failed.","Nulljson was returned.","CUSTOM");
					} else if(json.has("error")) {
						return Packet.build(json,ErrorPacket.class);
					} else if(json.has("kind")&&json.get("kind").getAsString().equals("youtube#liveStreamListResponse")) {
						if(json.has("items")&&!json.getAsJsonArray("items").isEmpty()) {
							JsonArray array=json.getAsJsonArray("items");
							for(JsonElement element:array) {
								LiveStreamPacket packet=(LiveStreamPacket)Packet.build(element.getAsJsonObject(),LiveStreamPacket.class);
								if(packet.cdn.ingestionInfo.streamName.equals(Config.BROADCASTER.YOUTUBE_STREAM_KEY)) return packet;
							}
						}
						return Packet.emtpy();
					} else {
						return Packet.build(json,UnknownPacket.class);
					}
				});
			} catch(Exception e) {
				if(Config.DEBUG) ANSI.error(e.getMessage(),e);
				return ErrorPacket.buildError("Request to get default livestream resource failed.",e.getMessage(),"CUSTOM");
			}
		});		
	}
		
}
