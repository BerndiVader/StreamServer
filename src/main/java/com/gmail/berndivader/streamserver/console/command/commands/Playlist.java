package com.gmail.berndivader.streamserver.console.command.commands;

import com.gmail.berndivader.streamserver.Utils;
import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.console.ConsoleRunner;
import com.gmail.berndivader.streamserver.console.command.Command;

@ConsoleCommand(name="playlist",usage="[regex] -> Show playlist use regex to filter = valid regex pattern.")
public class Playlist extends Command {

	@Override
	public boolean execute(String[] args) {
		String regex=args[0];
		if(regex.startsWith("custom ")) regex=regex.replaceFirst("custom ", "");
		ConsoleRunner.println(Utils.getFilelistAsString(regex));
		return true;
	}

}
