package com.gmail.berndivader.streamserver.discord.action;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.ffmpeg.FFProbePacket;
import com.google.gson.JsonObject;

public class Action {

	public final transient JsonObject json;
	public final transient FFProbePacket packet;
	public final ID action;
	public final String uid;
	public final transient Long userId;

	private Action(ID action,String id,FFProbePacket packet,Long userId) {
		this.action=action;
		this.uid=id;
		this.userId=userId;
		this.packet=packet;
		this.json=Helper.GSON.toJsonTree(this).getAsJsonObject();
	}
	
	public static Action build(ID actionId,String id,FFProbePacket packet,Long userId) {
		Action action=new Action(actionId,id,packet,userId);
		return action;
	}
	
	@Override
	public String toString() {
		return ButtonAction.ACTION_PREFIX.concat(Helper.GSON.toJson(json));
	}
	
}