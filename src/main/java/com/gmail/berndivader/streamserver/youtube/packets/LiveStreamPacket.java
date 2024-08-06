package com.gmail.berndivader.streamserver.youtube.packets;

import java.util.List;

public class LiveStreamPacket extends Packet {

	public String kind;
	public String etag;
	public String id;
	public Snippet snippet;
	public Cdn cdn;
	public Status status;
	public ContentDetails contentDetails;

	public class Snippet {
		public String publishedAt;
		public String channelId;
		public String title;
		public String description;
		public boolean isDefaultStream;
	}

	public class Cdn {
		public String ingestionType;
		public IngestionInfo ingestionInfo;
		public String resolution;
		public String frameRate;
	}

	public class IngestionInfo {
		public String streamName;
		public String ingestionAddress;
		public String backupIngestionAddress;
	}

	public class Status {
		public String streamStatus;
		public HealthStatus healthStatus;
	}

	public class HealthStatus {
		public String status;
		public long lastUpdateTimeSeconds;
		public List<ConfigurationIssue>configurationIssues;
	}

	public class ConfigurationIssue {
		public String type;
		public String severity;
		public String reason;
		public String description;
	}

	public class ContentDetails {
		public String closedCaptionsIngestionUrl;
		public boolean isReusable;
	}    

}
