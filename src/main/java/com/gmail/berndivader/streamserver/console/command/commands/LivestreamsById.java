package com.gmail.berndivader.streamserver.console.command.commands;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.term.ANSI;
import com.gmail.berndivader.streamserver.youtube.BroadcastStatus;
import com.gmail.berndivader.streamserver.youtube.Youtube;
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
		Future<Packet>future=Youtube.getLiveBroadcast(BroadcastStatus.active);
		
		try {
			p=future.get(15l,TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			ANSI.printErr("Error while waiting for Youtube response.",e);
		}
		
		if(p instanceof LiveBroadcastPacket) {
			LiveBroadcastPacket packet=(LiveBroadcastPacket)p;
			if(printJson) {
				ANSI.println(packet.source().toString());
			} else {
				StringBuilder out=new StringBuilder();
				out.append("[BLUE]Title: [YELLOW]".concat(packet.snippet.title));
				out.append("[BR][BLUE]Description: [YELLOW]".concat(packet.snippet.description));
				out.append("[BR][BLUE]Publish time: [YELLOW]".concat(packet.snippet.publishedAt));
				out.append("[BR][BLUE]Kind: [YELLOW]".concat(packet.kind));
				out.append("[BR][BLUE]Video URL: [YELLOW]https://www.youtube.com/watch?v=".concat(packet.id));
				out.append("[BR][BLUE]Channel URL: [YELLOW]https://www.youtube.com/channel/".concat(packet.snippet.channelId));
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
				
//				JsonObject entry=json.getAsJsonArray("items").get(0).getAsJsonObject();
//				JsonObject snippet=entry.get("snippet").getAsJsonObject();
//				String out="[BLUE][BR]Title: [YELLOW]"+snippet.get("title").getAsString();
//				out+="[BLUE][BR]Description: [YELLOW]"+snippet.get("description").getAsString();
//				out+="[BLUE][BR]Publish time: [YELLOW]"+snippet.get("publishedAt").getAsString();
//				out+="[BLUE][BR]Video id: [YELLOW]"+entry.get("id").getAsString();
//				out+="[BLUE][BR]Kind: [YELLOW]"+Youtube.fromPath(entry,"liveBroadcastContent");
//				out+="[BLUE][BR]URL: [YELLOW]https://www.youtube.com/watch?v="+Youtube.fromPath(entry,"id").replaceAll("\"","");
//				ANSI.println(out);
//			}
		
//		if(json.has("items")) {
//			if(printJson) {
//				ANSI.println(json.toString());
//			} else {
//				JsonObject entry=json.getAsJsonArray("items").get(0).getAsJsonObject();
//				JsonObject snippet=entry.get("snippet").getAsJsonObject();
//				String out="[BLUE][BR]Title: [YELLOW]"+snippet.get("title").getAsString();
//				out+="[BLUE][BR]Description: [YELLOW]"+snippet.get("description").getAsString();
//				out+="[BLUE][BR]Publish time: [YELLOW]"+snippet.get("publishedAt").getAsString();
//				out+="[BLUE][BR]Video id: [YELLOW]"+entry.get("id").getAsString();
//				out+="[BLUE][BR]Kind: [YELLOW]"+Youtube.fromPath(entry,"liveBroadcastContent");
//				out+="[BLUE][BR]URL: [YELLOW]https://www.youtube.com/watch?v="+Youtube.fromPath(entry,"id").replaceAll("\"","");
//				ANSI.println(out);
//			}
//		} else if(json.isEmpty()) {
//			ANSI.println("[YELLOW]There is no livestream broadcasting on this channel.");
//		} else if(json.has("error")) {
//			ANSI.printWarn(json.toString());
//		}
//		return true;
//	}

//}
