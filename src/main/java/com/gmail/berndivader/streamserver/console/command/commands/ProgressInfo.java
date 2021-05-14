package com.gmail.berndivader.streamserver.console.command.commands;

import java.util.concurrent.TimeUnit;

import com.github.kokorin.jaffree.ffmpeg.FFmpegProgress;
import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.console.ConsoleRunner;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;

@ConsoleCommand(name="p")
public class ProgressInfo extends Command {

	@Override
	public boolean execute(String[] args) {
		if(BroadcastRunner.isStreaming()) {
			FFmpegProgress progress=BroadcastRunner.currentProgress;
			if(progress!=null) {
				ConsoleRunner.println("===Progress information===");
				long duration=progress.getTime(TimeUnit.SECONDS);
				for(int i1=0;i1<args.length;i1++) {
					String option=args[i1];
					switch(option) {
						case "time":
							ConsoleRunner.println("Playtime:"+(long)(duration/60)+":"+(long)(duration%60));
							break;
						case "frames":
						case "frame":
							ConsoleRunner.println("Frames: "+progress.getFrame());
							break;
						case "bitrate":
						case "bits":
							ConsoleRunner.println("Bitrate: "+progress.getBitrate());
							break;
						case "quality":
						case "q":
							ConsoleRunner.println("Quality: "+progress.getQ());
							break;
						case "fps":
							ConsoleRunner.println("FPS: "+progress.getFps());
							break;
						case "drops":
							ConsoleRunner.println("Drops: "+progress.getDrop());
							break;
						case "size":
							ConsoleRunner.println("Size: "+progress.getSize());
							break;
						case "speed":
							ConsoleRunner.println("Speed: "+progress.getSpeed());
							break;
						default:
							ConsoleRunner.println("Playtime:"+(long)(duration/60)+":"+(long)(duration%60));
							ConsoleRunner.println("Frames: "+progress.getFrame());
							ConsoleRunner.println("Bitrate: "+progress.getBitrate());
							ConsoleRunner.println("Quality: "+progress.getQ());
							ConsoleRunner.println("FPS: "+progress.getFps());
							ConsoleRunner.println("Drops: "+progress.getDrop());
							ConsoleRunner.println("Size: "+progress.getSize());
							ConsoleRunner.println("Speed: "+progress.getSpeed());
							break;
					}
				}
			} else {
				ConsoleRunner.println("No progress available atm.");
			}
		} else {
			ConsoleRunner.println("Currently no stream is running.");
		}
		return true;
	}

}
