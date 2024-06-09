package com.gmail.berndivader.streamserver.console.command.commands;

import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.term.ANSI;

@ConsoleCommand(name="debug",usage="Switch debug on/off. Print out the trace tree on all catched exceptions.")
public class Debug extends Command {

	@Override
	public boolean execute(String[] args) {
		
		Config.DEBUG=args[0].isEmpty()?Config.DEBUG^=true:Boolean.valueOf(args[0]);
		ANSI.println("DEBUG is now ".concat(Boolean.toString(Config.DEBUG)));
		
		return true;
	}

}
