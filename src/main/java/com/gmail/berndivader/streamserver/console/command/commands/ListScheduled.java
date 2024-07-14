package com.gmail.berndivader.streamserver.console.command.commands;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.annotation.Requireds;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.mysql.GetAllScheduled;
import com.gmail.berndivader.streamserver.term.ANSI;

@ConsoleCommand(name="listscheduled",usage="Show all scheduled files.",requireds={Requireds.DATABASE})
public class ListScheduled extends Command {

	@Override
	public boolean execute(String[] args) {
		
		GetAllScheduled scheduled=new GetAllScheduled();
		ArrayList<String> files=null;
		try {
			files = scheduled.future.get(20, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			ANSI.printErr("Error while waiting for get all scheduled future.",e);
		}
		if(files!=null&&!files.isEmpty()) {
			StringBuilder builder=new StringBuilder();
			int size=files.size();
			for(int i1=0;i1<size;i1++) {
				builder.append(files.get(i1));
				builder.append("\n");
			}
			builder.append("\n");
			builder.append(size);
			builder.append(" files found.");
			ANSI.println(builder.toString());
		} else {
			ANSI.println("No scheduled files found.");
		}
		
		return true;
	}

}
