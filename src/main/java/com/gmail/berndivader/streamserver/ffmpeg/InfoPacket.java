package com.gmail.berndivader.streamserver.ffmpeg;

import com.google.gson.GsonBuilder;

public class InfoPacket {
		public String id;
		public String title;
		public String thumbnail;
		public String description;
		public String channel_url;
		public String webpage_url;
		public String channel;
		public String uploader;
		public String uploader_url;
		public String upload_date;
		public String duration_string;
		public String format;
		public Integer filesize_approx;
		
		@Override
		public String toString() {
	        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
		}
	}