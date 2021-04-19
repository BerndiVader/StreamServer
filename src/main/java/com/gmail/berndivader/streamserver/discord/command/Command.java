package com.gmail.berndivader.streamserver.discord.command;

import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

public abstract class Command<T> {

	public abstract Mono<T> execute(String string,MessageChannel channel);
	
}
