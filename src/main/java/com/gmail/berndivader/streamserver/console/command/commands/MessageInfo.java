package com.gmail.berndivader.streamserver.console.command.commands;

import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;
import com.gmail.berndivader.streamserver.term.ANSI;

@ConsoleCommand(name="m",usage="Show current ffmpeg output message.")
public class MessageInfo extends Command {

	@Override
	public boolean execute(String[] args) {
		if(BroadcastRunner.isStreaming()) {
			String message=BroadcastRunner.currentMessage;
			if(message!=null) {
				ANSI.println("Last ffmpeg output: "+message);
			} else {
				ANSI.println("Currently no ffmpeg message output present.");
			}
		} else {
			ANSI.println("Currently no stream is running.");
		}
		return true;
	}

}
