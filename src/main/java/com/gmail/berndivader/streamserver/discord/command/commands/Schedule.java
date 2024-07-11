package com.gmail.berndivader.streamserver.discord.command.commands;

import java.util.function.Consumer;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.annotation.DiscordCommand;
import com.gmail.berndivader.streamserver.annotation.Requireds;
import com.gmail.berndivader.streamserver.discord.command.Command;
import com.gmail.berndivader.streamserver.mysql.AddScheduled;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

@DiscordCommand(name="schedule",usage="[filename] -> Add file to schedule table.",requireds={Requireds.BROADCASTRUNNER})
public class Schedule extends Command<Message> {

	@Override
	public Mono<Message> execute(String p, MessageChannel channel) {
		
		int index=Helper.getFilePosition(p);
		if(index==-1) {
			index=Helper.getCustomFilePosition(p);
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

			});
		}
		return channel.createEmbed(new Consumer<EmbedCreateSpec>() {

			@Override
			public void accept(EmbedCreateSpec embed) {
				embed.setColor(Color.RED);
				embed.setTitle("No file found for");
				embed.setDescription(p);
			}

		});
				
	}
}
