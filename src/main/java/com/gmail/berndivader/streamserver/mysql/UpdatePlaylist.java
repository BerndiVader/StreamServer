package com.gmail.berndivader.streamserver.mysql;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;

import com.github.kokorin.jaffree.ffprobe.FFprobe;
import com.github.kokorin.jaffree.ffprobe.FFprobeResult;
import com.github.kokorin.jaffree.ffprobe.Format;
import com.gmail.berndivader.streamserver.ConsoleRunner;
import com.gmail.berndivader.streamserver.Helper;

public class UpdatePlaylist implements Callable<Boolean> {
	
	String sqlInsert="INSERT INTO `playlist` (`title`, `filepath`, `info`) VALUES(?, ?, ?);";
	
	public UpdatePlaylist() {
		Helper.executor.submit(this);
	}
	
	static FFprobeResult getFFprobeResult(String path) {
		return FFprobe.atPath()
				.setInput(path)
				.setShowFormat(true)
				.execute();		
	}

	@Override
	public Boolean call() throws Exception {
		File[]files=Helper.files.clone();
		
		try(Connection connection=DatabaseConnection.getNewConnection()) {
			try(PreparedStatement statement=connection.prepareStatement(sqlInsert,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE)) {
				
				ConsoleRunner.println("[BEGIN MYSQL PLAYLIST UPDATE]");
				
				statement.addBatch("START TRANSACTION;");
				statement.addBatch("DELETE FROM `playlist`;");
				statement.executeBatch();
				statement.clearBatch();
				
				for(int i1=0;i1<files.length;i1++) {
					
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
					statement.setString(2, path);
					statement.setString(3, comment);
					statement.execute();
					
				}
				
				statement.addBatch("COMMIT;");
				statement.executeBatch();
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		ConsoleRunner.println("[END MYSQL PLAYLIST UPDATE]");
		return true;
	}

}
