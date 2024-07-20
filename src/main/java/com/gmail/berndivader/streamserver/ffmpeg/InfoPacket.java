package com.gmail.berndivader.streamserver.ffmpeg;

import com.gmail.berndivader.streamserver.Helper;

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
		
		@Override
		public String toString() {
	        return Helper.LGSON.toJson(this);
		}
		
		public boolean isSet(String field) {
			return field!=null&&!field.equals(UNKNOWN)&&!field.equals(EMTPY);
		}

	}