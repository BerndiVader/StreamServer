package com.gmail.berndivader.streamserver.console.command.commands;

import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.annotation.Requireds;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;

@ConsoleCommand(name="next",usage="Play next file.",requireds={Requireds.BROADCASTRUNNER,Requireds.DATABASE})
public class Next extends Command {

	@Override
	public boolean execute(String[] args) {
		BroadcastRunner.next();
		return true;
	}

}
