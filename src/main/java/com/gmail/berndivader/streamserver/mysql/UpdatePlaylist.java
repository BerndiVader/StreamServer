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
import com.github.kokorin.jaffree.ffprobe.Format;
import com.gmail.berndivader.streamserver.term.ANSI;
import com.gmail.berndivader.streamserver.Helper;

public class UpdatePlaylist implements Callable<Boolean> {
	
	String sql="INSERT INTO `playlist` (`title`, `filepath`, `info`) VALUES(?, ?, ?);";
    String[]spinner=new String[] {"\u0008/", "\u0008-", "\u0008\\", "\u0008|"};
    boolean isCommand;
	
	public UpdatePlaylist(boolean fromConsole) throws InterruptedException, ExecutionException, TimeoutException {
		Future<Boolean>future=Helper.EXECUTOR.submit(this);
		
		if(isCommand=fromConsole) {
			if(future.get(20,TimeUnit.MINUTES)) {
				ANSI.println("[BR][SUCSESSFUL MYSQL PLAYLIST UPDATE]");
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
		Helper.refreshFilelist();
		File[]files=Helper.files.clone();
		
		try(Connection connection=DatabaseConnection.getNewConnection()) {
			connection.setAutoCommit(false);
			try(PreparedStatement statement=connection.prepareStatement(sql,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE)) {
				
				ANSI.println("[BEGIN MYSQL PLAYLIST UPDATE]");
				if(isCommand) {
					ANSI.print("|");
				}
				
				statement.addBatch("START TRANSACTION;");
				statement.addBatch("TRUNCATE TABLE playlist;");
				
				for(int i1=0;i1<files.length;i1++) {
					
					if(isCommand) {
						ANSI.print(spinner[i1%spinner.length]);
					}
					
					String path=files[i1].getAbsolutePath().replace("\\","/");
					String title=files[i1].getName();
					String comment="null:null:null";
					title=title.substring(0,title.length()-4);
					
					FFprobeResult result=getFFprobeResult(path);
					if(result!=null) {
						Format format=result.getFormat();
						if(format!=null) {
							comment=format.getTag("artist")+":"+format.getTag("date")+":"+format.getTag("comment");
						}
					}
					
					statement.setString(1,title);
					statement.setString(2,path);
					statement.setString(3,comment);
					statement.addBatch();
					
				}
				
				statement.addBatch("COMMIT;");
				statement.executeBatch();
				
			}
			connection.commit();
		} catch (SQLException e) {
			ANSI.printErr("Update playlist failed.",e);
			return false;
		}
		return true;
	}

}
