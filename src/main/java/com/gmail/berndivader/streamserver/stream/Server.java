package com.gmail.berndivader.streamserver.stream;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.stream.packet.Packet;
import com.gmail.berndivader.streamserver.stream.packet.StatusPacket;
import com.gmail.berndivader.streamserver.stream.packet.StatusPacket.STATUS;
import com.gmail.berndivader.streamserver.stream.packet.StreamPacket;
import com.gmail.berndivader.streamserver.term.ANSI;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public final class Server {
		
	private enum CODE {
		OK(200),
		FAILED(401),
		TIMEOUT(504);
		
		public final int value;
		
		CODE(int code) {
			value=code;
		}
		
	}
	
	public static Server build() throws IOException {
		return new Server();
	}
	
	private static void auth(HttpExchange exchange) throws Exception {
		
		Future<CODE>future=Helper.EXECUTOR.submit(new Callable<CODE>() {

			@Override
			public CODE call() throws Exception {
				
				CODE code=CODE.FAILED;
				try(InputStream input=exchange.getRequestBody()) {
					
					String body=new String(input.readAllBytes(),StandardCharsets.UTF_8);
					StreamPacket packet=Packet.build(body,StreamPacket.class);

					if(packet==null) {
						code=CODE.FAILED;
					} else {
						switch(packet.action) {
						case "publish":
							if(packet.query.equals(Config.LIVESTREAM.SECRET)
									&&packet.protocol.equals(Config.LIVESTREAM.PROTOCOL)
									&&packet.path.equals(Config.LIVESTREAM.PATH+"/"+Config.LIVESTREAM.STREAM_KEY)) {
								
								ANSI.info(String.format("Streamer accepted from %s with ID: %s",packet.ip,packet.id));
								code=CODE.OK;
								Live.registerStreamer(packet);
							} else {
								code=CODE.FAILED;
								packet.errReason="Credentials wrong.";
							}
							break;
						case "read":
							if(packet.protocol.equals(Config.LIVESTREAM.PROTOCOL)
									&&packet.path.equals(Config.LIVESTREAM.PATH+"/"+Config.LIVESTREAM.STREAM_KEY)
									&&(Config.LIVESTREAM.WATCHER_SECRET.isEmpty()||Config.LIVESTREAM.WATCHER_SECRET.equals(packet.query))) {

								if(Live.isLive(packet.token)) {
									ANSI.info(String.format("Watcher accepted from %s with ID: %s",packet.ip,packet.id));
									Live.registerWatcher(packet);
									code=CODE.OK;
								} else {
									code=CODE.FAILED;
									packet.errReason="No lifestream for requested path.";
								}
							} else {
								code=CODE.FAILED;
								packet.errReason="Credentials wrong.";
							}
							break;
						}
					}
					
					if(code!=CODE.OK) {
						if(packet!=null) {
							ANSI.info(String.format("Auth connection rejected: %s",packet.errReason));
						} else {
			                ANSI.error("Auth connection rejected: invalid or null packet",new Exception("StreamPacket is NULL"));
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

		String json=Live.query2jsonString(URLDecoder.decode(exchange.getRequestURI().getQuery(),StandardCharsets.UTF_8));
		StatusPacket packet=Helper.GSON.fromJson(json,StatusPacket.class);
		packet.status=STATUS.ONLINE;
		packet.validate();
		
		CODE code=Live.activateStreamer(packet.token)?CODE.OK:CODE.FAILED;
		try {
			exchange.sendResponseHeaders(code.value,-1);
		} catch(IOException e) {
			ANSI.error("Send online response failed.",e);
		}
		
		ANSI.println("ONLINE:"+Integer.toString(code.value));
		
	}

	private static void offline(HttpExchange exchange) {
		
		String json=Live.query2jsonString(URLDecoder.decode(exchange.getRequestURI().getQuery(),StandardCharsets.UTF_8));
		StatusPacket packet=Helper.GSON.fromJson(json,StatusPacket.class);
		packet.status=STATUS.OFFLINE;
		packet.validate();
		
		Live.removeStreamer(packet.token);
		CODE code=CODE.OK;
		try {
			exchange.sendResponseHeaders(code.value,-1);
		} catch(IOException e) {
			ANSI.error("Send offline response failed.",e);
		}
		
		ANSI.println("OFFLINE:"+Integer.toString(code.value));
		
	}
	
	private static void read(HttpExchange exchange) {
		
		String json=Live.query2jsonString(URLDecoder.decode(exchange.getRequestURI().getQuery(),StandardCharsets.UTF_8));
		StatusPacket packet=Helper.GSON.fromJson(json,StatusPacket.class);
		packet.status=STATUS.READ;
		packet.validate();
		
		Live.activateWatcher(packet.id);
		
		CODE code=CODE.OK;
		try {
			exchange.sendResponseHeaders(code.value,-1);
		} catch(IOException e) {
			ANSI.error("Send read response failed.",e);
		}
		
		ANSI.println("READ:"+Integer.toString(code.value));
		
	}
	
	private static void unread(HttpExchange exchange) {
		String json=Live.query2jsonString(URLDecoder.decode(exchange.getRequestURI().getQuery(),StandardCharsets.UTF_8));
		StatusPacket packet=Helper.GSON.fromJson(json,StatusPacket.class);
		packet.status=STATUS.UNREAD;
		
		packet.validate();
		Live.removeWatcher(packet.id);
		
		CODE code=CODE.OK;
		try {
			exchange.sendResponseHeaders(code.value,-1);
		} catch(IOException e) {
			ANSI.error("Send unread response failed.",e);
		}
		
		ANSI.println("UNREAD:"+Integer.toString(code.value));
	}

		
	private HttpServer http;
	
	private Server() throws IOException {
		
		http=HttpServer.create(new InetSocketAddress("127.0.0.1",8008),0);		
		http.setExecutor(Helper.EXECUTOR);
		
		http.createContext("/auth",new HttpHandler() {
			
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
		
		http.createContext("/stream/online",new HttpHandler() {
			
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
		http.createContext("/stream/offline",new HttpHandler() {
			
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
		http.createContext("/stream/read",new HttpHandler() {
			
			@Override
			public void handle(HttpExchange exchange) {
				try {
					read(exchange);
				} catch (Exception e) {
					ANSI.error(e.getMessage(),e);
				} finally {
					exchange.close();
				}
			}

		});
		http.createContext("/stream/unread",new HttpHandler() {
			
			@Override
			public void handle(HttpExchange exchange) {
				try {
					unread(exchange);
				} catch (Exception e) {
					ANSI.error(e.getMessage(),e);
				} finally {
					exchange.close();
				}
			}

		});

	}
	
	public void start() {
		ANSI.print("[YELLOW]Starting Httpserver...");
		http.start();
		ANSI.println("[GREEN]DONE![RESET]");
	}
	
	public void stop() {
		ANSI.print("[YELLOW]Stopping Httpserver...");
		http.stop(10);
		ANSI.println("[GREEN]DONE![RESET]");
	}

}
