package com.gmail.berndivader.streamserver;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.console.ConsoleRunner;
import com.gmail.berndivader.streamserver.discord.DiscordBot;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;
import com.gmail.berndivader.streamserver.mysql.DatabaseConnection;
import com.gmail.berndivader.streamserver.mysql.WipeDatabase;
import com.gmail.berndivader.streamserver.term.ANSI;

public final class StreamServer {
	
	private StreamServer() {}
	
	public static void main(String[] args) {
		
		new Config();
		new DatabaseConnection();
		
		if(args.length!=0) {
			switch(args[0]) {
			case"--db-wipe":
				try {
					new WipeDatabase();
				} catch (InterruptedException | ExecutionException | TimeoutException e) {
					ANSI.printErr("Failed to clear MYSQL tables.", e);
				}
				break;
			}
		}
		
		new DiscordBot();
		new BroadcastRunner();
		new ConsoleRunner();

		try {
			if(BroadcastRunner.instance!=null) BroadcastRunner.instance.stop();
			if(DiscordBot.instance!=null) DiscordBot.instance.close();
		} catch (Exception e) {
			ANSI.printErr("Exception while shutting down.", e);
		}
		
		Helper.close();
		if(ConsoleRunner.forceExit) {
			ANSI.println("[RED][FORCE EXIT][/RED]");
			System.exit(0);
		} else {
			ANSI.println("[GREEN][FINISH ALL RUNNING TASKS, THEN EXIT][/GREEN]");
		}
	}
	
}
