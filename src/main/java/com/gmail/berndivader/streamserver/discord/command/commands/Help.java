package com.gmail.berndivader.streamserver.discord.command.commands;

import com.gmail.berndivader.streamserver.annotation.DiscordCommand;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.discord.command.Command;
import com.gmail.berndivader.streamserver.discord.command.Commands;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

@DiscordCommand(name="help",usage="Show Help.")
public class Help extends Command<Message> {
	
	@Override
	public Mono<Message> execute(String string,MessageChannel channel) {
		
		return channel.createMessage(msg->{
			msg.addEmbed(embed->{
				embed.setTitle("StreamServer discord help");
				embed.setColor(Color.GREEN);
				
				StringBuilder builder=new StringBuilder().append(Config.DISCORD_HELP_TEXT);
				
				Commands.instance.commands.forEach((name,clazz)->{
					DiscordCommand a=clazz.getDeclaredAnnotation(DiscordCommand.class);
					if(a!=null) builder.append(a.name().concat(" - ").concat(a.usage()).concat("\n"));
				});
				embed.setDescription(builder.toString());
				
			});
		});
		
	}

}
