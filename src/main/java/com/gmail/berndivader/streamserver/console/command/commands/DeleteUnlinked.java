package com.gmail.berndivader.streamserver.console.command.commands;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.mysql.DeleteUnlinkedMediafiles;
import com.gmail.berndivader.streamserver.term.ANSI;

@ConsoleCommand(name="del-unlinked",usage="Collect downloadables and delete unlinked files and dead table entries")
public class DeleteUnlinked extends Command {

	@Override
	public boolean execute(String[] args) {
		
		DeleteUnlinkedMediafiles task=new DeleteUnlinkedMediafiles();
		try {
			task.future.get(20l,TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			ANSI.printErr(e.getMessage(),e);
			return false;
		}
		
		return true;
	}

}
