package com.gmail.berndivader.streamserver;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;

public class StreamServer {
	
	static BroadcastRunner BROADCASTRUNNER;
	static ConsoleRunner CONSOLERUNNER;
	static Config CONFIG;
	public static void main(String[] args) throws GeneralSecurityException, IOException, URISyntaxException, InterruptedException, ExecutionException, TimeoutException {
		
		CONFIG=new Config();
		BROADCASTRUNNER=new BroadcastRunner();
		CONSOLERUNNER=new ConsoleRunner();
		BROADCASTRUNNER.stop();
	}
	
}
