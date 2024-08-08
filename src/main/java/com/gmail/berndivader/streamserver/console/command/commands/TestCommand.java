package com.gmail.berndivader.streamserver.console.command.commands;

import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.console.command.Command;

@ConsoleCommand(name="test",usage="dummy command to fast try new functions.")
public class TestCommand extends Command {

	@Override
	public boolean execute(String[] args) {
		
		return true;
		
	}

}
