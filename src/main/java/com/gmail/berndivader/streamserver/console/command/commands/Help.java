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
		
		ANSI.raw(Config.HELP_TEXT);
		Commands.instance.cmds.forEach((name,clazzName)->{
			try {
				Class<?>clazz=Class.forName(clazzName);
				if(clazz!=null) {
					ConsoleCommand annotation=clazz.getAnnotation(ConsoleCommand.class);
					if(annotation!=null) ANSI.raw("[BOLD][RED]"+annotation.name().concat("[/RED][/BOLD] [BLUE]-[/BLUE] [GREEN]".concat(annotation.usage()).concat("[/GREEN][BR]")));
				}
			} catch (ClassNotFoundException e) {
				ANSI.error("Error while processing help.",e);
			}
		});
		return true;
	}

}
