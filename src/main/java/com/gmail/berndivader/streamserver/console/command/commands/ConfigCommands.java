package com.gmail.berndivader.streamserver.console.command.commands;

import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.console.ConsoleRunner;
import com.gmail.berndivader.streamserver.console.command.Command;

@ConsoleCommand(name="config", usage="")
public class ConfigCommands extends Command {

	@Override
	public boolean execute(String[] args) {
		switch(args[0]) {
		case "save":
			if(!Config.saveConfig()) {
				ConsoleRunner.println("Configuration saved");
			} else {
				ConsoleRunner.println("Failed to save configuration");
			}
			break;
		case "load":
			if(Config.loadConfig()) {
				ConsoleRunner.println("Configuration reloaded");
			} else {
				ConsoleRunner.println("Failed to load configuration");
			}
			break;
		}
		return true;
	}

}
