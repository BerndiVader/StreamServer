package com.gmail.berndivader.streamserver.discord;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.function.Function;

import com.gmail.berndivader.streamserver.Utils;

import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

public class Commands {

	public static Mono<Void> getPlaylist(String filter,MessageChannel channel) {
		
		AbstractMap.SimpleEntry<String,MessageChannel>pair=new AbstractMap.SimpleEntry<String, MessageChannel>(filter,channel);
		return Mono.just(pair).map(new Function<AbstractMap.SimpleEntry<String,MessageChannel>,Void>() {

			@Override
			public Void apply(SimpleEntry<String, MessageChannel> p) {

				String list=Utils.getPlaylistAsString(p.getKey());
				p.getValue().createMessage(list).subscribe();
				return null;
				
			}
		});
		
	}

}
