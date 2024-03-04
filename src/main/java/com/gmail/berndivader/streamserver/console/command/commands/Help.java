package com.gmail.berndivader.streamserver.console.command.commands;

import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.console.ConsoleRunner;
import com.gmail.berndivader.streamserver.console.command.Command;

@ConsoleCommand(name="h",usage="")
public class Help extends Command {

	@Override
	public boolean execute(String[] args) {
		
		ConsoleRunner.println(Config.HELP_TEXT);
		return true;
	}

}
