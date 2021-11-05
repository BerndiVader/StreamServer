package com.gmail.berndivader.streamserver.discord;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.reactivestreams.Subscription;

import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.console.ConsoleRunner;
import com.gmail.berndivader.streamserver.discord.command.Command;
import com.gmail.berndivader.streamserver.discord.command.Commands;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.playback.NonAllocatingAudioFrameBuffer;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.VoiceChannelCreateSpec;
import discord4j.core.spec.VoiceChannelJoinSpec;
import discord4j.rest.util.Color;
import discord4j.voice.AudioProvider;
import reactor.core.publisher.Mono;

public class DiscordBot {
	
	final AudioPlayerManager playerManager;
	public final AudioPlayer audioPlayer;
	final AudioProvider provider;
	final TrackScheduler scheduler;
	final Commands commands;
	
	final GatewayDiscordClient client;
	final EventDispatcher dispatcher;
	public static Status status;
	
	public DiscordBot() throws FileNotFoundException, IOException, ClassNotFoundException {
		commands=new Commands();
		playerManager=new DefaultAudioPlayerManager();
		playerManager.getConfiguration().setFrameBufferFactory(NonAllocatingAudioFrameBuffer::new);
		AudioSourceManagers.registerRemoteSources(playerManager);
		audioPlayer=playerManager.createPlayer();
		provider=new LavaPlayerAudioProvider(audioPlayer);
		scheduler=new TrackScheduler(audioPlayer);
		
		status=Status.DISCONNECTED;
		client=DiscordClientBuilder.create(Config.DISCORD_TOKEN).build().login()
				.doOnSubscribe(new Consumer<Subscription>() {

					@Override
					public void accept(Subscription t) {
						status=Status.CONNECTING;
						ConsoleRunner.println("[Try to connect to Discord...]");
					}
					
				})
			.doOnSuccess(new Consumer<GatewayDiscordClient>() {
	
				@Override
				public void accept(GatewayDiscordClient t) {
					status=Status.CONNECTED;
					ConsoleRunner.println("[Connection to Discord OPEN!]");
				}
				
			})
			.doOnError(new Consumer<Throwable>() {
	
				@Override
				public void accept(Throwable error) {
					status=Status.FAILED;
					ConsoleRunner.println(error.getMessage());
					ConsoleRunner.println("[Connection to Discord FAILED!]");
				}
				
			})
			.block();
		
		dispatcher=client.getEventDispatcher();
		
		dispatcher.on(ReadyEvent.class)
			.flatMap(new Function<ReadyEvent, Mono<Void>>() {
				
				@Override
				public Mono<Void> apply(ReadyEvent ready) {
					
					if(!ready.getGuilds().isEmpty()) {
						Iterator<discord4j.core.event.domain.lifecycle.ReadyEvent.Guild>iterator=ready.getGuilds().iterator();
						for(;iterator.hasNext();) {
							Snowflake flake=iterator.next().getId();
							client.getGuildById(flake)
								.doOnSuccess(new Consumer<Guild>() {

									@Override
									public void accept(Guild guild) {
										guild.getChannels()
											.filter(new Predicate<GuildChannel>() {

												@Override
												public boolean test(GuildChannel channel) {
													return channel.getName().equals(Config.DISCORD_CHANNEL)&&channel.getType().equals(Channel.Type.GUILD_VOICE);
												}
												
											})
											.collectList()
											.doOnSuccess(new Consumer<List<GuildChannel>>() {

												@Override
												public void accept(List<GuildChannel> channels) {
													if(channels.isEmpty()) {
														
														guild.createVoiceChannel(new Consumer<VoiceChannelCreateSpec>() {

															@Override
															public void accept(VoiceChannelCreateSpec spec) {
																spec.setName(Config.DISCORD_CHANNEL);
																spec.setUserLimit(99);
																spec.setBitrate(96000);
															}
															
														})
														.doOnSuccess(new Consumer<VoiceChannel>() {

															@Override
															public void accept(VoiceChannel voice) {
																voice.join(new Consumer<VoiceChannelJoinSpec>() {

																	@Override
																	public void accept(VoiceChannelJoinSpec t) {
																		t.setSelfMute(false);
																		t.setProvider(provider);
																		connectStream();
																	}
																	
																})
																.subscribe();
															}
															
														})
														.doOnError(new Consumer<Throwable>() {

															@Override
															public void accept(Throwable t) {
																ConsoleRunner.println(t.getMessage());
															}
															
														})
														.subscribe();
													} else {
														int size=channels.size();
														for(int i1=0;i1<size;i1++) {
															VoiceChannel voice=(VoiceChannel)channels.get(i1);
															voice.join(new Consumer<VoiceChannelJoinSpec>() {

																@Override
																public void accept(VoiceChannelJoinSpec t) {
																	t.setSelfMute(false);
																	t.setProvider(provider);
																	connectStream();
																}
																
															})
															.doOnError(new Consumer<Throwable>() {

																@Override
																public void accept(Throwable t) {
																	ConsoleRunner.println(t.getMessage());
																}
																
															})
															.subscribe();
														}
													}
												}
												
											})
										.subscribe();
									}
									
								})
							.subscribe();
							
						}
					}
					return Mono.empty();
				}
				
			}).doOnError(new Consumer<Throwable>() {

				@Override
				public void accept(Throwable t) {
					ConsoleRunner.println(t.getMessage());
				}
			
			}).subscribe();
		
		
		
		dispatcher.on(MessageCreateEvent.class)
			.subscribe(new Consumer<MessageCreateEvent>() {

				@Override
				public void accept(MessageCreateEvent event) {

					Message message=event.getMessage();
					String content=message.getContent();
					
					if(event.getMember().isPresent()&&content.toLowerCase().startsWith(Config.DISCORD_COMMAND_PREFIX.toLowerCase())) {
						event.getMember().get().getRoles().filter(new Predicate<Role>() {

							@Override
							public boolean test(Role role) {
								return role.getName().equals(Config.DISCORD_ROLE);
							}
							
						}).collectList().doOnSuccess(new Consumer<List<Role>>() {

							@Override
							public void accept(List<Role> roles) {
								if(!roles.isEmpty()) {
									String[]parse=content.split(" ",2);
									String temp=parse.length==2?parse[1]:"";
									String[]args=temp.split(" ",2);
									String command=args[0].toLowerCase();
									
									Mono<? extends MessageChannel>mono=Config.DISCORD_RESPONSE_TO_PRIVATE?message.getAuthor().get().getPrivateChannel():message.getChannel();
									
									mono.subscribe(new Consumer<MessageChannel>() {

										@Override
										public void accept(MessageChannel channel) {
											if(channel!=null) {
												if(!command.isEmpty()) {
													Command<?> kommand=commands.getCommand(command);
													
													if(kommand!=null) {
														Mono<?>cmd=kommand.execute(args.length==2?args[1]:"",channel);
														cmd.subscribe();
													}
												} else {
													Command<?> kommand=commands.getCommand("help");
													if(kommand!=null) {
														Mono<?>cmd=kommand.execute(args.length==2?args[1]:"",channel);
														cmd.subscribe();
													}
												}
											}
										}
										
									});
								}
							}
							
						}).subscribe();
					}
					
				}

			});
		
		client.onDisconnect().doOnSuccess(new Consumer<Void>() {

			@Override
			public void accept(Void t) {
				status=Status.DISCONNECTED;
				ConsoleRunner.println("[Connection to Discord CLOSED!]");
			}
			
		}).subscribe();
	}
	
	void sendHelp(MessageChannel channel) {
		channel.createEmbed(new Consumer<EmbedCreateSpec>() {

			@Override
			public void accept(EmbedCreateSpec spec) {
				spec.setTitle("Streamserver help text");
				spec.addField("test",Config.DISCORD_HELP_TEXT,true);
				spec.setColor(Color.GREEN);
			}
			
		}).subscribe();
	}
	
	public void connectStream() {
		playerManager.loadItem(Config.YOUTUBE_LINK,scheduler);
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
		
		audioPlayer.destroy();
		client.logout().block(Duration.ofSeconds(20));
	}
	
}
