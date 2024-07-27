package com.gmail.berndivader.streamserver.ffmpeg;

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
		
		private InfoPacket() {}
		
		@Override
		public String toString() {
	        return Helper.LGSON.toJson(this);
		}
		
		public boolean isSet(String field) {
			return field!=null&&!field.equals(UNKNOWN)&&!field.equals(EMTPY);
		}
		
		public static InfoPacket build(String url) {
			ProcessBuilder builder=new ProcessBuilder();
			builder.command("yt-dlp"
				,"--quiet"
				,"--no-warnings"
				,"--dump-json"
				,"--no-playlist"
			);
			if(Config.YOUTUBE_USE_COOKIES&&Config.YOUTUBE_COOKIES.exists()) {
				builder.command().add("--cookies");
				builder.command().add(Config.YOUTUBE_COOKIES.getAbsolutePath());
			}
			builder.command().add(url);
			
			InfoPacket info=new InfoPacket();
			try {
				String out=Helper.startAndWaitForProcess(builder,20l);
				if(!out.isEmpty()) {
					int index=out.indexOf('{');
					if(index!=-1) out=out.substring(index);
					info=Helper.LGSON.fromJson(out,InfoPacket.class);
				}
			} catch (Exception e) {
				ANSI.printErr("getinfoPacket method failed.",e);
			}
			
			if(!info.isSet(info.webpage_url)) info.webpage_url=url;
			return info;
		}

	}