package com.gmail.berndivader.streamserver.discord.command.commands;

import java.util.function.Consumer;

import com.gmail.berndivader.streamserver.annotation.DiscordCommand;
import com.gmail.berndivader.streamserver.annotation.Requireds;
import com.gmail.berndivader.streamserver.discord.command.Command;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;

import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

@DiscordCommand(name="current",usage="Display current playing file.",requireds={Requireds.BROADCASTRUNNER})
public class Current extends Command<Void> {

	@Override
	public Mono<Void> execute(String string, MessageChannel channel) {
		
		return channel.createEmbed(new Consumer<EmbedCreateSpec>() {

			@Override
			public void accept(EmbedCreateSpec embed) {
				embed.setColor(Color.CINNABAR);
				embed.setTitle("Current playing");
				if(BroadcastRunner.currentPlaying!=null) {
					embed.setDescription(BroadcastRunner.currentPlaying.getName());
				} else {
					embed.setDescription("Nothing playing");
				}
			}
			
		}).then();
						
	}

}
