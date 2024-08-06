package com.gmail.berndivader.streamserver.console.command.commands;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.annotation.Requireds;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.term.ANSI;
import com.gmail.berndivader.streamserver.youtube.Youtube;
import com.gmail.berndivader.streamserver.youtube.packets.Packet;

@ConsoleCommand(name="startlive",usage="(title, description, privacy) - Start the livestream on Youtube.",requireds={Requireds.BROADCASTRUNNER})
public class StartLiveStream extends Command {

	@Override
	public boolean execute(String[] args) {
		Future<Packet>future=Youtube.createLivestream("MCH Varo 1-4","24/7 Stream von allen alten Varo Videos.","private");
		
		Packet packet=null;
		try {
			packet=future.get(15l,TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			ANSI.printErr(e.getMessage(),e);
		}
		
		return true;
	}

}
