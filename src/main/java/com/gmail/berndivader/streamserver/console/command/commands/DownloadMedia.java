package com.gmail.berndivader.streamserver.console.command.commands;

import java.io.BufferedReader;
import java.io.File;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.ffmpeg.InfoPacket;
import com.gmail.berndivader.streamserver.mysql.MakeDownloadable;
import com.gmail.berndivader.streamserver.term.ANSI;

@ConsoleCommand(name="dl",usage="Download media. Usage: dl --url <http source> or use --help")
public class DownloadMedia extends Command {
		
	private class InterruptHandler implements Callable<Boolean> {
		
		private final Process process;
		private boolean run=true;
		
		public InterruptHandler(Process process) {
			this.process=process;
		}

		@Override
		public Boolean call() throws Exception {
			
			int avail=0;
			while(run&&process.isAlive()) {
				avail=System.in.available();
				if(avail>0) run=!Helper.getStringFromStream(System.in,avail).equals(".q");
			}
			if(process.isAlive()) process.destroy();
			return true;
		}
	}

	@Override
	public boolean execute(String[] args) {
		File directory=new File(Config.DL_MUSIC_PATH);
		if(!directory.exists()) directory.mkdir();
		if(directory.isFile()) return false;
				
		Entry<ProcessBuilder,Optional<InfoPacket>>entry=Helper.prepareDownloadBuilder(directory,args[0]);
		ProcessBuilder builder=entry.getKey();
		
		Optional<InfoPacket>infoPacket=entry.getValue();
		infoPacket.ifPresent(info->ANSI.println(info.toString()));

		try {
			Process process=builder.start();
			Future<Boolean>future=Helper.EXECUTOR.submit(new InterruptHandler(process));
			BufferedReader input=process.inputReader();
			long time=System.currentTimeMillis();
			
			while(process.isAlive()&&!future.isDone()) {
				if(input.ready()) {
					String line=input.readLine();
					if(line.contains("[Metadata]")) {
						infoPacket.ifPresent(info->{
							String[]temp=line.split("\"");
							if(temp.length>0) info.local_filename=temp[1];
						});
					}
					time=System.currentTimeMillis();
					ANSI.printRaw("[CR][DL]"+line);
				} else if(System.currentTimeMillis()-time>Config.DL_TIMEOUT_SECONDS*1000l){
					ANSI.printRaw("[BR]");
					ANSI.printWarn("Download will be terminated, because it appears, that the process is stalled since "+(long)(Config.DL_TIMEOUT_SECONDS/60)+" minutes.");
					process.destroy();
				}
			}
						
			BufferedReader error=process.errorReader();
			if(error!=null&&error.ready()) {
				ANSI.printRaw("[BR]");
				error.lines().forEach(line->ANSI.printWarn(line));
			}
			if(process.isAlive()) process.destroy();
			
			if(infoPacket.isPresent()) {
				InfoPacket info=infoPacket.get();
				if(info.downloadable) {
					File file=new File(builder.directory().getAbsolutePath()+"/"+info.local_filename);
					if(file.exists()&&file.isFile()&&file.canRead()) {
						MakeDownloadable downloadable= new MakeDownloadable(file);
						boolean ok=downloadable.future.get(2,TimeUnit.MINUTES);
						if(ok) {
							ANSI.println("[BR][BOLD][GREEN]"+downloadable.getDownloadLink()+"[RESET]");
						} else {
							ANSI.println("[BR]Failed to create download link.");
						}
					}
				}
			}
		} catch (Exception e) {
			ANSI.printRaw("[BR]");
			ANSI.printErr("Error while looping yt-dlp process.",e);
		}
		return true;
	}
	
}
