package com.gmail.berndivader.streamserver.console.command.commands;

import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.annotation.Requireds;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;

@ConsoleCommand(name="restart",usage="Restart current stream.",requireds={Requireds.BROADCASTRUNNER,Requireds.DATABASE})
public class Restart extends Command {

	@Override
	public boolean execute(String[] args) {
		BroadcastRunner.restartStream();
		return true;
	}

}
