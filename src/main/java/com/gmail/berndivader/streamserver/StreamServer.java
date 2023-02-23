package com.gmail.berndivader.streamserver;

import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.console.ConsoleRunner;
import com.gmail.berndivader.streamserver.discord.DiscordBot;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;
import com.gmail.berndivader.streamserver.mysql.DatabaseConnection;

public final class StreamServer {
		
	static {
		new Config();
		new DiscordBot();
		new DatabaseConnection();
		new BroadcastRunner();
		new ConsoleRunner();
	}
	
	private StreamServer() {}
	
	public static void main(String[] args) throws InterruptedException {
				
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
