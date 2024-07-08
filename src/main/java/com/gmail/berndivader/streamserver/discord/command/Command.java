package com.gmail.berndivader.streamserver.discord.command;

import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

public abstract class Command<T> {
	
	public final Mono<T> exec(String string,MessageChannel channel) {
		return execute(string, channel);
	}
	
	protected abstract Mono<T> execute(String string,MessageChannel channel);
	
}
