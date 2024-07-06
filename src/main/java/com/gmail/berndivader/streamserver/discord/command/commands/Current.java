package com.gmail.berndivader.streamserver.discord.command.commands;

import java.util.function.Consumer;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.annotation.DiscordCommand;
import com.gmail.berndivader.streamserver.annotation.Requireds;
import com.gmail.berndivader.streamserver.discord.command.Command;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;
import com.gmail.berndivader.streamserver.ffmpeg.FFProbePacket;
import com.gmail.berndivader.streamserver.term.ANSI;

import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

@DiscordCommand(name="current",usage="Display current playing file.",requireds={Requireds.BROADCASTRUNNER})
public class Current extends Command<Void> {
	private FFProbePacket packet;
	
	public Current() {
		packet=new FFProbePacket();
	}

	@Override
	public Mono<Void> execute(String string, MessageChannel channel) {
		if(BroadcastRunner.currentPlaying!=null) packet=Helper.getProbePacket(BroadcastRunner.currentPlaying);
		return channel.createEmbed(new Consumer<EmbedCreateSpec>() {

			@Override
			public void accept(EmbedCreateSpec embed) {
				embed.setColor(Color.CINNABAR);
				if(BroadcastRunner.currentPlaying!=null) {
					embed.setTitle(packet.isSet(packet.format.tags.title)?packet.format.tags.title:"");
					embed.setAuthor(packet.isSet(packet.format.tags.title)?packet.format.tags.title:"","","");
					embed.setDescription(packet.isSet(packet.format.tags.description)?packet.format.tags.description:"");
					embed.addField("Duration",packet.isSet(packet.format.duration)?packet.format.duration:"",false);
				} else {
					embed.setDescription("No media streaming.");
				}
			}
			
		}).then();
						
	}

}
