package com.gmail.berndivader.streamserver.console.command.commands;

import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;


@ConsoleCommand(name="test",usage="dummy command to fast try new functions.")
public class TestCommand extends Command {

	@Override
	public boolean execute(String[] args) {
		
		BroadcastRunner.checkBroadcast();
		return true;
		
	}

}
