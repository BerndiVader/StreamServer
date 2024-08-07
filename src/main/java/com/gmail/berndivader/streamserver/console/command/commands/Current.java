package com.gmail.berndivader.streamserver.console.command.commands;

import java.io.File;

import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;
import com.gmail.berndivader.streamserver.ffmpeg.FFProbePacket;
import com.gmail.berndivader.streamserver.term.ANSI;

@ConsoleCommand(name="fileinfo", usage="Show current or give filename media ffprobe info.")
public class Current extends Command {

	@Override
	public boolean execute(String[] args) {
		
		if(args[0].isEmpty()) {
			if(BroadcastRunner.isStreaming()&&BroadcastRunner.playing()!=null) {
				FFProbePacket packet=FFProbePacket.build(BroadcastRunner.playing());
				ANSI.println("[GREEN]"+packet.toString()+"[RESET]");
			} else {
				ANSI.println("[YELLOW]There is currently no media streaming.[RESET]");
			}
		} else {
			File file=new File(args[0]);
			if(file.exists()&&file.isFile()) {
				FFProbePacket packet=FFProbePacket.build(file);
				ANSI.println("[GREEN]"+packet.toString()+"[RESET]");
			} else {
				ANSI.println("[YELLOW]No file found.[RESET]");
			}
		}
		
		return true;
	}

}
