package com.gmail.berndivader.streamserver.youtube.packets;

import java.util.Map;

public class LiveBroadcastPacket extends Packet {
	public String kind;
	public String etag;
	public String id;
	public Snippet snippet;
	public Status status;
	public ContentDetails contentDetails;
	public Statistics statistics;
	public MonetizationDetails monetizationDetails;
	
	public class Snippet {
		public String publishedAt;
		public String channelId;
		public String title;
		public String description;
		public Map<String,Thumbnail>thumbnails;
		public String scheduledStartTime;
		public String scheduledEndTime;
		public String actualStartTime;
		public String actualEndTime;
		public boolean isDefaultBroadcast;
		public String liveChatId;
	}

	public class Status {
		public String lifeCycleStatus;
		public String privacyStatus;
		public String recordingStatus;
		public String madeForKids;
		public String selfDeclaredMadeForKids;
	}
	
	public class ContentDetails {
		public String boundStreamId;
		public String boundStreamLastUpdateTimeMs;
		public MonitorStream monitorStream;
		public boolean enableEmbed;
		public boolean enableDvr;
		public boolean recordFromStart;
		public boolean enableClosedCaptions;
		public String closedCaptionsType;
		public String projection;
		public boolean enableLowLatency;
		public boolean latencyPreference;
		public boolean enableAutoStart;
		public boolean enableAutoStop;
	}
	
	public class MonitorStream {
		public boolean enableMonitorStream;
		public int broadcastStreamDelayMs;
		public String embedHtml;
	}
	
	public class Statistics {
		public long totalChatCount;
	}
	
	public class MonetizationDetails {
		public CuepointSchedule cuepointSchedule;
	}
	
	public class CuepointSchedule {
		public boolean enabled;
		public String pauseAdsUntil;
		public String scheduleStrategy;
		public int repeatIntervalSecs;
	}	

}
