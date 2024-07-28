package com.gmail.berndivader.streamserver;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.console.ConsoleRunner;
import com.gmail.berndivader.streamserver.discord.DiscordBot;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;
import com.gmail.berndivader.streamserver.mysql.DatabaseConnection;
import com.gmail.berndivader.streamserver.mysql.DatabaseConnection.STATUS;
import com.gmail.berndivader.streamserver.mysql.WipeDatabase;
import com.gmail.berndivader.streamserver.term.ANSI;
import com.gmail.berndivader.streamserver.youtube.Youtube;

public final class StreamServer {
	
	private StreamServer() {}
	
	public static void main(String[] args) {
		
		Config.instance=new Config();
		DatabaseConnection.instance=new DatabaseConnection();
				
		if(args.length>0) {
			Arrays.stream(args).forEach(arg->{
				switch(args[0]) {
				case"--db-wipe":
					if(DatabaseConnection.status==STATUS.OK) {
						try {
							WipeDatabase wipe=new WipeDatabase();
							wipe.future.get(20l,TimeUnit.SECONDS);
						} catch (InterruptedException | ExecutionException | TimeoutException e) {
							ANSI.printErr("Failed to clear MYSQL tables.", e);
						}
					}
					break;
				}
			});
		}
		
		if(Config.STREAM_BOT_START) BroadcastRunner.instance=new BroadcastRunner();
		if(Config.DISCORD_BOT_START) DiscordBot.instance=new DiscordBot();
		
		ConsoleRunner.instance=new ConsoleRunner();

		try {
			if(BroadcastRunner.instance!=null) BroadcastRunner.instance.stop();
			if(DiscordBot.instance!=null) DiscordBot.instance.close();
		} catch (Exception e) {
			ANSI.printErr("Exception while shutting down.", e);
		}
		
		Helper.close();
		Youtube.close();
		
		if(ConsoleRunner.forceExit) {
			ANSI.println("[RED][FORCE EXIT][/RED]");
			System.exit(0);
		} else ANSI.println("[GREEN][FINISH ALL RUNNING TASKS, THEN EXIT][/GREEN]");
	}
	
}
