package com.gmail.berndivader.streamserver.console.command.commands;

import com.github.kokorin.jaffree.ffprobe.Format;
import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.console.ConsoleRunner;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;

@ConsoleCommand(name="f",usage="")
public class StreamInfo extends Command {

	@Override
	public boolean execute(String[] args) {
		if(BroadcastRunner.isStreaming()) {
			Format format=BroadcastRunner.currentFormat;
			if(format!=null) {
				float duration=format.getDuration();
				ConsoleRunner.println("===Current playing===");
				for(int i1=0;i1<args.length;i1++) {
					switch(args[i1]) {
					case "title":
						ConsoleRunner.println("Title: "+format.getTag("title"));
						break;
					case "artist":
						ConsoleRunner.println("Artist:"+format.getTag("artist"));
						break;
					case "date":
						ConsoleRunner.println("Date:"+format.getTag("date"));
						break;
					case "comment":
						ConsoleRunner.println("Comment:"+format.getTag("comment"));
						break;
					case "playtime":
					case "time":
						ConsoleRunner.println("Playtime:"+(long)(duration/60)+":"+(long)(duration%60));
						break;
					case "file":
						ConsoleRunner.println("File:"+format.getFilename());
						break;
					case "bitrate":
					case "bits":
						ConsoleRunner.println("Bitrate:"+format.getBitRate());
						break;
					case "format":
						ConsoleRunner.println("Format:"+format.getFormatName());
						break;
					default:
						ConsoleRunner.println("Title: "+format.getTag("title"));
						ConsoleRunner.println("Artist:"+format.getTag("artist"));
						ConsoleRunner.println("Date:"+format.getTag("date"));
						ConsoleRunner.println("Comment:"+format.getTag("comment"));
						ConsoleRunner.println("Playtime:"+(long)(duration/60)+":"+(long)(duration%60));
						ConsoleRunner.println("File:"+format.getFilename());
						ConsoleRunner.println("Bitrate:"+format.getBitRate());
						ConsoleRunner.println("Format:"+format.getFormatName());
						break;
					}
				}
			} else {
				ConsoleRunner.println("No information about stream available.");
			}
		} else {
			ConsoleRunner.println("Currently no stream is running.");
		}
		return true;
	}

}
