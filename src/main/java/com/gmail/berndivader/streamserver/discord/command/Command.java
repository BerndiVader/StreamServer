package com.gmail.berndivader.streamserver.discord.command;

import com.gmail.berndivader.streamserver.discord.permission.Permission;
import com.gmail.berndivader.streamserver.discord.permission.Permissions;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

public abstract class Command<T> {
	
	public Mono<T> exec(String string,MessageChannel channel,Member member) {
		
		
		if(Permissions.Users.permitted(member.getId().asLong(),this.getClass().getDeclaredAnnotation(Permission.class).required())) {
			return execute(string,channel);
		} else {
			return Mono.empty();
		}
	}
	
	protected abstract Mono<T> execute(String string,MessageChannel channel);
	
}
