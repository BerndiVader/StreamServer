package com.gmail.berndivader.streamserver.discord.command.commands;

import java.io.File;
import java.util.Optional;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.annotation.DiscordCommand;
import com.gmail.berndivader.streamserver.annotation.Requireds;
import com.gmail.berndivader.streamserver.discord.command.Command;
import com.gmail.berndivader.streamserver.discord.permission.Permission;
import com.gmail.berndivader.streamserver.discord.permission.User.Rank;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;
import com.gmail.berndivader.streamserver.ffmpeg.FFProbePacket;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

@Permission(required=Rank.MEMBER)
@DiscordCommand(name="play",usage="[filename|next|last|repeat]",requireds={Requireds.BROADCASTRUNNER})
public class Play extends Command<Message> {
		
	@Override
	public Mono<Message> exec() {
		
		Mono<Message>mono=Mono.empty();
		String s=string;
		
		switch(s.toLowerCase()) {
		case "next":
			mono=createMessage(BroadcastRunner.getFiles()[BroadcastRunner.index.get()],channel);
			BroadcastRunner.next();
			break;
		case "prev":
			mono=createMessage(
					BroadcastRunner.getFiles()[(BroadcastRunner.index.get()-2+BroadcastRunner.getFiles().length)%BroadcastRunner.getFiles().length],
					channel
					);
			BroadcastRunner.previous();
			break;
		case "repeat":
			if(BroadcastRunner.playing()!=null) {
				mono=createMessage(BroadcastRunner.playing(),channel);
				BroadcastRunner.restart();
			} else {
				mono=createMessage(BroadcastRunner.getFiles()[BroadcastRunner.index.get()],channel);
				BroadcastRunner.next();
			}
			break;
		default:
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
			break;
		}
		
		return mono;
	}
	
	private static Mono<Message> createMessage(File file,MessageChannel channel) {
		final FFProbePacket packet=FFProbePacket.build(file);
		
		return channel.createMessage(EmbedCreateSpec.builder()
				.title(packet.isSet(packet.tags.title)?packet.tags.title:"Now playing...")
				.author(packet.isSet(packet.tags.artist)?packet.tags.artist:"","","")
				.description(packet.isSet(packet.tags.description)?packet.tags.description:file.getName())
				.addField("Duration",Helper.stringFloatToTime(packet.duration),false)
				.build());
	}


}
