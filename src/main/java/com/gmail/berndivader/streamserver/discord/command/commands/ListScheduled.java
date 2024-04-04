package com.gmail.berndivader.streamserver.discord.command.commands;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import com.gmail.berndivader.streamserver.annotation.DiscordCommand;
import com.gmail.berndivader.streamserver.console.ConsoleRunner;
import com.gmail.berndivader.streamserver.discord.command.Command;
import com.gmail.berndivader.streamserver.mysql.GetAllScheduled;

import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

@DiscordCommand(name="allscheduled",usage="List all scheduled files.")
public class ListScheduled extends Command<Void>{

	@Override
	public Mono<Void> execute(String string, MessageChannel channel) {
		return channel.createEmbed(new Consumer<EmbedCreateSpec>() {

			@Override
			public void accept(EmbedCreateSpec embed) {
				GetAllScheduled scheduled=new GetAllScheduled();
				ArrayList<String>files=null;
				try {
					files=scheduled.future.get(20,TimeUnit.SECONDS);
				} catch (InterruptedException | ExecutionException | TimeoutException e) {
					ConsoleRunner.println(e.getMessage());
				}
				embed.setTitle("SCHEDULED FILES");
				if(files!=null&&!files.isEmpty()) {
					embed.setColor(Color.CINNABAR);
					StringBuilder builder=new StringBuilder();
					int size=files.size();
					for(int i1=0;i1<size;i1++) {
						builder.append(files.get(i1));
						builder.append("\n");
					}
					embed.setDescription(builder.toString());
				} else {
					embed.setColor(Color.RED);
					embed.setDescription("No scheduled files.");
				}
			}
			
		}).then();
		
	}

}
