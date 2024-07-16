package com.gmail.berndivader.streamserver.discord.command.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import com.gmail.berndivader.streamserver.annotation.DiscordCommand;
import com.gmail.berndivader.streamserver.discord.command.Command;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@DiscordCommand(name="playlist",usage="[filter] -> Search for files.")
public class Playlist extends Command<List<Message>> {
	
	@Override
	public Mono<List<Message>> execute(String command,MessageChannel channel) {
		String regex=command.toLowerCase();
		
		List<String>list=BroadcastRunner.getFilesAsList(regex);
		int filesSize=list.size();
		
		List<String>messages=new ArrayList<String>();
		StringBuilder builder=new StringBuilder();
		list.forEach(name->{
			if(builder.length()+name.length()<1975) {
				builder.append(name+"\n");
			} else {
				messages.add(builder.toString());
				builder.setLength(0);
			}
		});
		messages.add(builder.toString());
		
		List<Mono<Message>>monos=new ArrayList<Mono<Message>>();
		IntStream.range(0,messages.size()).mapToObj(i->{
			
			return channel.createMessage(EmbedCreateSpec.builder()
					.color(Color.CINNABAR)
					.title(i==0?"Playlist result":"Playlist continue")
					.description("```"+messages.get(i)+"```")
					.footer(i==messages.size()-1?"Found ".concat(Integer.toString(filesSize)).concat(" matches"):"",null)
					.build());
			
		}).forEach(monos::add);
		
		return Flux.fromIterable(monos).flatMap(mono->mono).collectList();		
	}

}
