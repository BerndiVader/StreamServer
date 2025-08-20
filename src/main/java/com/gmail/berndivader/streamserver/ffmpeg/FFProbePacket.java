package com.gmail.berndivader.streamserver.ffmpeg;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map.Entry;
import java.util.stream.Stream;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.term.ANSI;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;

public class FFProbePacket {
	private static final String UNKNOWN="<UNKNOWN>";
	
	private String filename=UNKNOWN;
	private String file=UNKNOWN;
	public Integer nb_streams=-1;
	public Integer nb_programs=-1;
	public Integer nb_stream_groups=-1;
	public String format_name=UNKNOWN;
	public String format_long_name=UNKNOWN;
	public String start_time="0";
	public String duration="0";
	public String size="0";
	public String bit_rate=UNKNOWN;
	public Integer probe_score=-1;
	public Tags tags=new Tags();
		
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
		public String date="0";
		@SerializedName(value="encoder", alternate={"ENCODER"})
		public String encoder=UNKNOWN;
		@SerializedName(value="comment", alternate={"COMMENT"})
		public String comment=UNKNOWN;
		@SerializedName(value="description", alternate={"DESCRIPTION"})
		public String description=UNKNOWN;
		@SerializedName(value="synopsis", alternate={"SYNOPSIS"})
		public String synopsis=UNKNOWN;
		@SerializedName(value="purl", alternate={"PURL"})
		public String purl="";
	}
	
	private FFProbePacket() {}
	
	public String getFileName() {
		return file;
	}
	
	public String getPath() {
		return filename;
	}
	
	@Override
	public String toString() {
        return Helper.LGSON.toJson(this);
	}
	
	public boolean isSet(String field) {
		return field!=null&&!field.equals(UNKNOWN)&&!field.isEmpty();
	}
	
	public static FFProbePacket build(File file) {
		FFProbePacket packet=new FFProbePacket();
		if(file!=null&&file.exists()) {
			ProcessBuilder builder=new ProcessBuilder();
			String path=file.getAbsolutePath();
			try {
				path=file.getCanonicalPath();
			} catch (IOException e) {
				ANSI.error(e.getMessage(),e);
			}
			builder.command(Config.DL_FFPROBE_PATH,"-v","quiet","-print_format","json","-show_format",path);
			try {
				Entry<String,String>output=Helper.startAndWaitForProcess(builder,10l);
				String out=output.getKey();
				String err=output.getValue();
				if(!err.isEmpty()) ANSI.error(err,null);
				if(!out.isEmpty()) {
					JsonObject o=JsonParser.parseString(out).getAsJsonObject();
					if(o.has("format")) {
						Tags tags=packet.tags;
						packet=Helper.LGSON.fromJson(o.get("format"),FFProbePacket.class);
						packet.tags=mergeTags(tags,packet.tags);
					}
				}
			} catch (Exception e) {
				ANSI.error("getProbePacket method failed.", e);
			}
			packet.file=file.getName();
			if(!packet.isSet(packet.tags.title)) {
				int lastDot=packet.file.lastIndexOf('.');
				packet.tags.title=lastDot!=-1?packet.file.substring(0,lastDot):packet.file;
			}
		}
		return packet;
	}
	
	private static Tags mergeTags(Tags def,Tags parsed) {
		if(parsed==null) return def;
		
		Field[]fields=Tags.class.getDeclaredFields();
		Stream.of(fields).forEach(field->{
			try {
				Object value=field.get(parsed);
				if(value!=null) field.set(def,value);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				if(Config.DEBUG) ANSI.error(e.getMessage(),e);
			}
		});
		
	    return def;
	}
	
}
