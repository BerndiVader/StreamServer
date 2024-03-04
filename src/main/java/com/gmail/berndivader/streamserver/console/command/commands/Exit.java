package com.gmail.berndivader.streamserver.console.command.commands;

import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.console.ConsoleRunner;
import com.gmail.berndivader.streamserver.console.command.Command;

@ConsoleCommand(name="x",usage="")
public class Exit extends Command {

	@Override
	public boolean execute(String[] args) {
		
		ConsoleRunner.forceExit=args[0].equals("force");
		ConsoleRunner.exit=true;
		return true;
		
	}

}
