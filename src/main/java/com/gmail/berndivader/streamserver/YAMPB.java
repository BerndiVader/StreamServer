package com.gmail.berndivader.streamserver;

import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.console.ConsoleRunner;
import com.gmail.berndivader.streamserver.discord.DiscordBot;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;
import com.gmail.berndivader.streamserver.mysql.DatabaseConnection;
import com.gmail.berndivader.streamserver.term.ANSI;
import com.gmail.berndivader.streamserver.youtube.Youtube;

public final class YAMPB {
		
	private YAMPB() {}
	
	public static void main(String[] args) {
		
		ANSI.raw(Config.YAMPB_ANSI);
		Config.instance=new Config();
		
		if(Config.FFMPEG_AVAIL) ANSI.info("FFMPEG found!");
		if(Config.YTDLP_AVAIL) {
			ANSI.info("YT-DLP found!");
			Helper.updateYTDLP();
		}
		
		if(Config.FRESH_INSTALL&&!install()) {
			ANSI.warn("Failed to complete the install flow.[BR]You can configure it manual by editing the config/config.json or you can delete the config dir and try it again.");
			System.exit(0);
		}
		
		if(Config.DATABASE_USE) DatabaseConnection.instance=new DatabaseConnection();		
		if(Config.STREAM_BOT_START&&Config.FFMPEG_AVAIL) BroadcastRunner.instance=new BroadcastRunner();
		if(Config.DISCORD_BOT_START) DiscordBot.instance=new DiscordBot();

		ConsoleRunner.instance=new ConsoleRunner();

		try {
			if(BroadcastRunner.instance!=null) BroadcastRunner.instance.stop();
			if(DiscordBot.instance!=null) DiscordBot.instance.close();
		} catch (Exception e) {
			ANSI.error("Exception while shutting down.", e);
		}
		
		Helper.close();
		Youtube.close();
		
		if(ConsoleRunner.forceExit) {
			ANSI.println("[RED][FORCE EXIT][/RED]");
			System.exit(0);
		} else ANSI.println("[GREEN][FINISH ALL RUNNING TASKS, THEN EXIT][/GREEN]");
	}
	
	private static boolean install() {
		return InstallFlow.create().install().done;
	}
	
}
