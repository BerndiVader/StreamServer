package com.gmail.berndivader.streamserver.console.command.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.console.ConsoleRunner;
import com.gmail.berndivader.streamserver.console.command.Command;

@ConsoleCommand(name="dlp",usage="Download media files from various platforms\nUsage: dlp -url <http source> or dlp --help for more.")
public class DownloadMediaFile extends Command {
	
	private class InterruptHandler implements Callable<Boolean> {
		
		private final Process process;
		private boolean exit=false;
		
		public InterruptHandler(Process process) {
			this.process=process;
		}

		@Override
		public Boolean call() throws Exception {
			
			String input="";
			while(!exit&&process.isAlive()) {
				if(System.in.available()>0) {
					byte[]bytes=new byte[System.in.available()];
					int size=System.in.read(bytes);
					input=new String(bytes).substring(0,size-1);
					if(input!=null&&input.equals(".q")) exit=true;
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
		builder.command("yt-dlp"
				,"--ignore-errors"
				,"--extract-audio"
				,"--format","bestaudio"
				,"--audio-format","mp3"
				,"--audio-quality","160K"
				,"--output","%(title)s.%(ext)s"
				,"--restrict-filenames"
				,"--no-playlist"
			);
		
		String[]temp=args[0].split(" --");
		
		for(int i=0;i<temp.length;i++) {
			if(!temp[i].startsWith("--")) temp[i]="--".concat(temp[i]);
			String[]parse=temp[i].split(" ",2);
			for(int j=0;j<parse.length;j++) {
				if(!parse[j].equals("--url")) {
					builder.command().add(parse[j]);
				}
			}
		}
				
		try {
			Process process=builder.start();
			
			InterruptHandler handler=new InterruptHandler(process);
			Future<Boolean>future=Helper.executor.submit(handler);
						
			try(BufferedReader reader=new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				while(process.isAlive()&&!future.isDone()) {
					String out=reader.readLine();
					if(out!=null) {
						ConsoleRunner.println(out);
					}
				}
			}
			try(BufferedReader reader=new BufferedReader(new InputStreamReader(process.getErrorStream()))){
				reader.lines().forEach(line->ConsoleRunner.printErr(line));
			}
			process.destroy();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

}
