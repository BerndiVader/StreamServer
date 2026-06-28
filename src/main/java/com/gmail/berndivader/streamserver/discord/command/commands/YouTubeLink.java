package com.gmail.berndivader.streamserver.discord.command.commands;

import java.util.concurrent.TimeUnit;

import com.gmail.berndivader.streamserver.annotation.DiscordCommand;
import com.gmail.berndivader.streamserver.annotation.Requireds;
import com.gmail.berndivader.streamserver.discord.command.Command;
import com.gmail.berndivader.streamserver.discord.permission.Permission;
import com.gmail.berndivader.streamserver.youtube.Broadcast;
import com.gmail.berndivader.streamserver.youtube.BroadcastStatus;
import com.gmail.berndivader.streamserver.youtube.packets.LiveBroadcastPacket;
import com.gmail.berndivader.streamserver.youtube.packets.Packet;

import discord4j.core.object.entity.Message;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

@Permission
@DiscordCommand(name="link",usage="Display youtube stream url.",requireds={Requireds.BROADCASTRUNNER})
public class YouTubeLink extends Command<Message> {

	@Override
	public Mono<Message> exec() {
		
		try {
			Packet p=Broadcast.getLiveBroadcast(BroadcastStatus.active).get(15l,TimeUnit.SECONDS);
			if(p instanceof LiveBroadcastPacket) {
				LiveBroadcastPacket live=(LiveBroadcastPacket)p;
				String link="https://www.youtube.com/watch?v=".concat(live.id);
				return channel.createMessage(EmbedCreateSpec.builder()
						.title("Youtube link:")
						.color(Color.CINNABAR)
						.description(link)
						.build());			
			} else {
				return channel.createMessage(EmbedCreateSpec.builder()
						.title("Youtube link:")
						.color(Color.ORANGE)
						.description("There is currently no live broadcast on Youtube.")
						.build());
			}
		} catch(Exception e) {
			return channel.createMessage(EmbedCreateSpec.builder()
					.title("Youtube link:")
					.color(Color.ORANGE)
					.description("There is currently no live broadcast on Youtube.")
					.build());
		}
		
	}

}
