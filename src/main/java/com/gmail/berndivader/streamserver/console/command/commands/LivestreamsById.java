package com.gmail.berndivader.streamserver.console.command.commands;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.term.ANSI;
import com.gmail.berndivader.streamserver.youtube.Youtube;
import com.gmail.berndivader.streamserver.youtube.packets.EmptyPacket;
import com.gmail.berndivader.streamserver.youtube.packets.Packet;

@ConsoleCommand(name="streamby",usage="Get livestream by id.")
public class LivestreamsById extends Command{

	@Override
	public boolean execute(String[] args) {
		Packet packet=null;
		for(int i=0;i<args.length;i++) {
			if(args[i].length()==0) continue;
			Future<Packet>future=Youtube.livestreamsByChannelId(args[i]);
			try {
				packet=future.get(15l,TimeUnit.SECONDS);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				ANSI.printErr(e.getMessage());
				return false;
			}
			if(packet==null||packet instanceof EmptyPacket) return false;

		}
		return true;
	}

}
