package com.gmail.berndivader.streamserver.discord.command;

import com.gmail.berndivader.streamserver.discord.permission.Permission;
import com.gmail.berndivader.streamserver.discord.permission.Permissions;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

public abstract class Command<T> {
	
	protected Member member;
	protected MessageChannel channel;
	protected String string;
	
	public Mono<T> execute(String string,MessageChannel channel,Member member) {
		this.member=member;
		this.channel=channel;
		this.string=string;
		
		if(Permissions.Users.permitted(member.getId().asLong(),this.getClass().getDeclaredAnnotation(Permission.class).required())) {
			return exec();
		} else {
			return Mono.empty();
		}
	}
	
	protected abstract Mono<T> exec();
	
}
