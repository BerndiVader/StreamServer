package com.gmail.berndivader.streamserver.console.command.commands;

import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.console.ConsoleRunner;
import com.gmail.berndivader.streamserver.console.command.Command;

public class Help extends Command {

	@Override
	public boolean execute(String[] args) {
		
		ConsoleRunner.println(Config.HELP_TEXT);
		return true;
	}

}
