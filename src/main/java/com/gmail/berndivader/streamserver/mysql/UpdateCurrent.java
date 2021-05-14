package com.gmail.berndivader.streamserver.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.gmail.berndivader.streamserver.console.ConsoleRunner;
import com.gmail.berndivader.streamserver.Helper;

public class UpdateCurrent implements Runnable{
	
	String sql="INSERT INTO `current` (`title`, `info`) VALUES(?, ?);";
	String title,info;
	
	public UpdateCurrent(String title, String info) {
		
		this.title=title;
		this.info=info;
		
		Helper.executor.submit(this);
		
	}

	@Override
	public void run() {
		
		try(Connection connection=DatabaseConnection.getNewConnection()) {
			try(PreparedStatement statement=connection.prepareStatement(sql,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE)) {
				
				ConsoleRunner.println("[BEGIN MYSQL CURRENT UPDATE]");
				
				statement.addBatch("START TRANSACTION;");
				statement.addBatch("DELETE FROM `current`;");
				statement.executeBatch();
				statement.clearBatch();
				
				statement.setString(1,title);
				statement.setString(2,info);
				statement.execute();
				
				statement.addBatch("COMMIT;");
				statement.executeBatch();

			}
			
			
		} catch (SQLException e) {
			e.printStackTrace();
			ConsoleRunner.println("\n[FAILED MYSQL CURRENT UPDATE]");
			return;
		}
		ConsoleRunner.println("\n[SUCSESSFUL MYSQL CURRENT UPDATE]");
		
	}

}
