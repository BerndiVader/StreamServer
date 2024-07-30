package com.gmail.berndivader.streamserver.discord.command.commands;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.gmail.berndivader.streamserver.annotation.DiscordCommand;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.discord.command.Command;
import com.gmail.berndivader.streamserver.term.ANSI;
import com.gmail.berndivader.streamserver.youtube.Youtube;
import com.gmail.berndivader.streamserver.youtube.packets.EmptyPacket;
import com.gmail.berndivader.streamserver.youtube.packets.ErrorPacket;
import com.gmail.berndivader.streamserver.youtube.packets.LiveStreamPacket;
import com.gmail.berndivader.streamserver.youtube.packets.Packet;
import com.google.gson.JsonObject;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

@DiscordCommand(name="liveinfo",usage="Info about the current running livestream.")
public class LivestreamInfo extends Command<Message> {
	
	private Packet packet;

	@Override
	public Mono<Message> execute(String string, MessageChannel channel) {
		
		packet=Packet.build(new JsonObject(),EmptyPacket.class);
		Future<Packet>future=Youtube.livestreamsByChannelId(Config.YOUTUBE_CHANNEL_ID);
		try {
			packet=future.get(15l,TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			ANSI.printErr(e.getMessage(),e);
		}
		
		EmbedCreateSpec.Builder builder=EmbedCreateSpec.builder();
		
		if(packet instanceof LiveStreamPacket) {
			LiveStreamPacket p=(LiveStreamPacket)packet;
			builder.color(Color.CINNABAR)
				.author(p.snippet.channelTitle,"https://www.youtube.com/channel/"+p.snippet.channelId,p.snippet.thumbnails.low.url)
				.image(p.snippet.thumbnails.medium.url)
				.url("https://www.youtube.com/watch?v="+p.id.videoId)
				.description(p.snippet.description)
				.footer("Broadcastcontent: "+p.snippet.liveBroadcastContent,p.snippet.thumbnails.low.url)
				.thumbnail(p.snippet.thumbnails.low.url)
				.title(p.snippet.title);
		} else if(packet instanceof ErrorPacket) {
			ErrorPacket p=(ErrorPacket)packet;
			builder.title("ERROR")
				.color(Color.RED)
				.title(p.code+":"+p.status)
				.description(p.message);
		} else {
			builder.title("WARNING")
				.color(Color.ORANGE)
				.description("Youtube didnt answer the request.");
		}
		
		return channel.createMessage(builder.build());
				
	}

}
