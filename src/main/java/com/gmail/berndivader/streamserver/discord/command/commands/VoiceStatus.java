package com.gmail.berndivader.streamserver.discord.command.commands;

import java.util.function.Consumer;

import com.gmail.berndivader.streamserver.annotation.DiscordCommand;
import com.gmail.berndivader.streamserver.discord.DiscordBot;
import com.gmail.berndivader.streamserver.discord.command.Command;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

@DiscordCommand(name="status")
public class VoiceStatus extends Command<Message> {
	
	@Override
	public Mono<Message> execute(String string,MessageChannel channel) {
		
		AudioTrack track=DiscordBot.instance.audioPlayer.getPlayingTrack();
		if(track!=null) {
			return channel.createEmbed(new Consumer<EmbedCreateSpec>() {

				@Override
				public void accept(EmbedCreateSpec embed) {
					embed.setTitle("Audioinfo:");
					embed.setColor(Color.GREEN);
					String outcome="Identifier:".concat(track.getIdentifier());
					outcome=outcome.concat("\nState:").concat(track.getState().name());
					outcome=outcome.concat("Title:").concat(track.getInfo().title);
					embed.setDescription(outcome);
				}
			});
		}

		return channel.createEmbed(new Consumer<EmbedCreateSpec>() {

			@Override
			public void accept(EmbedCreateSpec embed) {
				embed.setTitle("Error:");
				embed.setColor(Color.RED);
				embed.setDescription("No connection to livestream.");
			}
		});
	}

}
