package com.gmail.berndivader.streamserver.ffmpeg;

import com.gmail.berndivader.streamserver.Helper;

public class FFProbePacket {
	private static final String UNKNOWN="<UNKNOWN>";
	
	public String filename=UNKNOWN;
	public String format_long_name=UNKNOWN;
	public String duration=UNKNOWN;
	public String size=UNKNOWN;
	public String title=UNKNOWN;
	public String PURL=UNKNOWN;
	public String COMMENT=UNKNOWN;
	public String ARTIST=UNKNOWN;
	public String DATE=UNKNOWN;
	public String DESCRIPTION=UNKNOWN;
	public String SYNOPSIS=UNKNOWN;
	public String ENCODER=UNKNOWN;
	
	@Override
	public String toString() {
        return Helper.GSON.toJson(this);
	}	

}
