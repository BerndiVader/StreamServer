package com.gmail.berndivader.streamserver;

import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.console.ConsoleRunner;
import com.gmail.berndivader.streamserver.discord.DiscordBot;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;
import com.gmail.berndivader.streamserver.mysql.DatabaseConnection;

public final class StreamServer {
	
	public static final BroadcastRunner BROADCASTRUNNER;
	public static final ConsoleRunner CONSOLERUNNER;
	public static final Config CONFIG;
	public static final DatabaseConnection DATABASECONNECTION;
	public static final DiscordBot DISCORDBOT;
	
	static {
		CONFIG=new Config();
		DISCORDBOT=new DiscordBot();
		DATABASECONNECTION=new DatabaseConnection();
		BROADCASTRUNNER=new BroadcastRunner();
		CONSOLERUNNER=new ConsoleRunner();
	}
	
	private StreamServer() {}
	
	public static void main(String[] args) throws InterruptedException {
				
		BROADCASTRUNNER.stop();
		DISCORDBOT.close();
		Helper.close();
		
		if(ConsoleRunner.forceExit) {
			ConsoleRunner.println("[FORCE EXIT]");
			System.exit(0);
		} else {
			ConsoleRunner.println("[FINISH ALL RUNNING TASKS, THEN EXIT]");
		}
	}
	
}
