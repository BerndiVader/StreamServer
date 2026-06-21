package com.gmail.berndivader.streamserver.stream;

import java.net.http.HttpClient;
import java.time.Duration;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.term.ANSI;

public class Clients {
		
	public static Clients build() {
		return new Clients();
	}
	
	private HttpClient http;
	
	private Clients() {
		start();
	}
		
	private void start() {
		ANSI.print("[YELLOW]Starting Httpclient...");
		http=HttpClient.newBuilder()
				.executor(Helper.EXECUTOR)
				.connectTimeout(Duration.ofSeconds(5l))
				.build();
		ANSI.println("[GREEN]DONE![RESET]");
	}
	
	public void stop() {
		if(http!=null) {
			ANSI.print("[YELLOW]Closing Httpclient...");
			http.close();
			try {
				boolean ok=http.awaitTermination(Duration.ofSeconds(10l));
				if(!ok) http.shutdownNow();
			} catch(InterruptedException e) {
				ANSI.error("Httpclient closing timout.",e);
				http.shutdownNow();
			}
			ANSI.println("[GREEN]DONE![RESET]");
		}
	}

}
