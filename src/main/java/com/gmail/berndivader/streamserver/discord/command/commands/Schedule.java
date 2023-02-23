package com.gmail.berndivader.streamserver.discord.command.commands;

import java.util.function.Consumer;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.Utils;
import com.gmail.berndivader.streamserver.annotation.DiscordCommand;
import com.gmail.berndivader.streamserver.discord.command.Command;
import com.gmail.berndivader.streamserver.mysql.AddScheduled;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

@DiscordCommand(name="schedule")
public class Schedule extends Command<Message> {

	@Override
<<<<<<< HEAD
	public Mono<Void> execute(String p, MessageChannel channel) {
		
=======
	public Mono<Message> execute(String p, MessageChannel channel) {
		
		Mono<Message>mono=Mono.empty();
		
>>>>>>> a25f33e71db747c369d305e9a33fc4d3154e2a7f
		int index=Utils.getFilePosition(p);
		if(index==-1) {
			index=Utils.getCustomFilePosition(p);
			if(index>-1) {
				new AddScheduled(Helper.customs[index].getName());
<<<<<<< HEAD
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
=======
>>>>>>> a25f33e71db747c369d305e9a33fc4d3154e2a7f
			}
		} else {
			new AddScheduled(p);
		}
		if(index!=-1) {
			mono=channel.createEmbed(new Consumer<EmbedCreateSpec>() {

<<<<<<< HEAD
		}).then();
=======
				@Override
				public void accept(EmbedCreateSpec embed) {
					embed.setColor(Color.BROWN);
					embed.setTitle("Scheduled");
					embed.setDescription("```"+p+"```");
				}

			});
		} else {
			mono=channel.createEmbed(new Consumer<EmbedCreateSpec>() {

				@Override
				public void accept(EmbedCreateSpec embed) {
					embed.setColor(Color.RED);
					embed.setTitle("No file found for");
					embed.setDescription("```"+p+"```");
				}

			});
		}
		
		return mono;
>>>>>>> a25f33e71db747c369d305e9a33fc4d3154e2a7f
				
	}
}
