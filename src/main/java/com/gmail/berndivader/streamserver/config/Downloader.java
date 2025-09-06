package com.gmail.berndivader.streamserver.config;

import java.awt.Point;

public class Downloader {

	public String YTDLP_PATH;
	public String FFMPEG_PATH;
	public String FFPROBE_PATH;
	public String ROOT_PATH;
	public String MUSIC_PATH;
	public String TEMP_PATH;
	public String MEDIA_PATH;
	public String WWW_THUMBNAIL_PATH;
	public Point THUMBNAIL_SIZE;
	
	public Long TIMEOUT_SECONDS;
	public String URL;
	public String INTERVAL_FORMAT;
	public Integer INTERVAL_VALUE;
		
	public Boolean USE_COOKIES;
	
	public Boolean USE_TOR;
	public String TOR_HOST;
	public Integer TOR_PORT;
	
	public Boolean USE_CTOR;
	public String CTOR_HOST;
	public Integer CTOR_PORT;
	public String CTOR_PWD;
	
	public Downloader() {
		YTDLP_PATH="yt-dlp";
		FFMPEG_PATH="ffmpeg";
		FFPROBE_PATH="ffprobe";
		ROOT_PATH="./library";
		MUSIC_PATH="/music";
		TEMP_PATH="/temp";
		MEDIA_PATH="/media";
		WWW_THUMBNAIL_PATH="/ABSOLUTE/PATH/TO/THUMBNAILS";
		THUMBNAIL_SIZE=new Point(640,480);
		
		TIMEOUT_SECONDS=1800l;
		URL="https://PATH.TO.PHP";
		INTERVAL_FORMAT="DAY";
		INTERVAL_VALUE=14;
		
		USE_TOR=false;
		TOR_HOST="127.0.0.1";
		TOR_PORT=9050;

		USE_CTOR=false;
		CTOR_HOST="127.0.0.1";
		CTOR_PORT=9051;
		CTOR_PWD="PWD_HASH_STRING";
					
		USE_COOKIES=false;
		
	}

}
