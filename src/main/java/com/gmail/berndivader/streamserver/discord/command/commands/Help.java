package com.gmail.berndivader.streamserver.discord.command.commands;

import com.gmail.berndivader.streamserver.annotation.DiscordCommand;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.discord.command.Command;
import com.gmail.berndivader.streamserver.discord.command.Commands;
import com.gmail.berndivader.streamserver.discord.permission.Permission;
import com.gmail.berndivader.streamserver.discord.permission.Permissions;
import com.gmail.berndivader.streamserver.discord.permission.User.Rank;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

@Permission
@DiscordCommand(name="help",usage="Show Help.")
public class Help extends Command<Message> {
	
	@Override
	public Mono<Message> execute(String string,MessageChannel channel,Member member) {

		if(Permissions.Users.permitted(member.getId().asLong(),this.getClass().getDeclaredAnnotation(Permission.class).required())) {
			Rank rank=Rank.GUEST;
			if(Config.DISCORD_PERMITTED_USERS.containsKey(member.getId().asLong())) {
				rank=Config.DISCORD_PERMITTED_USERS.get(member.getId().asLong()).rank;
			}
			return execute(string,channel,rank);
		} else {
			return Mono.empty();
		}
	}
		
	@Override
	protected Mono<Message> exec() {
		return null;
	}
	
	private Mono<Message>execute(String string,MessageChannel channel,Rank rank) {
		
		StringBuilder builder=new StringBuilder().append(Config.DISCORD_HELP_TEXT);
		Commands.instance.commands.forEach((name,clazz)->{
			
			DiscordCommand a=clazz.getDeclaredAnnotation(DiscordCommand.class);
			Permission perm=clazz.getDeclaredAnnotation(Permission.class);
			Rank required=perm!=null?perm.required():Rank.GUEST;
			if(a!=null&&rank.ordinal()>=required.ordinal()) builder.append(a.name().concat(" - ").concat(a.usage()).concat("\n"));
			
		});
		
		return channel.createMessage(MessageCreateSpec.builder()
				.addEmbed(EmbedCreateSpec.builder()
					.title(Config.DISCORD_HELP_TEXT_TITLE)
					.color(Color.GREEN)
					.description(builder.toString())
					.build())				
				.build());
		
	}		
}

