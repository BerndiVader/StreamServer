package com.gmail.berndivader.streamserver.console.command.commands;

import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.term.ANSI;

@ConsoleCommand(name="config", usage="save|load -> Save or load current settings to config file")
public class ConfigCommands extends Command {

	@Override
	public boolean execute(String[] args) {
		switch(args[0]) {
		case "save":
			if(Config.saveConfig()) {
				ANSI.println("Configuration saved");
			} else {
				ANSI.println("Failed to save configuration");
			}
			break;
		case "load":
			if(Config.loadConfig()) {
				ANSI.println("Configuration reloaded");
			} else {
				ANSI.println("Failed to load configuration");
			}
			break;
		}
		return true;
	}

}
