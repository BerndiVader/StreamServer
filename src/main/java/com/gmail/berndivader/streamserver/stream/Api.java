package com.gmail.berndivader.streamserver.stream;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.stream.packet.api.ApiPacket;
import com.gmail.berndivader.streamserver.stream.packet.api.ErrorPacket;
import com.gmail.berndivader.streamserver.stream.packet.api.GeneralPacket;
import com.gmail.berndivader.streamserver.term.ANSI;
import com.gmail.berndivader.streamserver.youtube.Youtube;

public class Api {
		
	public static Api build() {
		return new Api();
	}
	
	private static CloseableHttpClient HTTP_CLIENT;
	
	private Api() {
		start();
	}
		
	private void start() {
		ANSI.print("[YELLOW]Using YT Httpclient for Live...");
		HTTP_CLIENT=Youtube.getClient();
		if(HTTP_CLIENT!=null) {
			ANSI.println("[GREEN]DONE![RESET]");
		} else {
			ANSI.println("[RED]FAILED![RESET]");
		}
	}
	
	public boolean isOnline() {
		
		HttpGet get=new HttpGet(Config.LIVESTREAM.API_URL+"/v3/info");
		try {
			ApiPacket packet=HTTP_CLIENT.execute(get,r->{
				
					int status=r.getStatusLine().getStatusCode();
					if(status==200) {
						return ApiPacket.build(EntityUtils.toString(r.getEntity()),GeneralPacket.class);
					} else if(status==500) {
						return ApiPacket.build(EntityUtils.toString(r.getEntity()),ErrorPacket.class);
					}
					return null;
					
				});
			return (packet instanceof GeneralPacket);
			
		} catch (Exception e) {
			ANSI.error(e.getMessage(),e);
		}
		
		return false;
		
	}
	
	public GeneralPacket getGeneral() {
		
		HttpGet get=new HttpGet(Config.LIVESTREAM.API_URL+"/v3/info");
		try {
			ApiPacket packet=HTTP_CLIENT.execute(get,r->{
				
					int status=r.getStatusLine().getStatusCode();
					if(status==200) {
						return ApiPacket.build(EntityUtils.toString(r.getEntity()),GeneralPacket.class);
					} else if(status==500) {
						return ApiPacket.build(EntityUtils.toString(r.getEntity()),ErrorPacket.class);
					}
					return null;
				});
			
			if(packet instanceof GeneralPacket) {
				return (GeneralPacket)packet;
			}
			if(packet instanceof ErrorPacket) {
				ANSI.error(packet.print(),null);
			}
		} catch (Exception e) {
			ANSI.error(e.getMessage(),e);
		}
		return null;
	}

}
