package com.gmail.berndivader.streamserver.console.command.commands;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.Utils;
import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.mysql.AddScheduled;

@ConsoleCommand(name="schedule")
public class Schedule extends Command {

	@Override
	public boolean execute(String[] args) {
		if(args.length>0) {
			String filename=args[0];
			if(!filename.endsWith(".mp4")) {
				filename=filename+".mp4";
			}
			int index=Utils.getFilePosition(filename);
			if(index>-1) {
				filename=Helper.files[index].getName();
				new AddScheduled(filename);
			} else {
				index=Utils.getCustomFilePosition(filename);
				if(index>-1) {
					filename=Helper.customs[index].getName();
					new AddScheduled(filename);
				}
			}
		}
		return true;
	}

}
