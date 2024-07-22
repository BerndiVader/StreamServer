package com.gmail.berndivader.streamserver.console.command.commands;

import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.annotation.Requireds;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;
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
					ANSI.println("Current File: "+BroadcastRunner.getFiles()[BroadcastRunner.index.get()-1].getName());
					break;
				case "next":
					ANSI.println("Next File: "+BroadcastRunner.getFiles()[BroadcastRunner.index.get()].getName());
					break;
				default:
					break;
			}
		}
		return true;
	}

}
