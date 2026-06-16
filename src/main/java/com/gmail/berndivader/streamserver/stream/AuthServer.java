package com.gmail.berndivader.streamserver.stream;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.stream.packet.AuthPacket;
import com.gmail.berndivader.streamserver.stream.packet.Packet;
import com.gmail.berndivader.streamserver.term.ANSI;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public final class AuthServer {
	
	public static AuthServer instance;
	
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
		
		Future<CODE>future=Helper.EXECUTOR.submit(new Callable<CODE>() {

			@Override
			public CODE call() throws Exception {
				
				CODE code=CODE.FAILED;
				try(InputStream input=exchange.getRequestBody()) {
					
					String body=new String(input.readAllBytes(),StandardCharsets.UTF_8);
					AuthPacket packet=Packet.build(body,AuthPacket.class);
					
					if(packet==null) {
						code=CODE.FAILED;
					} else {
						switch(packet.action) {
						case "publish":
							if(packet.query.equals(Config.LIVESTREAM.SECRET)
									&&packet.protocol.equals(Config.LIVESTREAM.PROTOCOL)
									&&packet.path.equals(Config.LIVESTREAM.PATH+"/"+Config.LIVESTREAM.STREAM_KEY)) {
								ANSI.info(String.format("Streaming accepted from %s with ID: %s",packet.ip,packet.id));
								code=CODE.OK;
							}
							break;
						case "read":
							ANSI.println(body);
							if(packet.protocol.equals(Config.LIVESTREAM.PROTOCOL)
									&&packet.path.equals(Config.LIVESTREAM.PATH+"/"+Config.LIVESTREAM.STREAM_KEY)
									&&(Config.LIVESTREAM.WATCHER_SECRET.isEmpty()||Config.LIVESTREAM.WATCHER_SECRET.equals(packet.query))) {
								ANSI.info(String.format("Watcher accepted from %s with ID: %s",packet.ip,packet.id));
								code=CODE.OK;
							}
							break;
						}
					}
					
					if(code!=CODE.OK) {
						if(packet!=null) {
							ANSI.info(String.format("Livestream connection rejected: %s",packet.source.toString()));
						} else {
			                ANSI.error("Livestream connection rejected: invalid or null packet",new Exception("AuthPacket is NULL"));
						}
					}
					
				} catch (IOException e) {
					ANSI.error("Authserver failed: "+e.getMessage(),e);
				}
				
				return code;
			}
			
		});
				
		try {
			CODE code=future.get(5l,TimeUnit.SECONDS);
			exchange.sendResponseHeaders(code.value,-1);
		} catch(TimeoutException e) {
			future.cancel(true);
			exchange.sendResponseHeaders(CODE.TIMEOUT.value,-1);
			throw(e);
		} catch(Exception e1) {
			throw(e1);
		}

	}
	

	private static void online(HttpExchange exchange) {
		
		ANSI.println("ONLINE: "+exchange.getRequestURI().getQuery());
		String query=exchange.getRequestURI().getQuery();
		
		
	}

	private static void offline(HttpExchange exchange) {
		
		ANSI.println("OFFLINE: "+exchange.getRequestURI().getQuery());
		
	}
	
		
	private HttpServer server;
	
	private AuthServer() throws IOException {
		
		server=HttpServer.create(new InetSocketAddress("127.0.0.1",8008),0);		
		server.setExecutor(Helper.EXECUTOR);
		
		server.createContext("/auth",new HttpHandler() {
			
			@Override
			public void handle(HttpExchange exchange) {
				try {
					auth(exchange);
				} catch (Exception e) {
					ANSI.error(e.getMessage(),e);
				} finally {
					exchange.close();
				}
			}

		});
		
		server.createContext("/stream/online",new HttpHandler() {
			
			@Override
			public void handle(HttpExchange exchange) {
				try {
					online(exchange);
				} catch (Exception e) {
					ANSI.error(e.getMessage(),e);
				} finally {
					exchange.close();
				}
			}

		});		
		server.createContext("/stream/offline",new HttpHandler() {
			
			@Override
			public void handle(HttpExchange exchange) {
				try {
					offline(exchange);
				} catch (Exception e) {
					ANSI.error(e.getMessage(),e);
				} finally {
					exchange.close();
				}
			}

		});		
		
	}
	
	public void start() {
		ANSI.print("[YELLOW]Starting Authserver...");
		server.start();
		ANSI.println("[GREEN]DONE![RESET]");
	}
	
	public void stop() {
		ANSI.print("[YELLOW]Stopping Authserver...");
		server.stop(10);
		ANSI.println("[GREEN]DONE![RESET]");
	}

}
