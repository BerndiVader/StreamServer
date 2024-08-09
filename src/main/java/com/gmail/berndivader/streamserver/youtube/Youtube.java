package com.gmail.berndivader.streamserver.youtube;

import java.io.IOException;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.gmail.berndivader.streamserver.term.ANSI;

public final class Youtube {

	private Youtube() {}

	static final CloseableHttpClient HTTP_CLIENT=HttpClients.createSystem();
	static final String URL="https://youtube.googleapis.com/youtube/v3/";
	
	public static void close() {
		ANSI.println("Close YouTube httpclient.");
		if(HTTP_CLIENT!=null) {
			try {
				HTTP_CLIENT.close();
			} catch (IOException e) {
				ANSI.printErr(e.getMessage(),e);
			}
		}
	}

}

