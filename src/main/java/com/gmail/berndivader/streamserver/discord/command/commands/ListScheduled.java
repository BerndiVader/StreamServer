package com.gmail.berndivader.streamserver.discord.command.commands;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.gmail.berndivader.streamserver.annotation.DiscordCommand;
import com.gmail.berndivader.streamserver.annotation.Requireds;
import com.gmail.berndivader.streamserver.discord.command.Command;
import com.gmail.berndivader.streamserver.mysql.GetAllScheduled;
import com.gmail.berndivader.streamserver.term.ANSI;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import reactor.core.publisher.Mono;

@DiscordCommand(name="allscheduled",usage="List all scheduled files.",requireds={Requireds.DATABASE})
public class ListScheduled extends Command<Message>{

	@Override
	public Mono<Message> execute(String string, MessageChannel channel) {
		
		GetAllScheduled scheduled=new GetAllScheduled();
		ArrayList<String>files=null;
		try {
			files=scheduled.future.get(20,TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			ANSI.printErr("Error while waiting for get all scheduled future.",e);
		}

		EmbedCreateSpec.Builder embed=EmbedCreateSpec.builder()
				.title("SCHEDULED FILES");
		
		if(files!=null&&!files.isEmpty()) {
			embed.color(Color.CINNABAR);
			StringBuilder builder=new StringBuilder("```");
			int size=files.size();
			for(int i1=0;i1<size;i1++) {
				builder.append(files.get(i1));
				builder.append("\n");
			}
			builder.append("```");
			embed.description(builder.toString());
		} else {
			embed.color(Color.RED)
				.description("No scheduled files.");
		}
		
		return channel.createMessage(embed.build());
	}

}
