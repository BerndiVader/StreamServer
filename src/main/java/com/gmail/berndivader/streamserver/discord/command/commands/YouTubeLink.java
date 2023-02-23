package com.gmail.berndivader.streamserver.discord.command.commands;

import java.util.function.Consumer;

import com.gmail.berndivader.streamserver.annotation.DiscordCommand;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.discord.command.Command;

import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

@DiscordCommand(name="link")
public class YouTubeLink extends Command<Void> {

	@Override
	public Mono<Void> execute(String string, MessageChannel channel) {
		return channel.createEmbed(new Consumer<EmbedCreateSpec>() {

			@Override
			public void accept(EmbedCreateSpec embed) {
				embed.setTitle("Youtube link:");
				embed.setColor(Color.CINNABAR);
				embed.setDescription(Config.YOUTUBE_LINK);
			}
			
		}).then();
		
	}

}
