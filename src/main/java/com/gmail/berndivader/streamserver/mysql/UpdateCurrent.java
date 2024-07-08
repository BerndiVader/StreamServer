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
	
	String sql="INSERT INTO `current` (`title`, `info`) VALUES(?, ?);";
	String title,info;
	
	public Future<Boolean>future;
	
	public UpdateCurrent(String title, String info) {
		
		this.title=title;
		this.info=info;
		
		future=Helper.EXECUTOR.submit(this);
	}

	@Override
	public Boolean call() {
		
		try(Connection connection=DatabaseConnection.getNewConnection()) {
			try(PreparedStatement statement=connection.prepareStatement(sql,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY)) {
				
				if(Config.DEBUG) ANSI.println("[BEGIN MYSQL CURRENT UPDATE]");
				
				statement.addBatch("START TRANSACTION;");
				statement.addBatch("DELETE FROM `current`;");
				
				statement.setString(1,title);
				statement.setString(2,info);
				statement.addBatch();
				
				statement.addBatch("COMMIT;");
				statement.executeBatch();

			}
			
			
		} catch (SQLException e) {
			ANSI.printErr("Update now playing entry failed.",e);
			return false;
		}
		if(Config.DEBUG) ANSI.println("\n[SUCSESSFUL MYSQL CURRENT UPDATE]");
		return true;
		
	}

}
