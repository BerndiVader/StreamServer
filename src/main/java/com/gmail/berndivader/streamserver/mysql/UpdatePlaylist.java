package com.gmail.berndivader.streamserver.mysql;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.gmail.berndivader.streamserver.term.ANSI;
import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;
import com.gmail.berndivader.streamserver.ffmpeg.FFProbePacket;

public class UpdatePlaylist implements Callable<Boolean> {
		
	private static final String SQL="INSERT INTO `playlist` (`title`, `filepath`, `ffprobe`) VALUES(?, ?, ?);";
    private static final String[]SPINNER=new String[] {"\u0008/", "\u0008-", "\u0008\\", "\u0008|"};
    
    private final boolean IS_COMMAND;
    private long duration;
    
    public Future<Boolean>future;
	
	public UpdatePlaylist(boolean fromConsole) throws InterruptedException, ExecutionException, TimeoutException {

		IS_COMMAND=fromConsole;
		duration=20l;
		future=Helper.EXECUTOR.submit(this);
		
		if(IS_COMMAND) {
			try {
				if(future.get(duration,TimeUnit.MINUTES)) {
					ANSI.info("[SUCESSFUL MYSQL PLAYLIST UPDATE]");
				} else {
					ANSI.warn("[FAILED MYSQL PLAYLIST UPDATE]");
				}
			} catch(TimeoutException e) {
	            ANSI.warn("UpdatePlaylist timed out after " + duration + " minutes.");
				future.cancel(true);
			}
		}
	}
	
	@Override
	public Boolean call() throws Exception {
		
		if(BroadcastRunner.PLAYLIST_UPDATE_RUNNING.get()) {
			ANSI.info("Playlist update already active, abort.[BR]");
			return false;
		}
		
		boolean ok=true;
		
		BroadcastRunner.refreshFilelist();
		File[]files=BroadcastRunner.files();
		
		try(Connection connection=DatabaseConnection.getNewConnection()) {
			connection.setAutoCommit(false);
			try(PreparedStatement statement=connection.prepareStatement(SQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY)) {
				
				if(IS_COMMAND) {
					ANSI.println("[BEGIN MYSQL PLAYLIST UPDATE]");
					ANSI.print("[GREEN]|");
				}
				
				statement.addBatch("DELETE FROM playlist;");
				
				for(int i1=0;i1<files.length;i1++) {
					
					if(IS_COMMAND) {
						ANSI.print(SPINNER[i1%SPINNER.length]);
					}
					
					FFProbePacket packet=FFProbePacket.build(files[i1]);
					String path=packet.getPath();
					String title=packet.tags.title;
					String comment=packet.toString();
					
					statement.setString(1,title);
					statement.setString(2,path);
					statement.setString(3,comment);
					statement.addBatch();
					
				}
				statement.executeBatch();
				connection.commit();
				
			} catch(SQLException e) {
				connection.rollback();
				throw e;
			}
		} catch (Exception e) {
			ANSI.error("Update playlist failed.",e);
			ok=false;
		}
		return ok;
	}

}
