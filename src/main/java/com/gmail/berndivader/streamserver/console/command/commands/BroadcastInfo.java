package com.gmail.berndivader.streamserver.console.command.commands;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;
import com.gmail.berndivader.streamserver.term.ANSI;

@ConsoleCommand(name="b", usage="[status|file|next] -> Info about broadcast status, current file and next file")
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
					ANSI.println("Current File: "+Helper.files[BroadcastRunner.index-1].getName());
					break;
				case "next":
					ANSI.println("Next File: "+Helper.files[BroadcastRunner.index].getName());
					break;
				default:
					break;
			}
		}
		return true;
	}

}
