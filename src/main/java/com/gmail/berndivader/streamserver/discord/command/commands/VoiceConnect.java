package com.gmail.berndivader.streamserver.discord.command.commands;

import java.util.function.Consumer;

import com.gmail.berndivader.streamserver.annotation.DiscordCommand;
import com.gmail.berndivader.streamserver.discord.DiscordBot;
import com.gmail.berndivader.streamserver.discord.command.Command;

import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

@DiscordCommand(name="reconnect")
public class VoiceConnect extends Command<Void> {

	@Override
	public Mono<Void> execute(String string, MessageChannel channel) {

		try {
			DiscordBot.instance.connectStream();
			return channel.createEmbed(new Consumer<EmbedCreateSpec>() {

				@Override
				public void accept(EmbedCreateSpec embed) {
					embed.setTitle("Reconnect to youtube");
					embed.setColor(Color.CINNABAR);
					embed.setDescription("Try to reconnect to youtube stream.");
				}
			}).then();
		} catch (Exception e) {
			return channel.createEmbed(new Consumer<EmbedCreateSpec>() {

				@Override
				public void accept(EmbedCreateSpec embed) {
					embed.setTitle("Reconnect to youtube");
					embed.setColor(Color.CINNABAR);
					embed.setDescription("Failed. ".concat(e.getMessage()));
				}
			}).then();			
		}
	}

}
