package com.gmail.berndivader.streamserver.youtube.packets;

import com.gmail.berndivader.streamserver.Helper;

public class LiveStreamPacket extends Packet {
	
	public class Id {
		public String kind;
		public String videoId;
	}
	
	public class Snippet {
		public String publishedAt;
		public String channelId;
		public String title;
		public String description;
		public String channelTitle;
		public String publishTime;
		public String liveBroadcastContent;

		public Thumbnail thumbnails;
		
		public class Thumbnail {
			public class Resolution {
				public String url;
				public int width;
				public int height;
			}
			public Resolution medium;
			public Resolution high;
		}
		
	}
	
	public Id id;
	public Snippet snippet;

	@Override
	public String toString() {
		return Helper.GSON.toJson(source);
	}
	
}
