package com.gmail.berndivader.streamserver.console.command.commands;

import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.console.ConsoleRunner;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;

@ConsoleCommand(name="m",usage="")
public class MessageInfo extends Command {

	@Override
	public boolean execute(String[] args) {
		if(BroadcastRunner.isStreaming()) {
			String message=BroadcastRunner.currentMessage;
			if(message!=null) {
				ConsoleRunner.println("Last ffmpeg output: "+message);
			} else {
				ConsoleRunner.println("Currently no ffmpeg message output present.");
			}
		} else {
			ConsoleRunner.println("Currently no stream is running.");
		}
		return true;
	}

}
