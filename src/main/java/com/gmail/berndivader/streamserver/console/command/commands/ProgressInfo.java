package com.gmail.berndivader.streamserver.console.command.commands;

import java.util.concurrent.TimeUnit;

import com.github.kokorin.jaffree.ffmpeg.FFmpegProgress;
import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;
import com.gmail.berndivader.streamserver.term.ANSI;

@ConsoleCommand(name="p",usage="[time|frames|bitrate|quality|fps|drops|size|speed] -> Show ffmpeg progress info.")
public class ProgressInfo extends Command {

	@Override
	public boolean execute(String[] args) {
		if(BroadcastRunner.isStreaming()) {
			FFmpegProgress progress=BroadcastRunner.currentProgress;
			if(progress!=null) {
				ANSI.println("===Progress information===");
				long duration=progress.getTime(TimeUnit.SECONDS);
				for(int i1=0;i1<args.length;i1++) {
					String option=args[i1];
					switch(option) {
						case "time":
							ANSI.println("Playtime:"+(long)(duration/60)+":"+(long)(duration%60));
							break;
						case "frames":
						case "frame":
							ANSI.println("Frames: "+progress.getFrame());
							break;
						case "bitrate":
						case "bits":
							ANSI.println("Bitrate: "+progress.getBitrate());
							break;
						case "quality":
						case "q":
							ANSI.println("Quality: "+progress.getQ());
							break;
						case "fps":
							ANSI.println("FPS: "+progress.getFps());
							break;
						case "drops":
							ANSI.println("Drops: "+progress.getDrop());
							break;
						case "size":
							ANSI.println("Size: "+progress.getSize());
							break;
						case "speed":
							ANSI.println("Speed: "+progress.getSpeed());
							break;
						default:
							ANSI.println("Playtime:"+(long)(duration/60)+":"+(long)(duration%60));
							ANSI.println("Frames: "+progress.getFrame());
							ANSI.println("Bitrate: "+progress.getBitrate());
							ANSI.println("Quality: "+progress.getQ());
							ANSI.println("FPS: "+progress.getFps());
							ANSI.println("Drops: "+progress.getDrop());
							ANSI.println("Size: "+progress.getSize());
							ANSI.println("Speed: "+progress.getSpeed());
							break;
					}
				}
			} else {
				ANSI.println("No progress available atm.");
			}
		} else {
			ANSI.println("Currently no stream is running.");
		}
		return true;
	}

}
