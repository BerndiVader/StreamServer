package com.gmail.berndivader.streamserver.discord.action;

import java.io.File;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.gmail.berndivader.streamserver.discord.DiscordBot;
import com.gmail.berndivader.streamserver.discord.permission.Permissions;
import com.gmail.berndivader.streamserver.discord.permission.User.Rank;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;
import com.gmail.berndivader.streamserver.ffmpeg.FFProbePacket;
import com.gmail.berndivader.streamserver.mysql.AddScheduled;
import com.gmail.berndivader.streamserver.term.ANSI;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.command.Interaction;
import discord4j.core.object.component.ActionComponent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.component.MessageComponent;
import discord4j.core.spec.MessageEditMono;
import discord4j.discordjson.json.ImmutableComponentData;
import discord4j.discordjson.possible.Possible;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class ButtonAction {
	
	public final static String ACTION_PREFIX="ACTION";
	private static ConcurrentHashMap<String,SimpleEntry<Long,Action>>actions=new ConcurrentHashMap<String,SimpleEntry<Long,Action>>();
	
	private ButtonAction() {}
	
	public static Mono<Object> process(ButtonInteractionEvent event) {

		return Mono.fromRunnable(()->{
			
			Interaction inter=event.getInteraction();
			if(inter.getApplicationId().asLong()!=DiscordBot.client.getSelfId().asLong()||!inter.getMember().isPresent()) return;
			
			if(inter.getCommandInteraction().isPresent()) {
				ApplicationCommandInteraction action=inter.getCommandInteraction().get();
				
				if(action.getCustomId().isPresent()) {
					String custom=action.getCustomId().get();
					if(custom.startsWith(ACTION_PREFIX)) {
						
						event.deferEdit().withEphemeral(true).subscribe();
						
						JsonObject json=JsonParser.parseString(custom.replaceFirst(ACTION_PREFIX,"")).getAsJsonObject();
						SimpleEntry<Long,Action>entry=json.has("uid")?actions.get(json.get("uid").getAsString()):null;
						
						if(entry==null) {
							event.getMessage().get().edit().withContentOrNull("Button action is expired.").subscribe();
							return;
						}
						
						Action a=entry.getValue();						
						Long userId=inter.getMember().get().getId().asLong();
						if(!userId.equals(a.userId)&&!Permissions.Users.permitted(userId,Rank.MOD)) return;
						
						actions.remove(a.uid);
						MessageEditMono mono=event.getMessage().get().edit();
						
						if(processAction(entry.getValue(),event)) {
							event.getMessage().get().getComponents().forEach(row->{
								if(row.getType().equals(MessageComponent.Type.ACTION_ROW)) {
									if(!row.getData().components().isAbsent()) {
										List<ActionComponent>list=new ArrayList<ActionComponent>();
										
										row.getData().components().get().forEach(aa->{
											if(!aa.customId().isAbsent()&&aa.customId().get().contains(a.uid)) {
												list.add(((Button)Button.fromData(aa)).disabled(true));
											} else {
												list.add((ActionComponent)ActionComponent.fromData(aa));
											}
										});
										
										mono.withComponents(ActionRow.of(list)).subscribe();
									}
								}
							});
						}
						
					}
				}
				
			};
			
		}).subscribeOn(Schedulers.boundedElastic());

		
	}
	
	private static boolean processAction(Action action,ButtonInteractionEvent event) {
		AtomicBoolean ok=new AtomicBoolean(false);
		switch(action.action) {
			case PLAY: {
				BroadcastRunner.getFileByName(action.packet.getFileName()).ifPresent(file->{
					BroadcastRunner.playFile(file);
					ok.set(true);
				});
				break;
			}
			case SCHEDULE: {
				BroadcastRunner.getFileByName(action.packet.getFileName()).ifPresent(file->{
					try {
						ok.set(new AddScheduled(file.getName()).future.get(30l,TimeUnit.SECONDS));
					} catch (InterruptedException | ExecutionException | TimeoutException e) {
						ANSI.printErr(e.getMessage(),e);
					}
				});
				break;
			}
			default: {
				break;
			}
		}
		return ok.get();
	}
	
	public static final class Builder {
		
		private static ImmutableComponentData.Builder builder=ImmutableComponentData.builder();
		
		public static Button cancel(String uuid) {
			
			return (Button)Button.fromData(
				builder
				.type(MessageComponent.Type.BUTTON.getValue())
				.style(Button.Style.DANGER.getValue())
				.customId(Possible.of(uuid.toString()))
				.label(Possible.of("CANCEL"))
				.url(Possible.absent())
				.build()
			);
			
		}
		
		public static Button schedule(File file,Long userId) {
			
			Action action=of(ID.SCHEDULE,file,userId);
			
			return (Button)Button.fromData(
				builder
				.customId(Possible.of(action.toString()))
				.type(MessageComponent.Type.BUTTON.getValue())
				.style(Button.Style.SECONDARY.getValue())
				.label(Possible.of("SCHEDULE"))
				.url(Possible.absent())
				.build()
			);
			
		}
		
		public static Button play(File file,Long userId) {
			
			Action action=of(ID.PLAY,file,userId);
			
			return (Button)Button.fromData(
					builder
					.customId(Possible.of(action.toString()))
					.type(MessageComponent.Type.BUTTON.getValue())
					.style(Button.Style.DANGER.getValue())
					.label(Possible.of("PLAY"))
					.url(Possible.absent())
					.build()
				);
			
		}
		
		public static Button link(String url,String label) {
			
			return (Button)Button.fromData(
				builder
				.type(MessageComponent.Type.BUTTON.getValue())
				.style(Button.Style.LINK.getValue())
				.label(Possible.of(label))
				.url(Possible.of(url))
				.customId(Possible.absent())
				.build()
			);
			
		}
		
		private static Action of(ID id,File file,Long userId) {
			FFProbePacket packet=FFProbePacket.build(file);
			String uid=UUID.randomUUID().toString();
			Action action=Action.build(id,uid,packet,userId);
			
			long current=System.currentTimeMillis()/60000l;
			actions.entrySet().removeIf(entry->current-entry.getValue().getKey()>15l);
			actions.put(uid,new SimpleEntry<Long, Action>(current,action));
						
			return action;
		}
		
	}
	
	
}
