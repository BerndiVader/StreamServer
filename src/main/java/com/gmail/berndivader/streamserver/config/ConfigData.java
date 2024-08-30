package com.gmail.berndivader.streamserver.config;

import java.util.HashMap;

import com.gmail.berndivader.streamserver.discord.permission.Guild;
import com.gmail.berndivader.streamserver.discord.permission.User;
import com.google.gson.annotations.SerializedName;

public class ConfigData {
	
	@SerializedName("STREAM_KEY")
	String YOUTUBE_STREAM_KEY;
	@SerializedName("STREAM_URL")
	String YOUTUBE_STREAM_URL;
	
	String BROADCAST_DEFAULT_TITLE;
	String BROADCAST_DEFAULT_DESCRIPTION;
	String BROADCAST_DEFAULT_PRIVACY;
	Long BROADCAST_REPEAT_TIMER_INTERVAL;
	
	@SerializedName("YOUTUBE_KEY")
	String YOUTUBE_API_KEY;
	String YOUTUBE_CLIENT_ID;
	String YOUTUBE_CLIENT_SECRET;
	String YOUTUBE_AUTH_REDIRECT;
	String YOUTUBE_ACCESS_TOKEN;
	String YOUTUBE_REFRESH_TOKEN;
	Long YOUTUBE_TOKEN_TIMESTAMP;
	
	Boolean YOUTUBE_USE_COOKIES;
	
	Boolean STREAM_BOT_START;
	Boolean DISCORD_BOT_START;
	
	String PLAYLIST_PATH;
	String PLAYLIST_PATH_CUSTOM;
	
	String DL_ROOT_PATH;
	String DL_MUSIC_PATH;
	String DL_TEMP_PATH;
	String DL_MEDIA_PATH;
	String DL_WWW_THUMBNAIL_PATH;
	
	Long DL_TIMEOUT_SECONDS;
	String DL_URL;
	String DL_INTERVAL_FORMAT;
	Integer DL_INTERVAL_VALUE;
	
	String DATABASE_CONNECTION;
	String DATABASE_HOST;
	String DATABASE_PORT;
	String DATABASE_NAME;
	String DATABASE_USER;
	String DATABASE_PWD;
	
	String DISCORD_TOKEN;
	@SerializedName("DISCORD_CHANNEL")
	String DISCORD_VOICE_CHANNEL_NAME;
	Boolean DISCORD_MUSIC_BOT;
	Long DISCORD_ROLE_ID;
	
	HashMap<Long,Guild>DISCORD_PERMITTED_GUILDS;
	HashMap<Long,User>DISCORD_PERMITTED_USERS;
	
}
