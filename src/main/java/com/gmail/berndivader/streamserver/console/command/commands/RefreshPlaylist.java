package com.gmail.berndivader.streamserver.console.command.commands;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.mysql.UpdatePlaylist;
import com.gmail.berndivader.streamserver.term.ANSI;

@ConsoleCommand(name="refresh",usage="Refresh playlist in database.")
public class RefreshPlaylist extends Command {

	@Override
	public boolean execute(String[] args) {
		try {
			new UpdatePlaylist(true);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			ANSI.printErr("Failed to update mysql playlist table.",e);
		}
		return true;
	}

}
