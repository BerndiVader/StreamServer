package com.gmail.berndivader.streamserver;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.ffmpeg.InfoPacket;
import com.gmail.berndivader.streamserver.term.ANSI;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class Helper {
	
	public final static ExecutorService EXECUTOR;
	public final static ScheduledExecutorService SCHEDULED_EXECUTOR;
	public final static Gson LGSON;
	public final static Gson GSON;
	
	static final Pattern EXTRACT_URL=Pattern.compile("\\b(https?)://[-A-Z0-9+&@#/%?=~_|!:,.;]*[-A-Z0-9+&@#/%=~_|]",Pattern.CASE_INSENSITIVE);

	private Helper() {}
	
	static {
		EXECUTOR=Executors.newCachedThreadPool();
		SCHEDULED_EXECUTOR=Executors.newScheduledThreadPool(1);
		GSON=new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		LGSON=new GsonBuilder().setPrettyPrinting().setFieldNamingStrategy(s->s.getName().toLowerCase()).disableHtmlEscaping().create();
	}
	
	public static Entry<String,String> startAndWaitForProcess(ProcessBuilder builder,long timeout) throws Exception {
		long start=System.currentTimeMillis();
		timeout*=1000l;
		Process process=builder.start();
		
		try(InputStream input=process.getInputStream();
			InputStream error=process.getErrorStream();
			PrintWriter output=new PrintWriter(process.getOutputStream())) {
			
			StringBuilder out=new StringBuilder();
			StringBuilder err=new StringBuilder();
			byte[]bytes=new byte[4096];
			int read=0;
			
			boolean onExit=false;
			while(process.isAlive()) {
				while(input.available()!=0) {
					if((read=input.read(bytes))!=-1) {
						out.append(new String(bytes,0,read));
						start=System.currentTimeMillis();
					}
				}
				if(error.available()!=0) err.append(new String(error.readAllBytes()));
				if(System.currentTimeMillis()-start>timeout&&!onExit) {
					onExit=true;
					waitForProcess(process,10l);
				}
			}
			
			output.println();
			output.flush();
			while(input.available()!=0) if((read=input.read(bytes))!=-1) out.append(new String(bytes,0,read));
			
			if(error.available()>0) err.append(new String(error.readAllBytes()));
			return new SimpleEntry<String,String>(out.toString(),err.toString());
		}
	}
	
	public static Entry<ProcessBuilder,InfoPacket> createDownloadBuilder(File defaultDirectory,String args) {
		return createDownloadBuilder(defaultDirectory,args,true);
	}
	
	
	public static Entry<ProcessBuilder,InfoPacket> createDownloadBuilder(File defaultDirectory,String args,boolean useInfo) {
		ProcessBuilder builder=new ProcessBuilder();
		getOrCreateMediaDir(Config.mediaPath()).ifPresentOrElse(dir->builder.directory(dir),()->builder.directory(new File("./")));
		boolean downloadable=false;
		boolean temp=false;
		
		builder.command(Config.DL_YTDLP_PATH
				,"--ignore-errors"
				,"--progress-delta","2"
				,"--restrict-filenames"
				,"--embed-metadata"
				,"--embed-thumbnail"
				,"--no-playlist"
				,"--output","%(title).64s.%(ext)s"
		);
		
		if(Config.DOWNLOADER.USE_COOKIES&&Config.cookiesExists()) {
			builder.command().add("--cookies");
			builder.command().add(Config.getCookies().getAbsolutePath());
		}
		
		String url="";
		Matcher matcher=EXTRACT_URL.matcher(args);
		if(matcher.find()) {
			url=matcher.group();
			args=matcher.replaceAll("");
		}
		
		String[]commands=args.split("--");
		
		for(String command:commands) {
			if(command.isEmpty()) continue;
						
			String[]parse=command.trim().split(" ",2);
			if(parse.length>0) {
				switch(parse[0]) {
					case("temp"):
						getOrCreateMediaDir(Config.tempPath()).ifPresent(dir->builder.directory(dir));
						temp=true;
						break;
					case("music"):
						builder.command().addAll(Arrays.asList(
								"--extract-audio"
								,"--format","bestaudio"
								,"--audio-format","mp3"
								,"--audio-quality","160K"
						));
						getOrCreateMediaDir(Config.musicPath()).ifPresent(dir->builder.directory(dir));;
						break;
					case("link"):
						downloadable=true;
						break;
					case("url"):
						if(parse.length==2&&!parse[1].isBlank()) {
							url=parse[1];
						}
						break;
					case("cookies"):
						if(!Config.DOWNLOADER.USE_COOKIES&&Config.cookiesExists()) {
							builder.command().add("--cookies");
							builder.command().add(Config.getCookies().getAbsolutePath());
						}
						break;
					case("yt"):
						temp=downloadable=false;
						builder.command().addAll(Arrays.asList(
								"--format","bestvideo[ext=mp4][vcodec=avc1]+bestaudio[ext=m4a]/best[ext=mp4]/best"
						));
						getOrCreateMediaDir(Config.PLAYLIST_PATH).ifPresent(dir->builder.directory(dir));;
						break;
					case("ytc"):
						temp=downloadable=false;
						builder.command().addAll(Arrays.asList(
								"--f","bestvideo[ext=mp4][vcodec=avc1]+bestaudio[ext=m4a]/best[ext=mp4]/best"
						));
						getOrCreateMediaDir(Config.PLAYLIST_PATH_CUSTOM).ifPresent(dir->builder.directory(dir));;
						break;
					case("tor"):
						ANSI.raw("\033[36m[INFO]Try using Tor for download... ");
						if(Config.DOWNLOADER.USE_TOR&&Config.torAccessible()) {
							ANSI.raw("OK![RESET]");
							builder.command().add("--proxy");
							builder.command().add(String.format("socks5://%s:%d",Config.DOWNLOADER.TOR_HOST,Config.DOWNLOADER.TOR_PORT));
							
							if(Config.DOWNLOADER.USE_CTOR) {
								ANSI.raw("\\033[36m[INFO]Try to change exit node... ");
								Helper.newTorCircuit();
								String torIP=Helper.getTorExitNode();
								if(torIP!=null) {
									ANSI.info(String.format("Now using IP: %s",torIP));
								} else {
									ANSI.info("Failed to change Tor IP.");
								}
							}
							
						} else {
							ANSI.info("Not able to use Tor.");
						}
						break;
					default:
						if(!parse[0].isEmpty()) builder.command().add("--"+parse[0]);
						if(parse.length==2&&!parse[1].isEmpty()) builder.command().add(parse[1]);
						break;
				}
			}
		}
		
		InfoPacket infoPacket=InfoPacket.build(url);
		if(!url.isEmpty()) builder.command().add(url);
		infoPacket.downloadable=downloadable;
		infoPacket.temp=temp;
		
		return Map.entry(builder,infoPacket);
	}
	
	public static byte[] extractImageFromMedia(File media,File dest) {
		ProcessBuilder builder=new ProcessBuilder("ffmpeg"
				,"-v","quiet"
				,"-i",media.getAbsolutePath()
				,"-map","0:v:0"
				,"-c:v"
				,"mjpeg"
				,"-f","mjpeg"
				,"-"
			);
		byte[]bytes=new byte[0];
		Process process=null;
		try {
			process=builder.start();
			
			try(InputStream in=process.getInputStream();
				ByteArrayOutputStream out=new ByteArrayOutputStream()) {
				BufferedImage bimage=ImageIO.read(in);
				
				int w,h;
				if(bimage.getWidth()>bimage.getHeight()) {
					double ratio=(double)bimage.getHeight()/bimage.getWidth();
					w=Config.DL_THUMBNAIL_SIZE.x;
					h=(int)(w*ratio);
				} else {
					double ratio=(double)bimage.getWidth()/bimage.getHeight();
					h=Config.DL_THUMBNAIL_SIZE.y;
					w=(int)(h*ratio);
				}
				
				int type=bimage.getType();
				Image temp=bimage.getScaledInstance(w,h,Image.SCALE_SMOOTH);
				bimage=new BufferedImage(w,h,type);
				Graphics2D g2d=bimage.createGraphics();
				g2d.drawImage(temp,0,0,null);
				g2d.dispose();
								
				if(dest==null) {
					if(ImageIO.write(bimage,"jpg",out)) bytes=out.toByteArray();
				} else ImageIO.write(bimage,"jpg",dest);
			}

		} catch (Exception e) {
			ANSI.error(e.getMessage(),e);
		}
		
		if(process!=null&&process.isAlive()) waitForProcess(process,10l);
		return bytes;
	}
	
	public static void waitForProcess(final Process process,long timeout) {
		Helper.EXECUTOR.submit(()->{
			try {
				process.waitFor(timeout,TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				ANSI.error(e.getMessage(),e);
				process.destroy();
			}				
		});
	}
	
	public static boolean ytdlpAvail() {
		if(!ffmpegAvail()) return false;
		ProcessBuilder builder=new ProcessBuilder(Config.DL_YTDLP_PATH,"--version");
		try {
			Process process=builder.start();
			waitForProcess(process,10l);
		} catch (Exception e) {
			ANSI.warn("YT-DLP not present from systempath.");
			return false;
		}
		return true;
	}
	
	public static boolean ffmpegAvail() {
		ProcessBuilder builder=new ProcessBuilder("ffmpeg","-version");
		try {
			Process process=builder.start();
			waitForProcess(process,10l);
		} catch (Exception e) {
			ANSI.warn("FFMPEG not present from systempath.");
			return false;
		}
		return true;
	}
	
	public static boolean updateYTDLP() {
		ProcessBuilder builder=new ProcessBuilder("yt-dlp","--update");
		try {
			Entry<String,String>output=startAndWaitForProcess(builder,10l);
			String out=output.getKey();
			String err=output.getValue();
			if(!err.isEmpty()) ANSI.warn(err);
			if(!out.isEmpty()) ANSI.info(out);
		} catch (Exception e) {
			ANSI.error("YT-DLP update went wrong!",e);
			return false;
		}
		return true;
	}
	
	public static Optional<File> getOrCreateMediaDir(String name) {
		try {
			File dir=new File(name).getCanonicalFile();
			if(!dir.exists()) dir.mkdir();
			if(dir.isDirectory()) return Optional.of(dir);
			if(Config.DEBUG) ANSI.warn("Failed to create directory: "+dir.getCanonicalPath());
		} catch(IOException e) {
			if(Config.DEBUG) ANSI.warn("Failed to get canonical path for directory: "+name);
		}
		return Optional.empty();
	}
		
	public static String stringFloatToTime(String time) {
		if(time.isEmpty()) return "";
		float duration=0f;
		try {
			duration=Float.parseFloat(time);
		} catch (Exception e) {
			ANSI.error(e.getMessage(),e);
		}
	    int h=(int)(duration/3600);
	    int m=(int)((duration%3600)/60);
	    int s=(int)(duration%60);		
	    return String.format("%02d:%02d:%02d",h,m,s);
	}
	
	public static List<String> getFilesByPath(String path,boolean sub,String glob) {
		File dir=new File(path);
		List<String>files=new ArrayList<String>();
		if(dir.exists()&&dir.isDirectory()) addFiles(dir,files,sub,glob);
		return files;
	}
	
	private static void addFiles(File dir,List<String>files,boolean sub,String glob) {
		PathMatcher matcher=FileSystems.getDefault().getPathMatcher("glob:"+glob);
		Deque<File>stack=new ArrayDeque<File>();
		stack.push(dir);

		while(!stack.isEmpty()) {
			File current=stack.pop();
			File[]found=current.listFiles();
		    if(found!=null) {
		    	Stream.of(found).forEach(file->{
		    		if(file.isFile()&&matcher.matches(Paths.get(file.getName()))) {
		    			files.add(current.getName()+"/"+file.getName());
		    		} else if(sub&&file.isDirectory()) {
		    			stack.push(file);
		    		}
		    	});
		    }
		}

	}
	
	public static boolean isUrl(String url) {
		try {
			URI uri=URI.create(url);
			String scheme=uri.getScheme();
			return scheme!=null&&(scheme.equalsIgnoreCase("http")||scheme.equalsIgnoreCase("https"));
		} catch(Exception e) {
			return false;
		}
	}
	
	public static void newTorCircuit() {
		
		Future<?>future=Helper.EXECUTOR.submit(new Runnable() {
			
			@Override
			public void run() {
				String host=Config.DOWNLOADER.CTOR_HOST;
				int port=Config.DOWNLOADER.CTOR_PORT;
				String pwd=Config.DOWNLOADER.CTOR_PWD;
				
				try(Socket socket=new Socket(host,port);
						BufferedReader reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
						PrintWriter writer=new PrintWriter(socket.getOutputStream(),true)) {
					
					writer.println(String.format("AUTHENTICATE \"%s\"",pwd));
					ANSI.raw(reader.readLine());
					
					writer.println("SIGNAL NEWNYM");
					ANSI.raw(String.format(" %s[RESET]",reader.readLine()));
					Thread.sleep(3000l);
					
				} catch(Exception e) {
					ANSI.error("Change Tor IP failed: ",e);
				}
			}
		});
		
		try {
			future.get(3200l,TimeUnit.MILLISECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			future.cancel(true);
			ANSI.error("Failed to change Tor's exit node. ",e);
		}
		
	}
	
	public static String getTorExitNode() {
		
		String ip=null;
		Future<String>future=EXECUTOR.submit(new Callable<String>() {

			@Override
			public String call() throws Exception {
				String ip=null;
				try {
					Proxy proxy=new Proxy(Proxy.Type.SOCKS,new InetSocketAddress(Config.DOWNLOADER.TOR_HOST,Config.DOWNLOADER.TOR_PORT));
					URL url=URI.create("https://api.ipify.org").toURL();
					HttpURLConnection conn=(HttpURLConnection)url.openConnection(proxy);
					conn.setRequestMethod("GET");
					
					try(BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
						ip=br.readLine();
					}
					
				} catch(Exception e) {
					ANSI.error("Failed to get Tor's exit node. ",e);
				}
				return ip;
			}
		});
		
		try {
			ip=future.get(5,TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			ANSI.error("Failed to get Tor's current exit node. ",e);
		}
		
		return ip;
		
	}
	
	public static boolean isDigit(String value) {
		return isDigit(value,null);
	}
	
	public static <T extends Number> boolean isDigit(String value,Class<T>clazz) {
		if(clazz==null) {
			return value.matches("\\d+");
		}
		try {
			if(clazz==Byte.class) {
				Byte.parseByte(value);
			} else if(clazz==Short.class) {
				Short.parseShort(value);
			} else if(clazz==Long.class) {
				Long.parseLong(value);
			} else if(clazz==Integer.class) {
				Integer.parseInt(value);
			}
		} catch(NumberFormatException e) {
			return false;
		}
		return true;
	}
	
	public static void close() {
		ANSI.print("[WHITE]Shutdown task executor...");
		EXECUTOR.shutdown();
		ANSI.println("[GREEN]DONE!");
		ANSI.print("[WHITE]Shutdown scheduled task executor...");
		SCHEDULED_EXECUTOR.shutdown();
		ANSI.println("[GREEN]DONE![/GREEN]");
	}

}
