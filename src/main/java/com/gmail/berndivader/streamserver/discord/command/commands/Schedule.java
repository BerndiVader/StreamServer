package com.gmail.berndivader.streamserver.discord.command.commands;

import java.io.File;
import java.util.Optional;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.annotation.DiscordCommand;
import com.gmail.berndivader.streamserver.annotation.Requireds;
import com.gmail.berndivader.streamserver.discord.command.Command;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;
import com.gmail.berndivader.streamserver.ffmpeg.FFProbePacket;
import com.gmail.berndivader.streamserver.mysql.AddScheduled;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

@DiscordCommand(name="schedule",usage="[filename] -> Add file to schedule table.",requireds={Requireds.BROADCASTRUNNER})
public class Schedule extends Command<Message> {
	
	@Override
	public Mono<Message> execute(String name, MessageChannel channel) {
		
		Optional<File>opt=BroadcastRunner.getFileByName(name);
		Mono<Message>mono=Mono.empty();
		
		if(opt.isPresent()) {
			new AddScheduled(opt.get().getName());
			final FFProbePacket packet=Helper.createProbePacket(opt.get());
			mono=channel.createMessage(EmbedCreateSpec.builder()
					.title(packet.isSet(packet.tags.title)?packet.tags.title:"Scheduled...")
					.author(packet.isSet(packet.tags.artist)?packet.tags.artist:"","","")
					.description(packet.isSet(packet.tags.description)?packet.tags.description:opt.get().getName())
					.addField("Duration",Helper.stringFloatToTime(packet.duration),false)
					.build());
		} else {
			mono=channel.createMessage(EmbedCreateSpec.builder()
					.color(Color.RED)
					.title("No file found for")
					.description(name)
					.build());
		}
		
		return mono;
				
	}
}
