package com.gmail.berndivader.streamserver.console.command.commands;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.mysql.UpdatePlaylist;

@ConsoleCommand(name="refresh",usage="Refresh playlist in database.")
public class RefreshPlaylist extends Command {

	@Override
	public boolean execute(String[] args) {
		try {
			new UpdatePlaylist(true);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

}
