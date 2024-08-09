package com.gmail.berndivader.streamserver.console.command.commands;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.term.ANSI;
import com.gmail.berndivader.streamserver.youtube.Broadcast;
import com.gmail.berndivader.streamserver.youtube.BroadcastStatus;
import com.gmail.berndivader.streamserver.youtube.packets.EmptyPacket;
import com.gmail.berndivader.streamserver.youtube.packets.ErrorPacket;
import com.gmail.berndivader.streamserver.youtube.packets.LiveBroadcastPacket;
import com.gmail.berndivader.streamserver.youtube.packets.Packet;
import com.google.gson.JsonObject;

@ConsoleCommand(name="livestatus",usage="Get info and status of running live broadcast from Youtube.")
public class LivestreamsById extends Command{

	@Override
	public boolean execute(String[] args) {
		String arg=args[0];
		boolean printJson=false;
		printJson=arg.contains("--json");
		
		Packet p=Packet.build(new JsonObject(),EmptyPacket.class);
		try {
			p=Broadcast.getLiveBroadcastWithTries(BroadcastStatus.active,2);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			ANSI.printErr(e.getMessage(),e);
			p=ErrorPacket.buildError(e.getMessage(),"Failed to execute livestatus command.","CUSTOM");
		}
		
		if(p instanceof LiveBroadcastPacket) {
			LiveBroadcastPacket broadcast=(LiveBroadcastPacket)p;
			Broadcast.currentLiveBroadcast=Optional.ofNullable(broadcast);
			if(printJson) {
				ANSI.println(broadcast.source().toString());
			} else {
				StringBuilder out=new StringBuilder();
				out.append("[BLUE]Title: [YELLOW]".concat(broadcast.snippet.title));
				out.append("[BR][BLUE]Description: [YELLOW]".concat(broadcast.snippet.description));
				out.append("[BR][BLUE]Publish time: [YELLOW]".concat(broadcast.snippet.publishedAt));
				out.append("[BR][BLUE]Kind: [YELLOW]".concat(broadcast.kind));
				out.append("[BR][BLUE]Video URL: [YELLOW]https://www.youtube.com/watch?v=".concat(broadcast.id));
				out.append("[BR][BLUE]Channel URL: [YELLOW]https://www.youtube.com/channel/".concat(broadcast.snippet.channelId));
				ANSI.println(out.toString());
			}
			
		} else if(p instanceof ErrorPacket) {
			ErrorPacket packet=(ErrorPacket)p;
			packet.printSimple();
		} else if(p instanceof EmptyPacket) {
			ANSI.println("Currently, no live broadcast running.");
		}
	
		return true;
	}
}

