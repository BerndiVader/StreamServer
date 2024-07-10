package com.gmail.berndivader.streamserver.console.command.commands;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.term.ANSI;
import com.gmail.berndivader.streamserver.youtube.Youtube;
import com.gmail.berndivader.streamserver.youtube.packets.EmptyPacket;
import com.gmail.berndivader.streamserver.youtube.packets.ErrorPacket;
import com.gmail.berndivader.streamserver.youtube.packets.LiveStreamPacket;
import com.gmail.berndivader.streamserver.youtube.packets.Packet;

@ConsoleCommand(name="ytlive",usage="Get livestream by channel id. Use --json for complete json response.")
public class LivestreamsById extends Command{

	@Override
	public boolean execute(String[] args) {
		String arg=args[0];
		boolean printJson=false;
		if(arg.contains("--json")) {
			printJson=true;
			arg=arg.replace("--json","");
		}
		arg=arg.strip();
		Packet packet=new EmptyPacket();
		if(arg.length()==0) arg=Config.YOUTUBE_CHANNEL_ID;
		Future<Packet>future=Youtube.livestreamsByChannelId(arg);
		try {
			packet=future.get(15l,TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			ANSI.printErr("Error while waiting for streamby future.",e);
		}
		if(packet instanceof LiveStreamPacket) {
			LiveStreamPacket lp=(LiveStreamPacket)packet;
			if(printJson) {
				ANSI.println(lp.toString());
			} else {
				String out="[BLUE]Channel: [YELLOW]"+lp.snippet.channelTitle;
				out+="[BLUE][BR]Title: [YELLOW]"+ lp.snippet.title;
				out+="[BLUE][BR]Description: [YELLOW]"+lp.snippet.description;
				out+="[BLUE][BR]Publish time: [YELLOW]"+lp.snippet.publishTime;
				out+="[BLUE][BR]Video id: [YELLOW]"+lp.id.videoId;
				out+="[BLUE][BR]Kind: [YELLOW]"+lp.id.kind;
				out+="[BLUE][BR]Content: [YELLOW]"+lp.snippet.liveBroadcastContent;
				ANSI.println(out);
			}
		} else if(packet instanceof EmptyPacket) {
			ANSI.println("[YELLOW]There is no livestream broadcasting on this channel.");
		} else if(packet instanceof ErrorPacket) {
			ErrorPacket er=(ErrorPacket)packet;
			er.printSimple();
		}
		return true;
	}

}
