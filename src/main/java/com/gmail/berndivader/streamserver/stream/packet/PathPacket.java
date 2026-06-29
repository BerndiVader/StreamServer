package com.gmail.berndivader.streamserver.stream.packet;

import com.google.gson.JsonElement;

public class PathPacket extends ApiPacket {
	
    public String name;
    public String confName;

    public Boolean ready;
    public String readyTime;

    public Boolean available;
    public String availableTime;

    public Boolean online;
    public String onlineTime;

    public Source source;

    public String[] tracks;
    public Track[] tracks2;

    public Long inboundBytes;
    public Long outboundBytes;
    public Long inboundFramesInError;

    public Long bytesReceived;
    public Long bytesSent;	
	
    public Reader[] readers;

    public static class Source {
        public String type;
        public String id;
    }

    public static class Track {
        public String codec;
        public CodecProps codecProps;
    }

    public static class CodecProps {

        public Integer width;
        public Integer height;

        public JsonElement profile;
        public JsonElement level;

        public Integer tier;

        public Integer sampleRate;
        public Integer channelCount;
    }

    public static class Reader {
        public String type;
        public String id;
    }

}
