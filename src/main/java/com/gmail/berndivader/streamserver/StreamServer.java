package com.gmail.berndivader.streamserver;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;
import com.gmail.berndivader.streamserver.mysql.DatabaseConnection;

public class StreamServer {
	
	public static BroadcastRunner BROADCASTRUNNER;
	public static ConsoleRunner CONSOLERUNNER;
	public static Config CONFIG;
	public static DatabaseConnection DATABASECONNECTION;
	
	public static void main(String[] args) throws GeneralSecurityException, IOException, URISyntaxException, InterruptedException, ExecutionException, TimeoutException, ClassNotFoundException, SQLException {
		
		CONFIG=new Config();
		DATABASECONNECTION=new DatabaseConnection();
		BROADCASTRUNNER=new BroadcastRunner();
		CONSOLERUNNER=new ConsoleRunner();
		BROADCASTRUNNER.stop();
		Helper.close();
		
	}
	
}
