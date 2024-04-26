package com.gmail.berndivader.streamserver.console.command.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.console.ConsoleRunner;
import com.gmail.berndivader.streamserver.console.command.Command;

@ConsoleCommand(name="dlp",usage="Download media. Usage: dlp --url <http source> or use --help")
public class DownloadMediaFile extends Command {
	
	private class InterruptHandler implements Callable<Boolean> {
		
		private final Process process;
		private boolean run=true;
		private boolean storeTemp=false;
		
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
		
		ProcessBuilder builder=new ProcessBuilder();
		builder.directory(directory);
		if(args[0].startsWith("--no-default")) {
			args[0]=args[0].substring(12);
			builder.command("yt-dlp"
					,"--restrict-filenames"
					,"--output","%(title).200s.%(ext)s"
			);
		} else {
			builder.command("yt-dlp"
					,"--ignore-errors"
					,"--extract-audio"
					,"--format","bestaudio"
					,"--audio-format","mp3"
					,"--audio-quality","160K"
					,"--output","%(title).200s.%(ext)s"
					,"--restrict-filenames"
					,"--no-playlist"
			);
		}
		
		String[]temp=args[0].split(" --");
		
		for(int i=0;i<temp.length;i++) {
			if(temp[i].isEmpty()) continue;
			if(!temp[i].startsWith("--")) temp[i]="--".concat(temp[i]);
			String[]parse=temp[i].split(" ",2);
			for(int j=0;j<parse.length;j++) {
				if(parse[j].equals("--url")) continue;
				if(parse[j].equals("--dir")) {
					if(parse.length==2) {
						File dir=new File(Config.DL_MUSIC_PATH.concat("/").concat(parse[j+1]));
						parse[j+1]="";
						if(!dir.exists()) dir.mkdir();
						if(dir.isDirectory()) {
							builder.directory(dir);
						} else {
							ConsoleRunner.printErr("Warning! Download directory is a file, using default.");
						}
					}
					continue;
				}
				if(!parse[j].isEmpty()) builder.command().add(parse[j]);
			}
		}

		try {
			Process process=builder.start();
			
			InterruptHandler handler=new InterruptHandler(process);
			Future<Boolean>future=Helper.executor.submit(handler);
			
			BufferedReader input=process.inputReader();
			long time=System.currentTimeMillis();
			while(process.isAlive()&&!future.isDone()) {
				if(input!=null&&input.ready()) {
					time=System.currentTimeMillis();
					ConsoleRunner.println(input.readLine());
				} else if(System.currentTimeMillis()-time>Config.DL_TIMEOUT_SECONDS*1000l){
					ConsoleRunner.printErr("Download will be terminated, because it appears, that the process is stalled since "+(long)(Config.DL_TIMEOUT_SECONDS/60)+" minutes.");
					process.destroy();
				}
			}
			
			BufferedReader error=process.errorReader();
			if(error!=null&&error.ready()) {
				error.lines().forEach(line->{
					ConsoleRunner.printErr(line);
				});
			}
			
			if(process.isAlive()) process.destroy();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

}
