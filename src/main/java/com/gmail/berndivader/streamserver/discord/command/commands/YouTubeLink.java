package com.gmail.berndivader.streamserver.discord.command.commands;

import com.gmail.berndivader.streamserver.annotation.DiscordCommand;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.discord.command.Command;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

@DiscordCommand(name="link",usage="Display youtube stream url.")
public class YouTubeLink extends Command<Message> {

	@Override
	public Mono<Message> execute(String string, MessageChannel channel) {
		
		return channel.createMessage(EmbedCreateSpec.builder()
				.title("Youtube link:")
				.color(Color.CINNABAR)
				.description(Config.YOUTUBE_LINK)
				.build());
				
	}

}
