package com.gmail.berndivader.streamserver.discord.command.commands;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import com.gmail.berndivader.streamserver.annotation.DiscordCommand;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.discord.command.Command;
import com.gmail.berndivader.streamserver.term.ANSI;
import com.gmail.berndivader.streamserver.youtube.Youtube;
import com.gmail.berndivader.streamserver.youtube.packets.EmptyPacket;
import com.gmail.berndivader.streamserver.youtube.packets.LiveStreamPacket;
import com.gmail.berndivader.streamserver.youtube.packets.Packet;

import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

@DiscordCommand(name="info",usage="Show stream status.")
public class LivestreamInfo extends Command<Void> {
	
	private Packet packet;

	@Override
	public Mono<Void> execute(String string, MessageChannel channel) {
		
		packet=new EmptyPacket();
		Future<Packet>future=Youtube.livestreamsByChannelId(Config.YOUTUBE_CHANNEL_ID);
		try {
			packet=future.get(15l,TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			ANSI.printErr(e.getMessage(),e);
		}
		
		return channel.createEmbed(new Consumer<EmbedCreateSpec>() {

			@Override
			public void accept(EmbedCreateSpec embed) {
				
				if(packet instanceof LiveStreamPacket) {
					LiveStreamPacket p=(LiveStreamPacket)packet;
					embed.setColor(Color.CINNABAR);
					embed.setAuthor(p.snippet.channelTitle,"https://www.youtube.com/channel/"+p.snippet.channelId,p.snippet.thumbnails.low.url);
					embed.setImage(p.snippet.thumbnails.medium.url);
					embed.setUrl("https://www.youtube.com/watch?v="+p.id.videoId);
					embed.setDescription(p.snippet.description);
					embed.setFooter("Broadcastcontent: "+p.snippet.liveBroadcastContent,p.snippet.thumbnails.low.url);
					embed.setThumbnail(p.snippet.thumbnails.low.url);
					embed.setTitle(p.snippet.title);					
				} else {
					embed.setTitle("Error");
					embed.setColor(Color.RED);
					embed.setDescription("Unable to get livestream status from YT.");
				}
				
			}
			
		}).then();
		
	}

}
