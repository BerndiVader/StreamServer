package com.gmail.berndivader.streamserver.ffmpeg;

import java.util.Map.Entry;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.term.ANSI;

public class InfoPacket {
		private static final String UNKNOWN="<UNKNOWN>";
		private static final String EMTPY="";
		
		public String id=UNKNOWN;
		public String title=UNKNOWN;
		public String thumbnail=EMTPY;
		public String description=UNKNOWN;
		public String channel_url=EMTPY;
		public String webpage_url=EMTPY;
		public String channel=UNKNOWN;
		public String uploader=UNKNOWN;
		public String uploader_url=EMTPY;
		public String upload_date=EMTPY;
		public String duration_string=EMTPY;
		public String format=UNKNOWN;
		public String filename=UNKNOWN;
		public String local_filename=UNKNOWN;
		public Integer filesize_approx=-1;
		public Boolean downloadable=false;
		public Boolean temp=false;
		public String error=UNKNOWN;
				
		private InfoPacket() {}
		
		@Override
		public String toString() {
	        return Helper.LGSON.toJson(this);
		}
		
		public boolean isSet(String field) {
			return field!=null&&!field.equals(UNKNOWN)&&!field.equals(EMTPY);
		}
		
		public static InfoPacket empty() {
			return new InfoPacket();
		}
		
		public static InfoPacket build(String url) {
			
			InfoPacket packet=new InfoPacket();
			if(url==null||url.isEmpty()) return packet;
			
			ProcessBuilder builder=new ProcessBuilder();
			builder.command(Config.DL_YTDLP_PATH
				,"--quiet"
				,"--no-warnings"
				,"--dump-json"
				,"--no-playlist"
			);
			if(Config.YOUTUBE_USE_COOKIES&&Config.cookiesExists()) {
				builder.command().add("--cookies");
				builder.command().add(Config.getCookies().getAbsolutePath());
			}
			builder.command().add(url);
			
			try {
				Entry<String,String>output=Helper.startAndWaitForProcess(builder,20l);
				String out=output.getKey();
				String err=output.getValue();
				if(!out.isEmpty()) {
					int index=out.indexOf('{');
					if(index!=-1) out=out.substring(index);
					packet=Helper.LGSON.fromJson(out,InfoPacket.class);
				}
				if(!err.isEmpty()) {
					ANSI.error(err,null);
					packet.error=err;
				}
			} catch (Exception e) {
				ANSI.error("Failed to build InfoPacket out of recieved Json string.",e);
			}
			if(!packet.isSet(packet.webpage_url)) packet.webpage_url=url;
			return packet;
		}

	}