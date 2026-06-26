package com.gmail.berndivader.streamserver.console.command.commands;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.annotation.Requireds;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.term.ANSI;
import com.gmail.berndivader.streamserver.youtube.Broadcast;
import com.gmail.berndivader.streamserver.youtube.PrivacyStatus;
import com.gmail.berndivader.streamserver.youtube.packets.EmptyPacket;
import com.gmail.berndivader.streamserver.youtube.packets.ErrorPacket;
import com.gmail.berndivader.streamserver.youtube.packets.LiveBroadcastPacket;
import com.gmail.berndivader.streamserver.youtube.packets.Packet;
import com.gmail.berndivader.streamserver.youtube.packets.UnknownPacket;
import com.google.gson.JsonObject;

@ConsoleCommand(name="updatelive",usage="--title '<title>' --desc '<description>' --privacy <status> --json : Update title, description or privacy status for current YouTube livestream.",requireds={Requireds.BROADCASTRUNNER})
public class UpdateBroadcast extends Command{

	@Override
	public boolean execute(String[] args) {
		String arg=args[0];
		
		boolean printJson=false;
		String title="";
		String description="";
		PrivacyStatus privacy=null;
		
		printJson=arg.contains("--json");
		
		if(arg.contains("--title")) {
			Matcher m=Pattern.compile("--title\\s+'([^']*)'").matcher(arg);
			if(m.find()) title=m.group(1);
		}
		if(arg.contains("--desc")) {
			Matcher m=Pattern.compile("--desc\\s+'([^']*)'").matcher(arg);
			if(m.find()) description=m.group(1);
		}
		if(arg.contains("--privacy")) {
			Matcher m=Pattern.compile("--privacy\\s+(public|private|unlisted)").matcher(arg);
			if(m.find()) privacy=PrivacyStatus.valueOf(m.group(1).toUpperCase());
		}
		
		Packet p=Packet.build(new JsonObject(),EmptyPacket.class);
		try {
			p=Broadcast.updateLiveBroadcast(title,description,privacy).get(15l,TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			ANSI.error(e.getMessage(),e);
			p=ErrorPacket.buildError(e.getMessage(),"Failed to execute livestatus command.","CUSTOM");
		}
		
		if(p instanceof LiveBroadcastPacket) {
			LiveBroadcastPacket broadcast=(LiveBroadcastPacket)p;
			if(printJson) {
				ANSI.println(broadcast.source().toString());
			} else {
				StringBuilder out=new StringBuilder("[GREEN]LiveBroadcastPacket:[BR]");
				out.append("[BLUE]Title: [YELLOW]".concat(broadcast.snippet.title));
				out.append("[BR][BLUE]Description: [YELLOW]".concat(broadcast.snippet.description));
				out.append("[BR][BLUE]Publish time: [YELLOW]".concat(broadcast.snippet.publishedAt));
				out.append("[BR][BLUE]Privacy: [YELLOW]".concat(broadcast.status.privacyStatus));
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
		} else if(p instanceof UnknownPacket) {
			ANSI.println("The livebroadcast request anwered with an unknown packet.");
		}
	
		return true;
	}
}

