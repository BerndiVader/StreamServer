package com.gmail.berndivader.streamserver.config;

import java.awt.Point;
import java.util.HashMap;

import com.gmail.berndivader.streamserver.discord.permission.Guild;
import com.gmail.berndivader.streamserver.discord.permission.User;
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
	public Boolean YOUTUBE_USE_COOKIES;
	public String PLAYLIST_PATH;
	public String PLAYLIST_PATH_CUSTOM;
	
	public String DL_YTDLP_PATH;
	public String DL_FFMPEG_PATH;
	public String DL_FFPROBE_PATH;
	public String DL_ROOT_PATH;
	public String DL_MUSIC_PATH;
	public String DL_TEMP_PATH;
	public String DL_MEDIA_PATH;
	public String DL_WWW_THUMBNAIL_PATH;
	public Point DL_THUMBNAIL_SIZE;
	
	public Long DL_TIMEOUT_SECONDS;
	public String DL_URL;
	public String DL_INTERVAL_FORMAT;
	public Integer DL_INTERVAL_VALUE;
	
	public Boolean DATABASE_USE;
	public String DATABASE_HOST;
	public String DATABASE_PORT;
	public String DATABASE_NAME;
	public String DATABASE_USER;
	public String DATABASE_PWD;
	public Long DATABASE_TIMEOUT_SECONDS;
	
	public Boolean DISCORD_BOT_START;
	public String DISCORD_TOKEN;
	public Boolean DISCORD_DELETE_CMD_MESSAGE;
	public HashMap<Long,Guild>DISCORD_PERMITTED_GUILDS;
	public HashMap<Long,User>DISCORD_PERMITTED_USERS;
	public Long DISCORD_ROLE_ID;
	
	public Boolean DISCORD_MUSIC_BOT;
	@SerializedName("DISCORD_CHANNEL")
	public String DISCORD_VOICE_CHANNEL_NAME;
	public Boolean DISCORD_MUSIC_AUTOPLAY;
	
	public WebSocket WEBSOCKET;
	
}
