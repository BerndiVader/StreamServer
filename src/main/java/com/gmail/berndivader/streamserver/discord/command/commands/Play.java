package com.gmail.berndivader.streamserver.discord.command.commands;

import java.io.File;
import java.util.Optional;

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

@DiscordCommand(name="play",usage="[filename|next|last|repeat]",requireds={Requireds.BROADCASTRUNNER})
public class Play extends Command<Message> {
		
	@Override
	public Mono<Message> execute(String s,MessageChannel channel) {
		
		Mono<Message>mono=Mono.empty();
		
		
		if(s.toLowerCase().equals("next")) {
			File file=BroadcastRunner.getFiles()[BroadcastRunner.index.get()];
			mono=createMessage(file,channel);
			BroadcastRunner.next();
		} else if(s.toLowerCase().equals("prev")) {
			File file=BroadcastRunner.getFiles()[(BroadcastRunner.index.get()-2+BroadcastRunner.getFiles().length)%BroadcastRunner.getFiles().length];
			mono=createMessage(file,channel);
			BroadcastRunner.previous();
		} else if(s.toLowerCase().equals("repeat")) {
			if(BroadcastRunner.playing()!=null) {
				mono=createMessage(BroadcastRunner.playing(),channel);
				BroadcastRunner.restart();
			} else {
				File file=BroadcastRunner.getFiles()[BroadcastRunner.index.get()];
				mono=createMessage(file,channel);
				BroadcastRunner.next();
			}
		} else {
			final Optional<File>file=BroadcastRunner.getFileByName(s);
			if(file.isPresent()) {
				mono=createMessage(file.get(),channel);
				BroadcastRunner.playFile(file.get());
			} else {
				mono=channel.createMessage(EmbedCreateSpec.builder()
					.color(Color.RED)
					.title("No file found for")
					.description(s)
					.build());
			}
		}
		return mono;
	}
	
	private static Mono<Message> createMessage(File file,MessageChannel channel) {
		final FFProbePacket packet=Helper.createProbePacket(file);
		
		return channel.createMessage(EmbedCreateSpec.builder()
				.title(packet.isSet(packet.tags.title)?packet.tags.title:"Now playing...")
				.author(packet.isSet(packet.tags.artist)?packet.tags.artist:"","","")
				.description(packet.isSet(packet.tags.description)?packet.tags.description:file.getName())
				.addField("Duration",Helper.stringFloatToTime(packet.duration),false)
				.build());
	}


}
