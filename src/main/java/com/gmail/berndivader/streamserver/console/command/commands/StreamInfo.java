package com.gmail.berndivader.streamserver.console.command.commands;

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
			if(BroadcastRunner.format!=null) {
				float duration=BroadcastRunner.format.getDuration();
				ANSI.println("===Current playing===");
				for(int i1=0;i1<args.length;i1++) {
					switch(args[i1]) {
					case "title":
						ANSI.println("Title: "+BroadcastRunner.format.getTag("title"));
						break;
					case "artist":
						ANSI.println("Artist:"+BroadcastRunner.format.getTag("artist"));
						break;
					case "date":
						ANSI.println("Date:"+BroadcastRunner.format.getTag("date"));
						break;
					case "comment":
						ANSI.println("Comment:"+BroadcastRunner.format.getTag("comment"));
						break;
					case "playtime":
					case "time":
						ANSI.println("Playtime:"+(long)(duration/60)+":"+(long)(duration%60));
						break;
					case "file":
						ANSI.println("File:"+BroadcastRunner.format.getFilename());
						break;
					case "bitrate":
					case "bits":
						ANSI.println("Bitrate:"+BroadcastRunner.format.getBitRate());
						break;
					case "format":
						ANSI.println("Format:"+BroadcastRunner.format.getFormatName());
						break;
					default:
						ANSI.println("Title: "+BroadcastRunner.format.getTag("title"));
						ANSI.println("Artist:"+BroadcastRunner.format.getTag("artist"));
						ANSI.println("Date:"+BroadcastRunner.format.getTag("date"));
						ANSI.println("Comment:"+BroadcastRunner.format.getTag("comment"));
						ANSI.println("Playtime:"+(long)(duration/60)+":"+(long)(duration%60));
						ANSI.println("File:"+BroadcastRunner.format.getFilename());
						ANSI.println("Bitrate:"+BroadcastRunner.format.getBitRate());
						ANSI.println("Format:"+BroadcastRunner.format.getFormatName());
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
