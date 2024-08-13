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

import com.github.kokorin.jaffree.ffprobe.FFprobe;
import com.github.kokorin.jaffree.ffprobe.FFprobeResult;
import com.gmail.berndivader.streamserver.term.ANSI;
import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;
import com.gmail.berndivader.streamserver.ffmpeg.FFProbePacket;

public class UpdatePlaylist implements Callable<Boolean> {
	
	static final String SQL="INSERT INTO `playlist` (`title`, `filepath`, `info`) VALUES(?, ?, ?);";
    static final String[]SPINNER=new String[] {"\u0008/", "\u0008-", "\u0008\\", "\u0008|"};
    final boolean IS_COMMAND;
	
	public UpdatePlaylist(boolean fromConsole) throws InterruptedException, ExecutionException, TimeoutException {
		Future<Boolean>future=Helper.EXECUTOR.submit(this);
		
		if(IS_COMMAND=fromConsole) {
			if(future.get(20,TimeUnit.MINUTES)) {
				ANSI.println("[BR][SUCESSFUL MYSQL PLAYLIST UPDATE]");
			} else {
				ANSI.printWarn("[BR][FAILED MYSQL PLAYLIST UPDATE]");
			}
		}
	}
	
	static FFprobeResult getFFprobeResult(String path) {
		return FFprobe.atPath()
				.setInput(path)
				.setShowFormat(true)
				.execute();
	}

	@Override
	public Boolean call() throws Exception {
		BroadcastRunner.refreshFilelist();
		File[]files=BroadcastRunner.getFiles().clone();
		
		try(Connection connection=DatabaseConnection.getNewConnection()) {
			connection.setAutoCommit(false);
			try(PreparedStatement statement=connection.prepareStatement(SQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY)) {
				
				ANSI.println("[BEGIN MYSQL PLAYLIST UPDATE]");
				if(IS_COMMAND) ANSI.print("[GREEN]|");
				
				statement.addBatch("START TRANSACTION;");
				statement.addBatch("TRUNCATE TABLE playlist;");
				
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
				
				statement.addBatch("COMMIT;");
				statement.executeBatch();
				
			} catch(SQLException e) {
				connection.rollback();
				throw e;
			}
		} catch (Exception e) {
			ANSI.printErr("Update playlist failed.",e);
			return false;
		}
		return true;
	}

}
