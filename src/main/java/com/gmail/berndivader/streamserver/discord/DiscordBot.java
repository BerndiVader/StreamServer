package com.gmail.berndivader.streamserver.discord;

import java.time.Duration;

import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.discord.command.Command;
import com.gmail.berndivader.streamserver.discord.command.Commands;
import com.gmail.berndivader.streamserver.term.ANSI;

import discord4j.common.ReactorResources;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import io.netty.resolver.DefaultAddressResolverGroup;
import reactor.core.publisher.Mono;

public final class DiscordBot {
	
	public static DiscordBot instance;
	
	private final GatewayDiscordClient client;
	public static Status status;
	
	static {
		status=Status.DISCONNECTED;
	}
	
	public DiscordBot() {
		DiscordBot.status=Status.DISCONNECTED;
		
		Commands.instance=new Commands();
		
		ReactorResources reactor=ReactorResources.builder()
				.httpClient(ReactorResources.DEFAULT_HTTP_CLIENT.get()
						.resolver(DefaultAddressResolverGroup.INSTANCE))
				.build();
		
		client=DiscordClient.builder(Config.DISCORD_TOKEN)
			.setReactorResources(reactor)
			.build()
			.login()
		    .doOnSubscribe(sub->{
		        status=Status.CONNECTING;
		        ANSI.println("[Try to connect to Discord...]");
		    })
		    .doOnSuccess(c->{
		        status=Status.CONNECTED;
		        ANSI.println("[Connection to Discord OPEN!]");
		    })
		    .doOnError(err->{
		        status=Status.FAILED;
		        ANSI.printErr("Connection to Discord failed.",err);
		    }).block();
		
		if(status!=Status.CONNECTED) return;
		
		client.on(MessageCreateEvent.class)
			.filter(e->e.getMessage().getContent().startsWith(".")
					&&e.getMember().isPresent()
					&&e.getMember().get().getRoleIds().contains(Snowflake.of(Config.DISCORD_ROLE_ID)))
		    .flatMap(e->{
		        Message message=e.getMessage();
		        String content=message.getContent();
		        String[]parse=content.split(" ",2);
		        if(!parse[0].startsWith(".")) return Mono.empty();
		
		        String cmd=parse[0].toLowerCase().substring(1);
		        Command<?>command=Commands.instance.newCommandInstance(cmd);
		        if(command==null) return Mono.empty();
		        String args=parse.length==2?parse[1]:"";
		        
                return message.getChannel().flatMap(channel->{
                	if (channel==null) return Mono.empty();
                	return command.exec(args,channel);
                });
                
		    })
		    .onErrorContinue((throwable,object)->{
		    	ANSI.printErr(throwable.getMessage(),throwable);
		    }).subscribe();
		
		client.onDisconnect().doOnSuccess(t->{
		    status=Status.DISCONNECTED;
		    ANSI.println("[Connection to Discord CLOSED!]");
		}).subscribe();
		
	}
	
	public void updateStatus(String comment) {
		if(Config.DEBUG) ANSI.println("Set status to: "+comment);
	    client.updatePresence(ClientPresence.of(discord4j.core.object.presence.Status.ONLINE,ClientActivity.custom(comment))).doOnError(error->ANSI.printErr("Failed to update discord status",error)).subscribe();
	}
	
	public void close() {
		client.getGuilds().flatMap(Guild::getChannels)
				.filter(channel->channel.getName().equals(Config.DISCORD_CHANNEL)).flatMap(GuildChannel::delete)
				.blockLast(Duration.ofSeconds(20));
		client.logout().block(Duration.ofSeconds(20));
	}
	
}
