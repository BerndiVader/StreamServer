package com.gmail.berndivader.streamserver.discord.command.commands;

import java.util.function.Consumer;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.Utils;
import com.gmail.berndivader.streamserver.annotation.DiscordCommand;
import com.gmail.berndivader.streamserver.discord.command.Command;
import com.gmail.berndivader.streamserver.mysql.AddScheduled;

import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

@DiscordCommand(name="schedule")
public class Schedule extends Command<Void> {

	@Override
	public Mono<Void> execute(String p, MessageChannel channel) {
		
		int index=Utils.getFilePosition(p);
		if(index==-1) {
			index=Utils.getCustomFilePosition(p);
			if(index>-1) {
				new AddScheduled(Helper.customs[index].getName());
			}
		} else {
			new AddScheduled(p);
		}
		if(index!=-1) {
			return channel.createEmbed(new Consumer<EmbedCreateSpec>() {

				@Override
				public void accept(EmbedCreateSpec embed) {
					embed.setColor(Color.BROWN);
					embed.setTitle("Scheduled");
					embed.setDescription(p);
				}

			}).then();
		}
		return channel.createEmbed(new Consumer<EmbedCreateSpec>() {

			@Override
			public void accept(EmbedCreateSpec embed) {
				embed.setColor(Color.RED);
				embed.setTitle("No file found for");
				embed.setDescription(p);
			}

		}).then();
				
	}
}
