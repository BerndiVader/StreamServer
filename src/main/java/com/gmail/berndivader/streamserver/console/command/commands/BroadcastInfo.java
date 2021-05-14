package com.gmail.berndivader.streamserver.console.command.commands;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.console.ConsoleRunner;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;

@ConsoleCommand(name="b")
public class BroadcastInfo extends Command {

	@Override
	public boolean execute(String[] args) {
		ConsoleRunner.println("===Broadcast information===");
		for(int i1=0;i1<args.length;i1++) {
			switch(args[i1]) {
				case "status":
					ConsoleRunner.println("Is broadcasting: "+BroadcastRunner.isStreaming());
					break;
				case "file":
					ConsoleRunner.println("Current File: "+Helper.files[BroadcastRunner.index-1].getName());
					break;
				case "next":
					ConsoleRunner.println("Next File: "+Helper.files[BroadcastRunner.index].getName());
					break;
				default:
					break;
			}
		}
		return true;
	}

}
