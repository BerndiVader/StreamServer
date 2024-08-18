package com.gmail.berndivader.streamserver.discord;

import com.gmail.berndivader.streamserver.discord.action.ID;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.command.Interaction;
import discord4j.core.object.component.Button;
import discord4j.core.object.component.MessageComponent;
import discord4j.discordjson.json.ComponentData;
import discord4j.discordjson.json.ImmutableComponentData;
import discord4j.discordjson.possible.Possible;
import reactor.core.publisher.Mono;

public class ButtonAction {
	
	private final static String ACTION_PREFIX="ACTION";
	
	private ButtonAction() {}

	public static Mono<?> process(ButtonInteractionEvent event) {
		
		Interaction inter=event.getInteraction();
		if(inter.getApplicationId().asLong()!=DiscordBot.instance.client.getSelfId().asLong()) return Mono.empty();
		if(inter.getCommandInteraction().isPresent()) {
			ApplicationCommandInteraction action=inter.getCommandInteraction().get();
			action.getCustomId().ifPresent(custom->{
				if(custom.startsWith(ACTION_PREFIX)) {
					JsonObject json=JsonParser.parseString(custom.replaceFirst(ACTION_PREFIX,"")).getAsJsonObject();
				}
			});
		};
		
		return Mono.empty();
	}
	
	public static final class Builder {
		
		private static ImmutableComponentData.Builder builder=ImmutableComponentData.builder();
		
		public static Button cancel(String uuid) {
			ComponentData data=builder
					.type(MessageComponent.Type.BUTTON.getValue())
					.style(Button.Style.DANGER.getValue())
					.customId(Possible.of(uuid.toString()))
					.label(Possible.of("CANCEL"))
					.build();
			return (Button)Button.fromData(data);
		}
		
		public static Button schedule(long userId) {
			
			return (Button)Button.fromData(
				builder
				.customId(Possible.of(String.format("%s{actionId:%d,userId:%d}",ACTION_PREFIX,ID.SCHEDULE.ordinal(),userId)))
				.type(MessageComponent.Type.BUTTON.getValue())
				.style(Button.Style.PRIMARY.getValue())
				.label(Possible.of("SCHEDULE"))
				.build()
				
			);
		}
		
	}
	
	
}
