package com.gmail.berndivader.streamserver.discord;

import java.time.Duration;

import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.discord.command.Commands;
import com.gmail.berndivader.streamserver.discord.permission.Permissions;
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
		        ANSI.println("[YELLOW][Try to connect to Discord...][RESET]");
		    })
		    .doOnSuccess(c->{
		        status=Status.CONNECTED;
		        ANSI.println("[GREEN][Connection to Discord OPEN!][RESET]");
		    })
		    .doOnError(err->{
		        status=Status.FAILED;
		        ANSI.printErr("Connection to Discord failed.",err);
		    }).block();
		
		if(status!=Status.CONNECTED) return;
		
		client.on(GuildCreateEvent.class).subscribe(event->{
			if(!Config.DISCORD_PERMITTED_GUILDS.containsKey(event.getGuild().getId().asLong())) event.getClient().logout().subscribe();
		});
		
		client.on(MessageCreateEvent.class)
	    	.filter(
	    			e->e.getMessage().getContent().startsWith(".")
	    			&&e.getMember().isPresent()
	    			&&e.getGuildId().isPresent()
	    			&&e.getMember().get().getRoleIds().contains(Snowflake.of(Config.DISCORD_ROLE_ID))
	    			&&Permissions.Guilds.permitted(e.getGuildId().get().asLong(),e.getMessage().getChannelId().asLong())
	    		)
		    .flatMap(e->{
		        Message message=e.getMessage();
		        String content=message.getContent();
		        String[]parse=content.split(" ",2);
		        String cmd=parse[0].toLowerCase().substring(1);

		        return Mono.justOrEmpty(Commands.instance.build(cmd))
		        		.flatMap(command->{
		        			String args=parse.length==2?parse[1]:"";
		        			return message.getChannel().flatMap(channel->command.exec(args,channel,e.getMember().get()));
		        		});

		    })
		    .onErrorContinue((throwable,object)->ANSI.printErr(throwable.getMessage(),throwable))
		    .subscribe();
		
		client.onDisconnect().doOnSuccess(t->{
		    ANSI.println("[YELLOW][Connection to Discord CLOSED!][RESET]");
		}).subscribe();
		
	}
	
	public void updateStatus(String comment) {
		if(Config.DEBUG) ANSI.println("Set status to: "+comment);
	    client.updatePresence(ClientPresence.of(discord4j.core.object.presence.Status.ONLINE,ClientActivity.custom(comment)))
	    	.onErrorContinue((throwable,object)->ANSI.printErr("Failed to update discord status",throwable))
	    	.subscribe();
	}
	
	public void close() {
		client.getGuilds().flatMap(Guild::getChannels)
				.filter(channel->channel.getName().equals(Config.DISCORD_VOICE_CHANNEL_NAME)).flatMap(GuildChannel::delete)
				.blockLast(Duration.ofSeconds(20));
		status=Status.DISCONNECTED;
		client.logout().block(Duration.ofSeconds(20));
	}
	
}
