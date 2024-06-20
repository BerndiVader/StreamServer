package com.gmail.berndivader.streamserver.console.command.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.ffmpeg.InfoPacket;
import com.gmail.berndivader.streamserver.term.ANSI;

@ConsoleCommand(name="dl",usage="Download media. Usage: dl --url <http source> or use --help")
public class DownloadMediaFile extends Command {
	
	private class InterruptHandler implements Callable<Boolean> {
		
		private final Process process;
		private boolean run=true;
		
		public InterruptHandler(Process process) {
			this.process=process;
		}

		@Override
		public Boolean call() throws Exception {
			
			String input="";
			while(run&&process.isAlive()) {
				if(System.in.available()>0) {
					byte[]bytes=new byte[System.in.available()];
					int size=System.in.read(bytes);
					input=new String(bytes).substring(0,size-1);
					if(input!=null&&input.equals(".q")) run=false;
				}
			}
			if(process.isAlive()) process.destroy();
			return true;
		}
	}

	@Override
	public boolean execute(String[] args) {
		File directory=new File(Config.DL_MUSIC_PATH);
		if(!directory.exists()) {
			directory.mkdir();
		}
		if(directory.isFile()) {
			return false;
		}
		
		Entry<ProcessBuilder,InfoPacket>entry=Helper.prepareDownloadBuilder(directory,args[0]);
		InfoPacket infoPacket=entry.getValue();
		ProcessBuilder builder=entry.getKey();
		
		if(infoPacket!=null) {
			ANSI.println(infoPacket.toString());
		}

		try {
			Process process=builder.start();
			Future<Boolean>future=Helper.EXECUTOR.submit(new InterruptHandler(process));
			BufferedReader input=process.inputReader();
			long time=System.currentTimeMillis();
			
			while(process.isAlive()&&!future.isDone()) {
				if(input.ready()) {
					time=System.currentTimeMillis();
					ANSI.println(input.readLine());
				} else if(System.currentTimeMillis()-time>Config.DL_TIMEOUT_SECONDS*1000l){
					ANSI.printWarn("Download will be terminated, because it appears, that the process is stalled since "+(long)(Config.DL_TIMEOUT_SECONDS/60)+" minutes.");
					process.destroy();
				}
			}
			
			BufferedReader error=process.errorReader();
			if(error!=null&&error.ready()) {
				error.lines().forEach(line->{
					ANSI.printWarn(line);
				});
			}
			
			if(process.isAlive()) process.destroy();
		} catch (IOException e) {
			ANSI.printErr("Error while looping yt-dlp process.",e);
		}
		return true;
	}
	
}
