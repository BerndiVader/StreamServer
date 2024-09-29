package com.gmail.berndivader.streamserver.console.command.commands;

import java.io.File;

import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.annotation.Requireds;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;
import com.gmail.berndivader.streamserver.ffmpeg.FFProbePacket;
import com.gmail.berndivader.streamserver.term.ANSI;

@ConsoleCommand(name="b", usage="[status|file|next] -> Info about broadcast status, current file and next file",requireds={Requireds.BROADCASTRUNNER})
public class BroadcastInfo extends Command {

	@Override
	public boolean execute(String[] args) {
		
		ANSI.println("===Broadcast information===");
		for(int i1=0;i1<args.length;i1++) {
			switch(args[i1]) {
				case "status":
					ANSI.println("Is broadcasting: "+BroadcastRunner.isStreaming());
					break;
				case "file":
					File playling=BroadcastRunner.playing();
					if(playling!=null) {
						ANSI.println("Current File: "+playling.getName());
						ANSI.println(FFProbePacket.build(playling).toString());
					} else {
						ANSI.println("No file playling.");
					}
					break;
				case "next":
					File next=BroadcastRunner.getFiles()[BroadcastRunner.index.get()];
					if(next!=null) {
						ANSI.println("Next File: "+next.getName());
						ANSI.println(FFProbePacket.build(next).toString());
					} else {
						ANSI.println("No file to play next.");
					}
					break;
				default:
					break;
			}
		}
		return true;
	}

}
