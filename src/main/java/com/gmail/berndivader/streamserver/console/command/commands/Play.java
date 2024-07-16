package com.gmail.berndivader.streamserver.console.command.commands;

import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.annotation.Requireds;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;
import com.gmail.berndivader.streamserver.term.ANSI;

@ConsoleCommand(name="play",usage="[filename] -> Play file",requireds={Requireds.BROADCASTRUNNER,Requireds.DATABASE})
public class Play extends Command {

	@Override
	public boolean execute(String[] args) {
		String name=args[0];
		BroadcastRunner.getFileByName(name)
			.ifPresentOrElse(file->BroadcastRunner.playFile(file),
			()->ANSI.println("[YELLOW]No file found.[PROMPT]"));
		return true;
	}

}
