package com.gmail.berndivader.streamserver.config;

import java.util.HashMap;

import com.gmail.berndivader.streamserver.discord.permission.Guild;
import com.gmail.berndivader.streamserver.discord.permission.User;
import com.google.gson.annotations.SerializedName;

public class Discord {
	
	public Boolean BOT_START;
	public Boolean MUSIC_BOT;
	
	public String CMD_PREFIX;
	public String TOKEN;
	public Boolean DELETE_CMD_MESSAGE;
	@SerializedName("DISCORD_CHANNEL")
	public String VOICE_CHANNEL_NAME;
	public Boolean MUSIC_AUTOPLAY;
	public Long ROLE_ID;
		
	public HashMap<Long,Guild>PERMITTED_GUILDS;
	public HashMap<Long,User>PERMITTED_USERS;
			
	public Discord() {
		
		BOT_START=false;
		MUSIC_BOT=false;
		CMD_PREFIX=".";
		TOKEN="BOT-TOKEN";
		DELETE_CMD_MESSAGE=false;
		VOICE_CHANNEL_NAME="YAMPB-VOICE";
		MUSIC_AUTOPLAY=false;
		ROLE_ID=0l;
		
		PERMITTED_GUILDS=new HashMap<Long,Guild>();
		PERMITTED_USERS=new HashMap<Long,User>();
		
	}

}
