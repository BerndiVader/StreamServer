package com.gmail.berndivader.streamserver.discord;

import java.time.Duration;
import java.util.Optional;

import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.discord.action.ButtonAction;
import com.gmail.berndivader.streamserver.discord.command.Command;
import com.gmail.berndivader.streamserver.discord.command.Commands;
import com.gmail.berndivader.streamserver.discord.musicplayer.MusicPlayer;
import com.gmail.berndivader.streamserver.discord.musicplayer.DiscordAudioProvider;
import com.gmail.berndivader.streamserver.discord.musicplayer.TrackScheduler;
import com.gmail.berndivader.streamserver.discord.permission.Permissions;
import com.gmail.berndivader.streamserver.term.ANSI;

import discord4j.common.ReactorResources;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import io.netty.resolver.DefaultAddressResolverGroup;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

public final class DiscordBot {
	
	public static DiscordBot instance;
	public static GatewayDiscordClient client;
	public static Status status;
	
	public DiscordAudioProvider provider;
	
	static {
		status=Status.DISCONNECTED;
	}
	
	public DiscordBot() {
		DiscordBot.status=Status.DISCONNECTED;
		if(Config.DISCORD.MUSIC_BOT) provider=MusicPlayer.create();
		
		Commands.instance=new Commands();
		
		ReactorResources reactor=ReactorResources.builder()
				.httpClient(ReactorResources.DEFAULT_HTTP_CLIENT.get()
				.resolver(DefaultAddressResolverGroup.INSTANCE))
				.build();

		client=DiscordClient.builder(Config.DISCORD.TOKEN)
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
		        ANSI.error("Connection to Discord failed.",err);
		    }).block();
		
		if(status!=Status.CONNECTED) return;
		
		client.on(GuildCreateEvent.class)
			.filter(event->Config.DISCORD.PERMITTED_GUILDS.containsKey(event.getGuild().getId().asLong()))
			.flatMap(event->event.getGuild().getChannels())
			.filter(channel->Config.DISCORD.MUSIC_BOT&&channel.getType()==Channel.Type.GUILD_VOICE&&channel.getName().equals(Config.DISCORD.VOICE_CHANNEL_NAME))
			.cast(VoiceChannel.class)
			.flatMap(voice->{

				return voice.join().withProvider(provider).doOnSuccess(vc->{
					
					ANSI.println("Create musicplayer....");
					provider.player().addListener(new TrackScheduler(voice));
					if(Config.DISCORD.MUSIC_AUTOPLAY) MusicPlayer.playRandomMusic();
					
				}).retryWhen(Retry.backoff(1024l,Duration.ofSeconds(1)).maxBackoff(Duration.ofSeconds(10)))
				.onErrorContinue((error,o)->{
					
					voice.getVoiceConnection().subscribe(vc->vc.reconnect().subscribe());
					ANSI.error("Error occured while voicechannel join.",error);
					
			    });

			}).subscribe();
		
		if(Config.DISCORD.MUSIC_BOT) {
			client.on(VoiceStateUpdateEvent.class)
			.filter(event->(event.isJoinEvent()||event.isMoveEvent())&&!event.getCurrent().getUserId().equals(client.getSelfId()))
			.flatMap(event->{
				
				return event.getCurrent().getMember().doOnSuccess(member->{
					member.getVoiceState().flatMap(voiceState->voiceState.getChannel()).subscribe(channel -> {
						
						if(channel.getName().equalsIgnoreCase(Config.DISCORD.VOICE_CHANNEL_NAME)) {
							PermissionOverwrite overrite=PermissionOverwrite.forMember(member.getId(),null,PermissionSet.of(Permission.SPEAK,Permission.STREAM));
							channel.addMemberOverwrite(member.getId(),overrite).subscribe();
						}
						
					});
				});
				
			}).subscribe();		
		}
		
		client.on(MessageCreateEvent.class)
	    	.filter(
	    			e->e.getMessage().getContent().startsWith(Config.DISCORD.CMD_PREFIX)
	    			&&e.getMember().isPresent()
	    			&&e.getGuildId().isPresent()
	    			&&(Config.DISCORD.ROLE_ID==0l||e.getMember().get().getRoleIds().contains(Snowflake.of(Config.DISCORD.ROLE_ID)))
	    			&&Permissions.Guilds.permitted(e.getGuildId().get().asLong(),e.getMessage().getChannelId().asLong())
	    		)
		    .flatMap(e->{
		        Message message=e.getMessage();
		        String content=message.getContent();
		        String[]parse=content.split(" ",2);
		        String cmd=parse[0].toLowerCase().substring(1);

		        Optional<Command<?>>opt=Commands.instance.build(cmd);
		        if(Config.DISCORD.DELETE_CMD_MESSAGE&&opt.isPresent()) message.delete().subscribe();
		        return Mono.justOrEmpty(opt)
		        		.flatMap(command->{
		        			String args=parse.length==2?parse[1]:"";
		        			return message.getChannel().flatMap(channel->command.execute(args,channel,e.getMember().get()));
		        		});

		    })
		    .onErrorContinue((throwable,object)->ANSI.error("There was an issue within message create event.",throwable))
		    .subscribe();
		
		client.on(ButtonInteractionEvent.class,ButtonAction::process).subscribe();
		
		client.onDisconnect().doOnSuccess(t->{
		    ANSI.println("[YELLOW][Connection to Discord CLOSED!][RESET]");
		}).subscribe();
		
	}
	
	public void updateStatus(String comment) {
		if(Config.DEBUG) ANSI.info("Set status to: "+comment);
	    client.updatePresence(ClientPresence.of(discord4j.core.object.presence.Status.ONLINE,ClientActivity.custom(comment)))
	    	.onErrorContinue((throwable,object)->ANSI.error("Failed to update discord status",throwable))
	    	.subscribe();
	}
	
	public void close() {
		status=Status.DISCONNECTED;
		client.logout().block(Duration.ofSeconds(20));
	}
	
}
