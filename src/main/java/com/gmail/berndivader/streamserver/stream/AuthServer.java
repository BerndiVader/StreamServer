package com.gmail.berndivader.streamserver.stream;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.term.ANSI;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public final class AuthServer {
	
	public static AuthServer build() throws IOException {
		return new AuthServer();
	}
	
	private static void auth(HttpExchange exchange) throws Exception {
		
		Future<?>future=Helper.EXECUTOR.submit(()->{
			try(InputStream input=exchange.getRequestBody()) {
				String body=new String(input.readAllBytes(),StandardCharsets.UTF_8);
				ANSI.print(body);
				exchange.sendResponseHeaders(401,-1);
			} catch (IOException e) {
				// dummy
			} finally {
				exchange.close();
			}			
		});
		
		try {
			future.get(5l,TimeUnit.SECONDS);
		} catch(TimeoutException e) {
			future.cancel(true);
			exchange.sendResponseHeaders(504,-1);
			exchange.close();
		} catch(Exception e1) {
			exchange.close();
			throw(e1);
		}

	}
	
	private HttpServer server;
	
	private AuthServer() throws IOException {
		
		server=HttpServer.create(new InetSocketAddress("127.0.0.1",8800),0);		
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
		
		server.stop(30);
		
	}
	

}
