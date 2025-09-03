package com.gmail.berndivader.streamserver.youtube.packets;

import java.util.List;
import java.util.Map;

public class VideoSnippetPacket extends Packet {

	public String kind;
	public String etag;
	public String id;
	public Snippet snippet;
	public Statistics statistics;
	public RecordingDetails recordingDetails;
	public FileDetails fileDetails;

	public static class Snippet {
		public String publishedAt;
		public String channelId;
		public String title;
		public String description;
		public Map<String, Thumbnail> thumbnails;
		public String channelTitle;
		public List<String> tags;
		public String categoryId;
		public String liveBroadcastContent;
		public String defaultLanguage;
		public Localized localized;
		public String defaultAudioLanguage;

		public static class Localized {
			public String title;
			public String description;
		}
	}

	public static class Statistics {
		public String viewCount;
		public String likeCount;
		public String dislikeCount;
		public String favoriteCount;
		public String commentCount;
	}

	public static class RecordingDetails {
		public String recordingDate;
	}

	public static class FileDetails {
		public String fileName;
		public Long fileSize;
		public String fileType;
		public String container;
		public List<VideoStream> videoStreams;
		public List<AudioStream> audioStreams;
		public Long durationMs;
		public Long bitrateBps;
		public String creationTime;

		public static class VideoStream {
			public Integer widthPixels;
			public Integer heightPixels;
			public Double frameRateFps;
			public Double aspectRatio;
			public String codec;
			public Long bitrateBps;
			public String rotation;
			public String vendor;
		}

		public static class AudioStream {
			public Integer channelCount;
			public String codec;
			public Long bitrateBps;
			public String vendor;
		}
	}

}
