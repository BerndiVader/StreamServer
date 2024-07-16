package com.gmail.berndivader.streamserver.discord.command.commands;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.annotation.DiscordCommand;
import com.gmail.berndivader.streamserver.annotation.Requireds;
import com.gmail.berndivader.streamserver.discord.command.Command;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;
import com.gmail.berndivader.streamserver.ffmpeg.FFProbePacket;
import com.gmail.berndivader.streamserver.term.ANSI;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.EmbedCreateSpec.Builder;
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
		if(BroadcastRunner.playing!=null) packet=Helper.createProbePacket(BroadcastRunner.playing);

		Builder embed=EmbedCreateSpec.builder();
		embed.color(Color.CINNABAR);
		if(BroadcastRunner.playing!=null) {
			embed.title(packet.tags.title)
			.author(packet.tags.artist,packet.tags.purl,null)
			.description(packet.tags.description)
			.addField("Duration",Helper.stringFloatToTime(packet.duration),false);
		} else {
			embed.description("No media streaming.");
		}
		
		return channel.createMessage(embed.build()).doOnError(error->{
			
			channel.createMessage(EmbedCreateSpec.builder()
					.title("ERROR")
					.color(Color.RED)
					.description("Something went wrong while gathering media info.")
					.build()
			).subscribe();
			ANSI.printErr(error.getMessage(),error);
		});
	}

}
