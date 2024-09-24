package com.gmail.berndivader.streamserver.console.command.commands;

import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.annotation.Requireds;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;
import com.gmail.berndivader.streamserver.mysql.AddScheduled;
import com.gmail.berndivader.streamserver.term.ANSI;

@ConsoleCommand(name="schedule",usage="[filename] -> Add file to scheduled playlist.",requireds= {Requireds.DATABASE})
public class Schedule extends Command {

	@Override
	public boolean execute(String[] args) {
		if(args.length>0) {
			final String filename=args[0].endsWith(".mp4")?args[0]+".mp4":args[0];
			BroadcastRunner.getFileByName(filename)
				.ifPresentOrElse(file->new AddScheduled(file.getName()),
				()->ANSI.error("No file found for "+filename,new Throwable("File not found.")));
		}
		return true;
	}

}
