package com.gmail.berndivader.streamserver;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.console.ConsoleRunner;
import com.gmail.berndivader.streamserver.discord.DiscordBot;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;
import com.gmail.berndivader.streamserver.mysql.DatabaseConnection;
import com.gmail.berndivader.streamserver.mysql.WipeDatabase;

public final class StreamServer {
	
	private StreamServer() {}
	
	public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
		
		new Config();
		new DatabaseConnection();
		
		if(args.length!=0) {
			switch(args[0]) {
			case"--db-wipe":
				new WipeDatabase();
				break;
			}
		}
		
		new DiscordBot();
		new BroadcastRunner();
		new ConsoleRunner();
				
		BroadcastRunner.instance.stop();
		DiscordBot.instance.close();
		Helper.close();
		
		if(ConsoleRunner.forceExit) {
			ConsoleRunner.println("[FORCE EXIT]");
			System.exit(0);
		} else {
			ConsoleRunner.println("[FINISH ALL RUNNING TASKS, THEN EXIT]");
		}
	}
	
}
