package com.gmail.berndivader.streamserver.console.command.commands;

import com.github.kokorin.jaffree.ffprobe.Format;
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
			Format format=BroadcastRunner.currentFormat;
			if(format!=null) {
				float duration=format.getDuration();
				ANSI.println("===Current playing===");
				for(int i1=0;i1<args.length;i1++) {
					switch(args[i1]) {
					case "title":
						ANSI.println("Title: "+format.getTag("title"));
						break;
					case "artist":
						ANSI.println("Artist:"+format.getTag("artist"));
						break;
					case "date":
						ANSI.println("Date:"+format.getTag("date"));
						break;
					case "comment":
						ANSI.println("Comment:"+format.getTag("comment"));
						break;
					case "playtime":
					case "time":
						ANSI.println("Playtime:"+(long)(duration/60)+":"+(long)(duration%60));
						break;
					case "file":
						ANSI.println("File:"+format.getFilename());
						break;
					case "bitrate":
					case "bits":
						ANSI.println("Bitrate:"+format.getBitRate());
						break;
					case "format":
						ANSI.println("Format:"+format.getFormatName());
						break;
					default:
						ANSI.println("Title: "+format.getTag("title"));
						ANSI.println("Artist:"+format.getTag("artist"));
						ANSI.println("Date:"+format.getTag("date"));
						ANSI.println("Comment:"+format.getTag("comment"));
						ANSI.println("Playtime:"+(long)(duration/60)+":"+(long)(duration%60));
						ANSI.println("File:"+format.getFilename());
						ANSI.println("Bitrate:"+format.getBitRate());
						ANSI.println("Format:"+format.getFormatName());
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
