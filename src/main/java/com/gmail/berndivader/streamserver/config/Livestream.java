package com.gmail.berndivader.streamserver.config;

public class Livestream {

	public String URL;
	public String STREAM_KEY;
	public String PATH;
	public String PROTOCOL;
	public String ACTION;
	public String SECRET;
	
	public Livestream() {
		URL="rtmp://127.0.0.1";
		STREAM_KEY="xxxx-xxxx-xxxx-xxxx-xxxx";
		PATH="live";
		PROTOCOL="rtmp";
		ACTION="publish";
		SECRET="87654321";
	}

}
