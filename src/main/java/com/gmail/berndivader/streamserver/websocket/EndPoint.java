package com.gmail.berndivader.streamserver.websocket;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.ffmpeg.InfoPacket;
import com.gmail.berndivader.streamserver.mysql.MakeDownloadable;
import com.gmail.berndivader.streamserver.term.ANSI;

import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint(value="/dl")
public class EndPoint {
	
	private enum PACKET {
		
		ACCEPT("{%ACPT%}"),
		INFO("{%INF%}"),
		INFOPACKET("{%INFPKT%}"),
		READY("{%RDY%}"),
		ERROR("{%ERR%}");
		
		public final String value;
		
		PACKET(String string) {
			this.value=string;
		}
		
	}
	
	private Session session;
	private boolean flowRunning=false;
	
	public EndPoint() {}
	
	@OnOpen
	public void onOpen(Session session) {
		this.session=session;
		ANSI.info(session.getRequestURI().toASCIIString());
		ANSI.info("WebSocket client connected.");
		ANSI.prompt();
		Helper.EXECUTOR.submit(new PingRunner());
	}
	
	@OnMessage
	public void onMessage(String content) throws IOException {
		if(flowRunning) return;
		
		if(Helper.isUrl(content)) {
			flowRunning=true;
			session.getBasicRemote().sendText(PACKET.ACCEPT.value);
			download(content);
		}
		session.close();
	}
	
	@OnError
	public void onError(Throwable throwable) throws IOException {
		ANSI.error("WebSocket created an error: ",throwable);
		ANSI.prompt();
		session.close(new CloseReason(CloseReason.CloseCodes.PROTOCOL_ERROR,"WebSocket Error."));
	}
	
	@OnClose
	public void onClose(CloseReason reson) {
		ANSI.info(String.format("WS-CLIENT connection closed. Code: %d, Reason: %s",reson.getCloseCode().getCode(),reson.getReasonPhrase()));
		ANSI.prompt();
	}
	
	private void download(String content) throws IOException {
		try {
			Entry<ProcessBuilder,InfoPacket>entry=getDownloader(content);
			ProcessBuilder builder=entry.getKey();
			InfoPacket info=entry.getValue();
			
			if(info.isSet(info.error)) {
				if(session!=null&&session.isOpen()) session.getBasicRemote().sendText(PACKET.ERROR.value+info.error);
			} else {
				if(session!=null&&session.isOpen()) session.getBasicRemote().sendText(PACKET.INFOPACKET.value+info.toString());
				Process process=builder.start();
				try(InputStream input=process.getInputStream();
					BufferedReader error=process.errorReader()) {
					
					long time=System.currentTimeMillis();
					boolean onExit=false;
					
					while(process.isAlive()) {
						if(!onExit&&(session==null||!session.isOpen())) {
							ANSI.raw("[BR]");
							ANSI.raw("Download process terminated because it appears, ws-client is gone.");
							info.error="Download terminated because client gone.";
							process.destroy();
						} else {
							int avail=input.available();
							if(avail>0) {
								time=System.currentTimeMillis();
								String line=new String(input.readNBytes(avail)).trim();
								if(line.contains("[Metadata]")) {
									String[]temp=line.split("\"");
									if(temp.length>0) info.local_filename=temp[1];						
								}
								if(Config.DEBUG) {
									session.getAsyncRemote().sendText(PACKET.INFO.value+line);
								} else {
									if(line.toLowerCase().startsWith("[download]")) session.getAsyncRemote().sendText(line);
								}
							}
							if(System.currentTimeMillis()-time>Config.DL_TIMEOUT_SECONDS*1000l){
								ANSI.raw("[BR]");
								ANSI.raw(String.format("Download process terminated because it appears, process is stalled since %n minutes.",(long)(Config.DL_TIMEOUT_SECONDS/60)));
								process.destroy();
								info.error="Download terminated because process is stalled.";
							}
						}
					}
					
					if(error!=null&&error.ready()) {
						ANSI.raw("[BR]");
						error.lines().forEach(line->ANSI.warn(line));
					}
				}
				
				if(process.isAlive()) process.destroy();
				ANSI.raw("[BR]");
				
				if(info.downloadable&&Config.DATABASE_USE&&!info.isSet(info.error)) {
					File file=new File(builder.directory().getAbsolutePath()+"/"+info.local_filename);
					if(file.exists()&&file.isFile()&&file.canRead()) {
						MakeDownloadable downloadable=new MakeDownloadable(file,info.temp);
						Optional<String>optLink=downloadable.future.get(2,TimeUnit.MINUTES);
						optLink.ifPresentOrElse(link->{
							if(session!=null&&session.isOpen()) session.getAsyncRemote().sendText(PACKET.READY.value+link);
						},()->{
							if(session!=null&&session.isOpen()) session.getAsyncRemote().sendText(PACKET.ERROR.value+"Failed to create downloadable file.");
						});
					} else if(session!=null&&session.isOpen()) {
						session.getAsyncRemote().sendText(PACKET.ERROR.value+"Downloaded file not found or readable.");
					}
				}
				
			}
		} catch (Exception e) {
			if(session!=null&&session.isOpen()) session.getBasicRemote().sendText(PACKET.ERROR.value+"Download failed.");
			ANSI.raw("[BR]");
			ANSI.error("Error while looping yt-dlp process.",e);
		}
		if(session!=null) session.close();
	}
	
	private static Entry<ProcessBuilder,InfoPacket> getDownloader(String url) {
		Optional<File>opt=Helper.getOrCreateMediaDir(Config.DL_ROOT_PATH);
		if(opt.isEmpty()) return null;
		File directory=opt.get();
		return Helper.createDownloadBuilder(directory,String.format("--tor --link --temp %s",url));
	}
	
	public class PingRunner implements Runnable {
		
		long interval=1000l;

		@Override
		public void run() {
			while(session.isOpen()) {
				try {
					session.getBasicRemote().sendPing(ByteBuffer.allocate(0));
				} catch (IllegalArgumentException | IOException e) {
					try {
						session.close(new CloseReason(CloseReason.CloseCodes.CLOSED_ABNORMALLY,"Ping failed."));
					} catch (IOException e1) {
						ANSI.error("Ping fail: Close session failed.",e);
						ANSI.prompt();
					}
					break;
				}
			    try {
					Thread.sleep(interval);
				} catch (InterruptedException e) {
					ANSI.error("Ping fail: Thread interrupted.",e);
					ANSI.prompt();
				}
			}
		}
		
	}
	
}