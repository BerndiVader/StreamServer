package com.gmail.berndivader.streamserver.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.gmail.berndivader.streamserver.term.ANSI;
import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.config.Config;

public class UpdateCurrent implements Callable<Boolean>{
	
	String sql="INSERT INTO `current` (`title`, `ffprobe`) VALUES(?, ?);";
	String title,ffprobe;
	
	public Future<Boolean>future;
	
	public UpdateCurrent(String title, String ffprobe) {
		
		this.title=title;
		this.ffprobe=ffprobe;
		
		future=Helper.EXECUTOR.submit(this);
	}

	@Override
	public Boolean call() {
		
		try(Connection connection=DatabaseConnection.getNewConnection()) {
			connection.setAutoCommit(false);
			try(PreparedStatement statement=connection.prepareStatement(sql,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY)) {
				
				if(Config.DEBUG) ANSI.info("[BEGIN MYSQL CURRENT UPDATE]");
				
				statement.addBatch("DELETE FROM `current`;");
				
				statement.setString(1,title);
				statement.setString(2,ffprobe);
				statement.addBatch();
				
				statement.executeBatch();
				connection.commit();

			} catch(SQLException e) {
				connection.rollback();
				throw e;
			}
		} catch (SQLException e) {
			ANSI.error("Update now playing entry failed.",e);
			return false;
		}
		if(Config.DEBUG) ANSI.info("[SUCSESSFUL MYSQL CURRENT UPDATE]");
		return true;
		
	}

}
