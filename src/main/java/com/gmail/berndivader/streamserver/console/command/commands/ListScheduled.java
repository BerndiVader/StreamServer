package com.gmail.berndivader.streamserver.console.command.commands;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.console.ConsoleRunner;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.mysql.GetAllScheduled;

@ConsoleCommand(name="listscheduled")
public class ListScheduled extends Command {

	@Override
	public boolean execute(String[] args) {
		
		GetAllScheduled scheduled=new GetAllScheduled();
		ArrayList<String> files=null;
		try {
			files = scheduled.future.get(20, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			ConsoleRunner.println(builder.toString());
		} else {
			ConsoleRunner.println("No scheduled files found.");
		}
		
		return true;
	}

}
