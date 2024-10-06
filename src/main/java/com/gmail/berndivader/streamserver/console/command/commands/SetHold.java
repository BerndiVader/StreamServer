package com.gmail.berndivader.streamserver.console.command.commands;

import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.annotation.Requireds;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;
import com.gmail.berndivader.streamserver.term.ANSI;

@ConsoleCommand(name="hold",usage="Set the status of hold to true/false.",requireds={Requireds.BROADCASTRUNNER})
public class SetHold extends Command {

	@Override
	public boolean execute(String[] args) {
		
		String arg=args[0];
		BroadcastRunner.hold.set(arg.isEmpty()?!BroadcastRunner.hold.get():Boolean.valueOf(arg));
		ANSI.println(String.format("[BLUE]The Broadcastrunner is set to: [YELLOW]%s[PROMPT]",Boolean.toString(BroadcastRunner.hold.get())));
		return true;
	}

}
