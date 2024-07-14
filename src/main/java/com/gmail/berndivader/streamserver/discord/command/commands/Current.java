package com.gmail.berndivader.streamserver.discord.command.commands;

import java.util.function.Consumer;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.annotation.DiscordCommand;
import com.gmail.berndivader.streamserver.annotation.Requireds;
import com.gmail.berndivader.streamserver.discord.command.Command;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;
import com.gmail.berndivader.streamserver.ffmpeg.FFProbePacket;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

@DiscordCommand(name="current",usage="Display current playing file.",requireds={Requireds.BROADCASTRUNNER})
public class Current extends Command<Message> {
	private FFProbePacket packet;
	
	public Current() {
		packet=new FFProbePacket();
	}

	@Override
	public Mono<Message> execute(String string, MessageChannel channel) {
		if(BroadcastRunner.playling!=null) packet=Helper.getProbePacket(BroadcastRunner.playling);
		return channel.createEmbed(new Consumer<EmbedCreateSpec>() {

			@Override
			public void accept(EmbedCreateSpec embed) {
				embed.setColor(Color.CINNABAR);
				if(BroadcastRunner.playling!=null) {
					embed.setTitle(packet.isSet(packet.tags.title)?packet.tags.title:"");
					embed.setAuthor(packet.isSet(packet.tags.artist)?packet.tags.artist:"","","");
					embed.setDescription(packet.isSet(packet.tags.description)?packet.tags.description:"");
					embed.addField("Duration",packet.isSet(packet.duration)?packet.duration:"",false);
				} else {
					embed.setDescription("No media streaming.");
				}
			}
			
		}).doOnError(error->{
			channel.createEmbed(embed->{
				embed.setTitle("Error");
				embed.setColor(Color.RED);
				embed.setDescription("Something went wrong while gathering media info.");
			}).subscribe();
		});
	}

}
