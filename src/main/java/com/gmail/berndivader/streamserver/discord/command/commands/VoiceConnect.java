package com.gmail.berndivader.streamserver.discord.command.commands;

import java.util.function.Consumer;

import com.gmail.berndivader.streamserver.StreamServer;
import com.gmail.berndivader.streamserver.annotation.DiscordCommand;
import com.gmail.berndivader.streamserver.discord.command.Command;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

@DiscordCommand(name="reconnect")
public class VoiceConnect extends Command<Message> {

	@Override
	public Mono<Message> execute(String string, MessageChannel channel) {
		
		StreamServer.DISCORDBOT.provider.delayedConnect();
		
		return channel.createEmbed(new Consumer<EmbedCreateSpec>() {

			@Override
			public void accept(EmbedCreateSpec embed) {
				embed.setTitle("Reconnect to youtube");
				embed.setColor(Color.CINNABAR);
				embed.setDescription("Try to reconnect to youtube stream.");
			}
			
		});
		
	}

}
