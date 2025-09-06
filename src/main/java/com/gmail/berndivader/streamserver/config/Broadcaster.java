package com.gmail.berndivader.streamserver.config;

import com.google.gson.annotations.SerializedName;

public class Broadcaster {
	
	public static final Long YOUTUBE_TOKEN_EXPIRE_TIME=3599l;
	public static final long PLAYLIST_REFRESH_INTERVAL=60l;	
	
	public Boolean STREAM_BOT_START;
	@SerializedName("STREAM_KEY")
	public String YOUTUBE_STREAM_KEY;
	@SerializedName("STREAM_URL")
	public String YOUTUBE_STREAM_URL;
	public String BROADCAST_DEFAULT_TITLE;
	public String BROADCAST_DEFAULT_DESCRIPTION;
	public String BROADCAST_DEFAULT_PRIVACY;
	public Long BROADCAST_REPEAT_TIMER_INTERVAL;
	@SerializedName("YOUTUBE_KEY")
	public String YOUTUBE_API_KEY;
	public String YOUTUBE_CLIENT_ID;
	public String YOUTUBE_CLIENT_SECRET;
	public String YOUTUBE_AUTH_REDIRECT;
	public String YOUTUBE_ACCESS_TOKEN;
	public String YOUTUBE_REFRESH_TOKEN;
	public Long YOUTUBE_TOKEN_TIMESTAMP;
	public String PLAYLIST_PATH;
	public String PLAYLIST_PATH_CUSTOM;
	
	public Broadcaster() {
		BROADCAST_DEFAULT_TITLE="Lorem ipsum";
		BROADCAST_DEFAULT_DESCRIPTION="Lorem ipsum dolor sit amet, consectetur adipiscing elit.";
		BROADCAST_DEFAULT_PRIVACY="private";
		BROADCAST_REPEAT_TIMER_INTERVAL=1l;
		
		YOUTUBE_STREAM_KEY="xxxx-xxxx-xxxx-xxxx-xxxx";
		YOUTUBE_STREAM_URL="rtmp://a.rtmp.youtube.com/live2";
		
		YOUTUBE_API_KEY="YT-API-KEY";
		YOUTUBE_CLIENT_ID="YT-CLIENT-ID";
		YOUTUBE_CLIENT_SECRET="YT-CLIENT-SECRET";
		YOUTUBE_AUTH_REDIRECT="https://YOUR.OAUTH2-REDIRECT.PAGE";
		YOUTUBE_ACCESS_TOKEN="YT-OAUTH2-ACCESS-TOKEN";
		YOUTUBE_REFRESH_TOKEN="YT-OUATH2-REFRESH-TOKEN";
		YOUTUBE_TOKEN_TIMESTAMP=0l;
		
		STREAM_BOT_START=false;
		
		PLAYLIST_PATH="./playlist";
		PLAYLIST_PATH_CUSTOM="./custom";
	}

}
