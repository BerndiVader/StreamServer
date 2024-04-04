package com.gmail.berndivader.streamserver.console.command.commands;

import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.console.command.Commands;

@ConsoleCommand(name="h",usage="Help")
public class Help extends Command {

	@Override
	public boolean execute(String[] args) {
		
		System.out.println(Config.HELP_TEXT);
		Commands.instance.commands.forEach((name,clazz)->{
			ConsoleCommand annotation=clazz.getAnnotation(ConsoleCommand.class);
			if(annotation!=null) System.out.println(annotation.name().concat(" - ".concat(annotation.usage())));
		});
		return true;
	}

}
