package com.gmail.berndivader.streamserver.config;

import com.google.gson.annotations.SerializedName;

public class Data {
	
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
	
	public Boolean DATABASE_USE;
	public String DATABASE_HOST;
	public String DATABASE_PORT;
	public String DATABASE_NAME;
	public String DATABASE_USER;
	public String DATABASE_PWD;
	public Long DATABASE_TIMEOUT_SECONDS;
	
	public Downloader DOWNLOADER;
	public Discord DISCORD;
	public WebSocket WEBSOCKET;
	
	public Boolean DEBUG;
	
}
