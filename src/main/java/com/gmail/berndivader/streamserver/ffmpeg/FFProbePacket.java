package com.gmail.berndivader.streamserver.ffmpeg;

import com.gmail.berndivader.streamserver.Helper;

public class FFProbePacket {
	private static final String UNKNOWN="<UNKNOWN>";
	
	public Format format;
	
	public class Format {
		public String filename=UNKNOWN;
		public Integer nb_streams=-1;
		public Integer nb_programs=-1;
		public Integer nb_stream_groups=-1;
		public String format_name=UNKNOWN;
		public String format_long_name=UNKNOWN;
		public String start_time=UNKNOWN;
		public String duration=UNKNOWN;
		public String size=UNKNOWN;
		public String bit_rate=UNKNOWN;
		public Integer probe_score=-1;
		public Tags tags;
	}
		
	public class Tags {
		public String title=UNKNOWN;
		public String major_brand=UNKNOWN;
		public String minor_version=UNKNOWN;
		public String compatible_brands=UNKNOWN;
		public String date=UNKNOWN;
		public String encoder=UNKNOWN;
		public String comment=UNKNOWN;
		public String description=UNKNOWN;
		public String synopsis=UNKNOWN;
		public String purl=UNKNOWN;
	}
	
	@Override
	public String toString() {
        return Helper.GSON.toJson(this);
	}
	
	public boolean isSet(String field) {
		return !field.equals(UNKNOWN);
	}

}
