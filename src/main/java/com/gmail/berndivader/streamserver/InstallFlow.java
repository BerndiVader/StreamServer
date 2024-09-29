package com.gmail.berndivader.streamserver;

import java.io.Console;
import java.util.Arrays;

import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.mysql.DatabaseConnection;
import com.gmail.berndivader.streamserver.mysql.DatabaseConnection.STATUS;
import com.gmail.berndivader.streamserver.term.ANSI;

public class InstallFlow {
	
	public boolean done=false;
	private InstallFlow() {}
	
	public static InstallFlow create() {
		return new InstallFlow();
	}
	
	public InstallFlow install() {
		
		try {
			ANSI.raw(Config.YAMPB_ANSI);
			ANSI.println("[WHITE]WELCOME TO THE YAMPB INSTALLING FLOW![RESET]");
			ANSI.println("[BLUE]=================================[BR]");

			if(ask("Use MySql? [CYAN][YES/no]","yes").equalsIgnoreCase("yes")) {
				do {
					if(!installDB()) {
						String ask=ask("Failed to install database. Retry? [YES/no]","yes");
						if(ask.equalsIgnoreCase("no")) break;
					} else break;
				} while(true);
			} else {
				Config.DATABASE_USE=false;
			}
			
			if(ask("[BR]Config YT broadcaster? [CYAN][YES/no]","yes").equalsIgnoreCase("yes")) {
				do {
					if(!installBC()) {
						String ask=ask("Failed to setup yt broadcaster. Retry? [YES/no]","yes");
						if(ask.equalsIgnoreCase("no")) break;
					} else break;
				} while(true);
			} else {
				Config.STREAM_BOT_START=false;
			}
			
			if(ask("[BR]Config Discord bot? [CYAN][YES/no]","yes").equalsIgnoreCase("yes")) {
				do {
					if(!installDC()) {
						String ask=ask("Failed to setup Discord bot. Retry? [YES/no]","yes");
						if(ask.equalsIgnoreCase("no")) break;
					} else break;
				} while(true);
			} else {
				Config.DISCORD_BOT_START=false;
			}		
			
			Config.saveConfig();
			done=true;
			
		} catch(Exception e) {
			ANSI.error("Failed to run throu installation flow.",e);
			done=false;
		}
		
		return this;
	}
	
	private boolean installDB() {
		boolean use=false;
		
		Config.DATABASE_HOST=ask(String.format("MySQL hostname? [CYAN][%s]",Config.DATABASE_HOST),Config.DATABASE_HOST);
		Config.DATABASE_PORT=ask(String.format("MySQL port? [CYAN][%s]",Config.DATABASE_PORT),Config.DATABASE_PORT);
		Config.DATABASE_NAME=ask(String.format("MySQL databasename? [CYAN][%s]",Config.DATABASE_NAME),Config.DATABASE_NAME);
		Config.DATABASE_USER=ask(String.format("MySQL username? [CYAN][%s]",Config.DATABASE_USER),Config.DATABASE_USER);
		Config.DATABASE_PWD=askPwd("MySQL password? [CYAN][blank for current]",Config.DATABASE_PWD);
		
		STATUS test=DatabaseConnection.testInstall();
		ANSI.info(test.msg());
		
		switch(test) {
		case OK:
		case DB_CORRUPT_ERROR:
		case DB_TABLE_NOTFOUND_ERROR:
			use=DatabaseConnection.setup();
			break;
		default:
			use=false;
			break;
		}
		
		return Config.DATABASE_USE=use;
	}
	
	private boolean installBC() {
		boolean use=true;
		
		Config.YOUTUBE_API_KEY=ask(String.format("Youtube API key? [CYAN][%s]",Config.YOUTUBE_API_KEY),Config.YOUTUBE_API_KEY);
		Config.YOUTUBE_CLIENT_ID=ask(String.format("OAuth2 client id? [CYAN][%s]",Config.YOUTUBE_CLIENT_ID),Config.YOUTUBE_CLIENT_ID);
		Config.YOUTUBE_CLIENT_SECRET=ask(String.format("OAuth2 client secret? [CYAN][%s]",Config.YOUTUBE_CLIENT_SECRET),Config.YOUTUBE_CLIENT_SECRET);
		Config.YOUTUBE_AUTH_REDIRECT=ask(String.format("OAuth2 redirect page? [CYAN][%s]",Config.YOUTUBE_AUTH_REDIRECT),Config.YOUTUBE_AUTH_REDIRECT);
		
		Config.YOUTUBE_STREAM_KEY=ask(String.format("Livestream key? [CYAN][%s]",Config.YOUTUBE_STREAM_KEY),Config.YOUTUBE_STREAM_KEY);
		Config.YOUTUBE_STREAM_URL=ask(String.format("Livestream rtmp url? [CYAN][%s]",Config.YOUTUBE_STREAM_URL),Config.YOUTUBE_STREAM_URL);
		Config.BROADCAST_DEFAULT_PRIVACY=ask(String.format("Livestream default privacy?[BR]Possible values: PUBLIC|UNLISTED|PRIVATE [CYAN][%s]",Config.BROADCAST_DEFAULT_PRIVACY),Config.BROADCAST_DEFAULT_PRIVACY);
		
		return Config.STREAM_BOT_START=use;
	}
	
	private boolean installDC() {
		boolean use=true;
		
		ANSI.println("[BLUE]To get/create token visit: https://discord.com/developers/applications");
		Config.DISCORD_TOKEN=ask(String.format("Discord bot token? [CYAN][%s]",Config.DISCORD_TOKEN),Config.DISCORD_TOKEN);

		if(Config.DISCORD_MUSIC_BOT=ask("Use music bot? [CYAN][yes/NO]","no").equalsIgnoreCase("yes")) {
			Config.DISCORD_VOICE_CHANNEL_NAME=ask(String.format("Voice channel by name? [CYAN][%s]",Config.DISCORD_VOICE_CHANNEL_NAME),Config.DISCORD_VOICE_CHANNEL_NAME);
			Config.DISCORD_MUSIC_AUTOPLAY=ask("Use music bot? [CYAN][yes/NO]","no").equalsIgnoreCase("yes");
		}

		return Config.DISCORD_BOT_START=use;
	}
	
	private static String ask(String question,String defauld) {
		ANSI.print(String.format("[YELLOW]%s [WHITE]",question));
		String answer=ANSI.keyboard.nextLine();
		if(answer.isEmpty()||answer.equals(defauld)) return defauld;
		return answer;
	}
	
	private static String askPwd(String question,String defauld) {
		Console console=System.console();
		if(console==null) return defauld;
		ANSI.print(String.format("[YELLOW]%s [WHITE]",question));
	    char[]chars=console.readPassword();
	    String pwd=new String(chars);
	    Arrays.fill(chars,' ');
	    return pwd;
	}
	
	private static boolean isLong(String value) {
		return value.matches("\\d+");
	}
	
}
