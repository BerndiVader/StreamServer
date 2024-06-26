package com.gmail.berndivader.streamserver.discord.command.commands;

import java.util.ArrayList;
import java.util.function.Consumer;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.annotation.DiscordCommand;
import com.gmail.berndivader.streamserver.discord.command.Command;

import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

@DiscordCommand(name="playlist",usage="[filter] -> Search for files.")
public class Playlist extends Command<Void> {
	
	@Override
	public Mono<Void> execute(String command,MessageChannel channel) {
		
		String regex=command.toLowerCase();
		boolean custom=regex.startsWith("custom ");
		if(custom) {
			regex=regex.replaceFirst("custom ","");
		}
		
		ArrayList<String>list=Helper.getFilelistAsList(regex);
		int size=list.size();
		
		ArrayList<String>messages=new ArrayList<String>();
		StringBuilder builder=new StringBuilder();
		
		for(int i1=0;i1<size;i1++) {
			String next=list.get(i1);
			if(builder.length()+next.length()<1975) {
				builder.append(next+"\n");
			} else {
				messages.add(builder.toString());
				builder.delete(0,builder.length());
			}
		}
		messages.add(builder.toString());
		size=messages.size();
		for(int i1=0;i1<size;i1++) {
			int i=i1;
			int s=size;
			int ls=list.size();
			String msg=messages.get(i1);
			
			channel.createEmbed(new Consumer<EmbedCreateSpec>() {

				@Override
				public void accept(EmbedCreateSpec embed) {
					embed.setColor(Color.CINNABAR);
					embed.setTitle(i==0?"Playlist result":"Playlist continue");
					embed.setDescription(msg);
					if(i==s-1) {
						embed.setFooter("Found ".concat(Integer.toString(ls)).concat(" matches"),null);
					}
				}
				
			}).subscribe();
		}
		return Mono.empty();
	}

}
