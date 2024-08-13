package com.gmail.berndivader.streamserver.discord.permission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Guild {
		
	public String name;
	public List<Long>channelId=new ArrayList<Long>();

	public Guild(String name,Long...channelId) {
		this.name=name;
		this.channelId=Arrays.asList(channelId);
	}
	
	public void addChannelId(Long...channelId) {
		this.channelId.addAll(Arrays.asList(channelId));
	}
	
}