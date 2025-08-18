package com.gmail.berndivader.streamserver.websocket;

import org.glassfish.tyrus.server.Server;

import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.term.ANSI;


public final class WebSocket {
	
	private static final String WEBSOCKET_PATH="/yampb";
	private static final int PORT;
	private static final String HOST;
	
	private static Server server;
	
	static {
		PORT=Config.WEBSOCKET.PORT;
		HOST=Config.WEBSOCKET.HOST;
	}
	
	private WebSocket() {}
	
	public static void start() {
		
		if (server==null) {
			server=new Server(HOST,PORT,WEBSOCKET_PATH,null,EndPoint.class);
			try {
				server.start();
				ANSI.println("[GREEN]WebSocket server started on port "+PORT+ANSI.RESET);
			} catch (Exception e) {
				ANSI.error("Failed to start WebSocket server:",e);
				server=null;
				return;
			}
		}
	}
	
	public static void close() {
		if(server!=null) {
			ANSI.info("Stopping Websocket Server....");
			server.stop();
			ANSI.info("done!");
			ANSI.prompt();
		}
	}
		
}
