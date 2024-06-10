package com.gmail.berndivader.streamserver.discord.command.commands;

import java.util.function.Consumer;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.annotation.DiscordCommand;
import com.gmail.berndivader.streamserver.discord.command.Command;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;

import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

@DiscordCommand(name="play",usage="[filename|next|last|repeat]")
public class Play extends Command<Void> {
	
	int index=-1;
	
	@Override
	public Mono<Void> execute(String s,MessageChannel channel) {
		
		if(s.toLowerCase().equals("next")) {
			BroadcastRunner.playNext();
		} else if(s.toLowerCase().equals("last")) {
			BroadcastRunner.playPrevious();
		} else if(s.toLowerCase().equals("repeat")) {
			BroadcastRunner.restartStream();
		} else {
			index=Helper.getFilePosition(s);
			if(index==-1) {
				index=Helper.getCustomFilePosition(s);
				if(index>-1) {
					BroadcastRunner.broadcastFilename(Helper.customs[index]);
				}
			} else {
				BroadcastRunner.broadcastFilename(Helper.files[index]);
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
			}
			
		}).then();
		
	}


}
