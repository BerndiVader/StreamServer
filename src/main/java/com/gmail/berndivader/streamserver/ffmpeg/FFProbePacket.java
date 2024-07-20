package com.gmail.berndivader.streamserver.ffmpeg;

import java.nio.file.Paths;

import com.gmail.berndivader.streamserver.Helper;
import com.google.gson.annotations.SerializedName;

public class FFProbePacket {
	private static final String UNKNOWN="<UNKNOWN>";
	private static final String EMPTY="";
	
	private String filename=UNKNOWN;
	public Integer nb_streams=-1;
	public Integer nb_programs=-1;
	public Integer nb_stream_groups=-1;
	public String format_name=UNKNOWN;
	public String format_long_name=UNKNOWN;
	public String start_time=EMPTY;
	public String duration=EMPTY;
	public String size=EMPTY;
	public String bit_rate=UNKNOWN;
	public Integer probe_score=-1;
	public Tags tags;
		
	public class Tags {
		@SerializedName(value="title", alternate={"TITLE"})
		public String title=UNKNOWN;
		@SerializedName(value="artist", alternate={"ARTIST"})
		public String artist=UNKNOWN;
		@SerializedName(value="major_brand", alternate={"MAJOR_BRAND"})
		public String major_brand=UNKNOWN;
		@SerializedName(value="minor_version", alternate={"MINOR_VERSION"})
		public String minor_version=UNKNOWN;
		@SerializedName(value="compatible_brands", alternate={"COMPATIBLE_BRANDS"})
		public String compatible_brands=UNKNOWN;
		@SerializedName(value="date", alternate={"DATE"})
		public String date=EMPTY;
		@SerializedName(value="encoder", alternate={"ENCODER"})
		public String encoder=UNKNOWN;
		@SerializedName(value="comment", alternate={"COMMENT"})
		public String comment=UNKNOWN;
		@SerializedName(value="description", alternate={"DESCRIPTION"})
		public String description=UNKNOWN;
		@SerializedName(value="synopsis", alternate={"SYNOPSIS"})
		public String synopsis=UNKNOWN;
		@SerializedName(value="purl", alternate={"PURL"})
		public String purl=EMPTY;
	}
	
	public String getFileName() {
		return Paths.get(filename).getFileName().toString();
	}
	
	public String getPath() {
		return filename;
	}
	
	@Override
	public String toString() {
        return Helper.LGSON.toJson(this);
	}
	
	public boolean isSet(String field) {
		return field!=null&&!field.equals(UNKNOWN)&&!field.equals(EMPTY);
	}

}
