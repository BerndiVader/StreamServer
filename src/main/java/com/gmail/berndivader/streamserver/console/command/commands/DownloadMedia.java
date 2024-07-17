package com.gmail.berndivader.streamserver.console.command.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.annotation.Requireds;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.ffmpeg.InfoPacket;
import com.gmail.berndivader.streamserver.mysql.CleanUpDownloadables;
import com.gmail.berndivader.streamserver.mysql.MakeDownloadable;
import com.gmail.berndivader.streamserver.term.ANSI;

@ConsoleCommand(name="dl",usage="Download media. Usage: dl --url <http source> or use --help",requireds = {Requireds.DATABASE})
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
		
		CleanUpDownloadables cleanUp=new CleanUpDownloadables();
		try {
			cleanUp.future.get(20l,TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			ANSI.printErr("Failed to run cleanup process.",e);
		}

		Optional<File>opt=Helper.getOrCreateMediaDir(Config.DL_MEDIA_PATH);
		if(opt.isEmpty()) return false;
		
		File directory=opt.get();
				
		Entry<ProcessBuilder,InfoPacket>entry=Helper.createDownloadBuilder(directory,args[0]);
		ProcessBuilder builder=entry.getKey();
		
		InfoPacket infoPacket=entry.getValue();
		ANSI.println(infoPacket.toString());

		try {
			Process process=builder.start();
			Future<Boolean>interruptHandler=Helper.EXECUTOR.submit(new InterruptHandler(process));
			try(InputStream input=process.getInputStream();
				BufferedReader error=process.errorReader()) {
				long time=System.currentTimeMillis();
				
				while(process.isAlive()&&!interruptHandler.isDone()) {
					int avail=input.available();
					if(avail>0) {
						time=System.currentTimeMillis();
						String line=new String(input.readNBytes(avail));
						if(line.contains("[Metadata]")) {
							String[]temp=line.split("\"");
							if(temp.length>0) infoPacket.local_filename=temp[1];						
						}
						ANSI.printRaw("[CR][DL]"+line);
					}
					if(System.currentTimeMillis()-time>Config.DL_TIMEOUT_SECONDS*1000l){
						ANSI.printRaw("[BR]");
						ANSI.printWarn("Download will be terminated, because it appears, that the process is stalled since "+(long)(Config.DL_TIMEOUT_SECONDS/60)+" minutes.");
						process.destroy();
					}
				}
				
				if(error!=null&&error.ready()) {
					ANSI.printRaw("[BR]");
					error.lines().forEach(line->ANSI.printWarn(line));
				}
			}
						
			if(process.isAlive()) process.destroy();
			ANSI.printRaw("[BR]");
			
			if(infoPacket.downloadable) {
				File file=new File(builder.directory().getAbsolutePath()+"/"+infoPacket.local_filename);
				if(file.exists()&&file.isFile()&&file.canRead()) {
					MakeDownloadable downloadable= new MakeDownloadable(file,infoPacket.temp);
					Optional<String>optLink=downloadable.future.get(2,TimeUnit.MINUTES);
					optLink.ifPresentOrElse(link->{
						ANSI.println("[BR][BOLD][GREEN]"+link+"[RESET]");
					},()->{
						ANSI.printWarn("[BR]Failed to create download link.");
					});
				}
			}
		} catch (Exception e) {
			ANSI.printRaw("[BR]");
			ANSI.printErr("Error while looping yt-dlp process.",e);
		}
		return true;
	}
	
}
