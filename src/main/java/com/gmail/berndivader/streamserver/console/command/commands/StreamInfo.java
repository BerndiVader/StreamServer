package com.gmail.berndivader.streamserver.console.command.commands;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.annotation.Requireds;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;
import com.gmail.berndivader.streamserver.term.ANSI;

@ConsoleCommand(name="f",usage="[title|artist|date|comment|playtime|file|bitrate|format] -> Show ffmpegprobe gathered info.",requireds={Requireds.BROADCASTRUNNER})
public class StreamInfo extends Command {

	@Override
	public boolean execute(String[] args) {
		if(BroadcastRunner.isStreaming()) {
			if(BroadcastRunner.probePacket()!=null) {
				ANSI.println("===Current playing===");
				for(int i1=0;i1<args.length;i1++) {
					switch(args[i1]) {
					case "title":
						ANSI.println("Title: "+BroadcastRunner.probePacket().tags.title);
						break;
					case "artist":
						ANSI.println("Artist:"+BroadcastRunner.probePacket().tags.artist);
						break;
					case "date":
						ANSI.println("Date:"+BroadcastRunner.probePacket().tags.date);
						break;
					case "comment":
						ANSI.println("Comment:"+BroadcastRunner.probePacket().tags.comment);
						break;
					case "playtime":
					case "time":
						ANSI.println("Playtime:"+Helper.stringFloatToTime(BroadcastRunner.probePacket().duration));
						break;
					case "file":
						ANSI.println("File:"+BroadcastRunner.probePacket().getPath());
						break;
					case "bitrate":
					case "bits":
						ANSI.println("Bitrate:"+BroadcastRunner.probePacket().bit_rate);
						break;
					case "format":
						ANSI.println("Format:"+BroadcastRunner.probePacket().format_long_name);
						break;
					default:
						ANSI.println("Title: "+BroadcastRunner.probePacket().tags.title);
						ANSI.println("Artist:"+BroadcastRunner.probePacket().tags.artist);
						ANSI.println("Date:"+BroadcastRunner.probePacket().tags.date);
						ANSI.println("Comment:"+BroadcastRunner.probePacket().tags.comment);
						ANSI.println("Playtime:"+Helper.stringFloatToTime(BroadcastRunner.probePacket().duration));
						ANSI.println("File:"+BroadcastRunner.probePacket().getPath());
						ANSI.println("Bitrate:"+BroadcastRunner.probePacket().bit_rate);
						ANSI.println("Format:"+BroadcastRunner.probePacket().format_name);
						break;
					}
				}
			} else {
				ANSI.println("No information about stream available.");
			}
		} else {
			ANSI.println("Currently no stream is running.");
		}
		return true;
	}

}
