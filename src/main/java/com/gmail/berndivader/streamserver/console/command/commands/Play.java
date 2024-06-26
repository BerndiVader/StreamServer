package com.gmail.berndivader.streamserver.console.command.commands;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.annotation.Requireds;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;

@ConsoleCommand(name="play",usage="[filename] -> Play file",requireds={Requireds.BROADCASTRUNNER,Requireds.DATABASE})
public class Play extends Command {

	@Override
	public boolean execute(String[] args) {
		String file=args[0];
		int index=Helper.getFilePosition(file);
		if(index==-1) {
			index=Helper.getCustomFilePosition(file);
			if(index>-1) {
				BroadcastRunner.broadcastFilename(Helper.customs[index]);
			}
		} else {
			BroadcastRunner.broadcastPlaylistPosition(index);
		}
		return true;
	}

}
