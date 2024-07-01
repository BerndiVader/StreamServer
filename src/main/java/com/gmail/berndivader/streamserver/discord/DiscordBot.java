package com.gmail.berndivader.streamserver.discord;

import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.reactivestreams.Subscription;

import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.discord.command.Command;
import com.gmail.berndivader.streamserver.discord.command.Commands;
import com.gmail.berndivader.streamserver.term.ANSI;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.json.gateway.StatusUpdate;
import reactor.core.publisher.Mono;

public final class DiscordBot {
	
	public static DiscordBot instance;
	
	private final GatewayDiscordClient client;
	private EventDispatcher dispatcher;
	public static Status status;
	
	static {
		status=Status.DISCONNECTED;
	}
	
	public DiscordBot() {
		instance=this;
		DiscordBot.status=Status.DISCONNECTED;
		
		new Commands();
		
		client=DiscordClientBuilder.create(Config.DISCORD_TOKEN).build().login()
				.doOnSubscribe(new Consumer<Subscription>() {

					@Override
					public void accept(Subscription t) {
						status=Status.CONNECTING;
						ANSI.println("[Try to connect to Discord...]");
					}
					
				})
			.doOnSuccess(new Consumer<GatewayDiscordClient>() {
	
				@Override
				public void accept(GatewayDiscordClient t) {
					status=Status.CONNECTED;
					ANSI.println("[Connection to Discord OPEN!]");
				}
				
			})
			.doOnError(new Consumer<Throwable>() {
	
				@Override
				public void accept(Throwable error) {
					status=Status.FAILED;
					ANSI.printErr("Connection to Discord failed.",error);
				}
				
		}).block();
		
		if(status!=Status.CONNECTED) return;
		
		dispatcher=client.getEventDispatcher();
		dispatcher.on(MessageCreateEvent.class)
			.flatMap(new Function<MessageCreateEvent,Mono<Void>>() {

				@Override
				public Mono<Void> apply(MessageCreateEvent e) {
					
					boolean isCommand=e.getMessage().getContent().toLowerCase().startsWith(Config.DISCORD_COMMAND_PREFIX);
					if(!isCommand||!e.getMember().isPresent()) return Mono.empty();
					
					Message message=e.getMessage();
					String content=message.getContent();
					String[]parse=content.split(" ",2);
					String temp=parse.length==2?parse[1]:"";
					String[]args=temp.split(" ",2);
					Command<?>command=Commands.instance.newCommandInstance(args[0].toLowerCase());
					
					if(command==null) return Mono.empty();
					
					return Mono.just(e).flatMap(new Function<MessageCreateEvent, Mono<Void>>() {

						@Override
						public Mono<Void> apply(MessageCreateEvent event) {

							if(!event.getMember().get().getRoles().filter(new Predicate<Role>() {

								@Override
								public boolean test(Role role) {
									return role.getName().equals(Config.DISCORD_ROLE);
								}
								
							}).collectList().block().isEmpty()) {

								Mono<? extends MessageChannel>mono=Config.DISCORD_RESPONSE_TO_PRIVATE?message.getAuthor().get().getPrivateChannel():message.getChannel();

								mono.subscribe(new Consumer<MessageChannel>() {

									@Override
									public void accept(MessageChannel channel) {
										if(channel==null) return;
										command.execute(args.length==2?args[1]:"",channel).subscribe();
										updateStatus(content);
									}
									
								});
							}
							
							return Mono.empty();
						}
					});
				}
				
			}).subscribe();
		
		client.onDisconnect().doOnSuccess(new Consumer<Void>() {

			@Override
			public void accept(Void t) {
				status=Status.DISCONNECTED;
				ANSI.println("[Connection to Discord CLOSED!]");
			}
			
		}).subscribe();
	}
	
	public void updateStatus(String comment) {
		client.updatePresence(StatusUpdate.builder().afk(false).since(0l).status("!".concat(comment)).build()).subscribe();
		return;
	}
	
	public void close() {
		
		client.getGuilds().collectList()
			.doOnSuccess(new Consumer<List<Guild>>() {

				@Override
				public void accept(List<Guild> guilds) {
					
					for(int i1=0;i1<guilds.size();i1++) {
						Guild guild=guilds.get(i1);
						guild.getChannels().collectList()
							.doOnSuccess(new Consumer<List<GuildChannel>>() {

								@Override
								public void accept(List<GuildChannel> channels) {
									
									for(int i1=0;i1<channels.size();i1++) {
										GuildChannel channel=channels.get(i1);
										if(channel.getName().equals(Config.DISCORD_CHANNEL)) {
											channel.delete().subscribe();
										}
									}
									
								}
								
							})
						.block(Duration.ofSeconds(20));
					}
					
				}
				
			}).block(Duration.ofSeconds(20));
		
		client.logout().block(Duration.ofSeconds(20));
	}
	
}
