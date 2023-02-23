package com.gmail.berndivader.streamserver.discord.command.commands;

import java.util.function.Consumer;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.Utils;
import com.gmail.berndivader.streamserver.annotation.DiscordCommand;
import com.gmail.berndivader.streamserver.discord.command.Command;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

@DiscordCommand(name="play")
public class Play extends Command<Message> {
	
	int index=-1;
	
	@Override
<<<<<<< HEAD
	public Mono<Void> execute(String s,MessageChannel channel) {
		
=======
	public Mono<Message> execute(String s,MessageChannel channel) {
		
		Mono<Message>mono=Mono.empty();
		
		System.err.println(s);
		
>>>>>>> a25f33e71db747c369d305e9a33fc4d3154e2a7f
		if(s.toLowerCase().equals("next")) {
			BroadcastRunner.playNext();
		} else if(s.toLowerCase().equals("last")) {
			BroadcastRunner.playPrevious();
		} else if(s.toLowerCase().equals("repeat")) {
			BroadcastRunner.restartStream();
		} else {
			index=Utils.getFilePosition(s);
			if(index==-1) {
				index=Utils.getCustomFilePosition(s);
				if(index>-1) {
					BroadcastRunner.broadcastFilename(Helper.customs[index]);
				}
			} else {
				BroadcastRunner.broadcastFilename(Helper.files[index]);
<<<<<<< HEAD
			}
		}
		if(index!=-1) {
			
			return channel.createEmbed(new Consumer<EmbedCreateSpec>() {

				@Override
				public void accept(EmbedCreateSpec embed) {
					embed.setColor(Color.CINNABAR);
					embed.setTitle("Now playing");
					embed.setDescription(s);
				}
				
			}).then();
			
		}
		return channel.createEmbed(new Consumer<EmbedCreateSpec>() {

			@Override
			public void accept(EmbedCreateSpec embed) {
				embed.setColor(Color.RED);
				embed.setTitle("No file found for");
				embed.setDescription(s);
=======
>>>>>>> a25f33e71db747c369d305e9a33fc4d3154e2a7f
			}
		}
		if(index!=-1) {
			
<<<<<<< HEAD
		}).then();
=======
			mono=channel.createEmbed(new Consumer<EmbedCreateSpec>() {

				@Override
				public void accept(EmbedCreateSpec embed) {
					embed.setColor(Color.CINNABAR);
					embed.setTitle("Now playing");
					embed.setDescription(s);
				}
				
			});
			
		} else {
			
			mono=channel.createEmbed(new Consumer<EmbedCreateSpec>() {

				@Override
				public void accept(EmbedCreateSpec embed) {
					embed.setColor(Color.RED);
					embed.setTitle("No file found for");
					embed.setDescription(s);
				}
				
			});
			
		}
>>>>>>> a25f33e71db747c369d305e9a33fc4d3154e2a7f
		
		return mono;
				
	}


}
