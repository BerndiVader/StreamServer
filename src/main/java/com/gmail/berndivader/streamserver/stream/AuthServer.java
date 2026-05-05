package com.gmail.berndivader.streamserver.stream;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.stream.packet.AuthPacket;
import com.gmail.berndivader.streamserver.stream.packet.Packet;
import com.gmail.berndivader.streamserver.term.ANSI;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public final class AuthServer {
	
	private enum CODE {
		OK(200),
		FAILED(401),
		TIMEOUT(504);
		
		public final int value;
		
		CODE(int code) {
			value=code;
		}
		
	}
	
	public static AuthServer build() throws IOException {
		return new AuthServer();
	}
	
	private static void auth(HttpExchange exchange) throws Exception {
		
		Future<?>future=Helper.EXECUTOR.submit(()->{
			
			try(InputStream input=exchange.getRequestBody()) {
				CODE code=CODE.FAILED;
				String body=new String(input.readAllBytes(),StandardCharsets.UTF_8);
				AuthPacket packet=Packet.build(body,AuthPacket.class);
				if(packet!=null) {
					if(packet.action.equals(Config.LIVESTREAM.ACTION)) {
						if(packet.query.equals(Config.LIVESTREAM.SECRET)&&packet.protocol.equals(Config.LIVESTREAM.PROTOCOL)) {
							if(packet.path.equals(Config.LIVESTREAM.PATH+"/"+Config.LIVESTREAM.STREAM_KEY)) {
								ANSI.info(String.format("Streaming accepted from %s with ID: %s",packet.ip,packet.id));
								code=CODE.OK;
							}
						}
					} else if(packet.action.equals("read")) {
						if(packet.protocol.equals(Config.LIVESTREAM.PROTOCOL)
								&&packet.path.equals(Config.LIVESTREAM.PATH+"/"+Config.LIVESTREAM.STREAM_KEY)) {
							ANSI.info(String.format("Watcher accepted from %s with ID: %s",packet.ip,packet.id));
							code=CODE.OK;
						}
					}
				}
				exchange.sendResponseHeaders(code.value,-1);
				if(code!=CODE.OK) {
					ANSI.info(String.format("Livestream connection rejected: %s",packet.source.toString()));
				}
			} catch (IOException e) {
				ANSI.error("Authserver failed: "+e.getMessage(),e);
			} finally {
				exchange.close();
			}
			
		});
		
		try {
			future.get(5l,TimeUnit.SECONDS);
		} catch(TimeoutException e) {
			future.cancel(true);
			exchange.sendResponseHeaders(CODE.TIMEOUT.value,-1);
			exchange.close();
		} catch(Exception e1) {
			exchange.close();
			throw(e1);
		}

	}
	
	private HttpServer server;
	
	private AuthServer() throws IOException {
		
		server=HttpServer.create(new InetSocketAddress("127.0.0.1",8008),0);		
		server.setExecutor(Helper.EXECUTOR);
		server.createContext("/auth",exchange -> {
			try {
				auth(exchange);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
	
	public void start() {
		ANSI.print("[YELLOW]Starting BroadcastRunner...");
		server.start();
		ANSI.println("[GREEN]DONE![RESET]");
	}
	
	public void stop() {
		ANSI.print("[YELLOW]Stopping BroadcastRunner...");
		server.stop(10);
		ANSI.println("[GREEN]DONE![RESET]");
	}

}
