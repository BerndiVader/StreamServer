package com.gmail.berndivader.streamserver.config;

public class Livestream {
	
	
	public Boolean USE;
	public String URL;
	public String STREAM_KEY;
	public String PATH;
	public String PROTOCOL;
	public String ACTION;
	public String SECRET;
	public String WATCHER_SECRET;
	public String LIVE_TIMEOUT;
	public String WATCHER_TIMEOUT;
	
	public Livestream() {
		USE=false;
		URL="rtmp://127.0.0.1";
		STREAM_KEY="xxxx-xxxx-xxxx-xxxx-xxxx";
		PATH="live";
		PROTOCOL="rtmp";
		ACTION="publish";
		SECRET="87654321";
		WATCHER_SECRET="";
		LIVE_TIMEOUT="90";
		WATCHER_TIMEOUT="90";
	}

}
