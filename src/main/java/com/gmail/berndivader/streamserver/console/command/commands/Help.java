package com.gmail.berndivader.streamserver.console.command.commands;

import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.console.command.Commands;
import com.gmail.berndivader.streamserver.term.ANSI;

@ConsoleCommand(name="h",usage="Help")
public class Help extends Command {

	@Override
	public boolean execute(String[] args) {
		
		ANSI.printRaw(Config.HELP_TEXT);
		Commands.instance.commands.forEach((name,clazz)->{
			ConsoleCommand annotation=clazz.getAnnotation(ConsoleCommand.class);
			if(annotation!=null) ANSI.printRaw("[BOLD][RED]"+annotation.name().concat("[/RED][/BOLD] [BLUE]-[/BLUE] [GREEN]".concat(annotation.usage()).concat("[/GREEN][BR]")));
		});
		return true;
	}

}
