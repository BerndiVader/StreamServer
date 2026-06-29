package com.gmail.berndivader.streamserver.console.command.commands;

import java.util.concurrent.TimeUnit;

import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.annotation.Requireds;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.term.ANSI;
import com.gmail.berndivader.streamserver.youtube.Video;
import com.gmail.berndivader.streamserver.youtube.packets.EmptyPacket;
import com.gmail.berndivader.streamserver.youtube.packets.ErrorPacket;
import com.gmail.berndivader.streamserver.youtube.packets.Packet;
import com.gmail.berndivader.streamserver.youtube.packets.UnknownPacket;
import com.gmail.berndivader.streamserver.youtube.packets.VideoSnippetPacket;

@ConsoleCommand(name="videoinfo",usage="Get info about yt video by video id.",requireds={Requireds.NONE})
public class VideoInfo extends Command {
	
	private boolean printJson=false;

	@Override
	public boolean execute(String[] args) {
		
		String opts=args[0];
		if(opts.contains("--json")) {
			opts.replace("--json","");
			printJson=true;
		}
		
		String id=opts.trim();
		if(id.isEmpty()) return false;
		
		try {
			Packet candit=Video.getVideoById(id).get(15l,TimeUnit.SECONDS);
			
			if(!(candit instanceof VideoSnippetPacket)) {
				processFail(candit);
				return false;
			}
			
			VideoSnippetPacket video=(VideoSnippetPacket)candit;

			StringBuilder out=new StringBuilder("[GREEN]VideoSnippetPacket:[BR]");
			out.append("[BLUE]Title: [YELLOW]".concat(video.snippet.title));
			out.append("[BR][BLUE]Description: [YELLOW]".concat(video.snippet.description));
			out.append("[BR][BLUE]Channel title: [YELLOW]".concat(video.snippet.channelTitle));
			out.append("[BR][BLUE]Publish time: [YELLOW]".concat(video.snippet.publishedAt));
			out.append("[BR][BLUE]Kind: [YELLOW]".concat(video.kind));
			out.append("[BR][BLUE]Video URL: [YELLOW]https://www.youtube.com/watch?v=".concat(video.id));
			out.append("[BR][BLUE]Channel URL: [YELLOW]https://www.youtube.com/channel/".concat(video.snippet.channelId));
			ANSI.println(out.toString());
			
			if(printJson) ANSI.println(video.toString());
			
		} catch (Exception e) {
			ANSI.error("getvideo command failed: ",e);
		}
		
		return true;
		
	}
	
	private void processFail(Packet packet) {
		
		if(packet instanceof EmptyPacket) {
			ANSI.warn("Got empty packet in return.");
		} else if(packet instanceof UnknownPacket) {
			ANSI.warn("Got unkown packet in return.<BR>");
			ANSI.println(String.format("<YELLOW>%s<RESET>",packet.toString()));
		} else if(packet instanceof ErrorPacket) {
			ErrorPacket error=(ErrorPacket)packet;
			error.printSimple();
		}
		
	}

}
