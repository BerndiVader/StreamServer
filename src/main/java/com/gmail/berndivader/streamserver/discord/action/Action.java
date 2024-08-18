package com.gmail.berndivader.streamserver.discord.action;

import com.gmail.berndivader.streamserver.Helper;
import com.google.gson.JsonObject;

public abstract class Action {
	
	public JsonObject source;
	
	public ID actionId;
	public Long userId;
	public String uuid;
	
	protected static Action build(JsonObject source,Class<? extends Action> clazz) {
		Action action=Helper.GSON.fromJson(source,clazz);
		action.source=source;		
		return action;
	}
	
}