package com.gmail.berndivader.streamserver.console.command.commands;

import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;
import com.gmail.berndivader.streamserver.term.ANSI;

@ConsoleCommand(name="playlist",usage="[regex] -> Show playlist use regex to filter = valid regex pattern.")
public class Playlist extends Command {

	@Override
	public boolean execute(String[] args) {
		String regex=args[0].toLowerCase();
		ANSI.println(BroadcastRunner.getFilesAsString(regex));
		return true;
	}

}
