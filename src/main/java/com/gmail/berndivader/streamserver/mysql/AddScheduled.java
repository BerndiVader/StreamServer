package com.gmail.berndivader.streamserver.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.term.ANSI;

public class AddScheduled implements Runnable {
	
	String sql_insert="INSERT INTO `scheduled` (`title`, `filename`) VALUES(?, ?);";
	String sql_testfor="SELECT `filename` from `scheduled` where `filename` = ?";
	String title,filename;
	
	public AddScheduled(String filename) {
		
		this.title=filename.substring(0,filename.length()-4);
		this.filename=filename;
		
		Helper.executor.submit(this);
	}

	@Override
	public void run() {
		
		boolean exists=false;
		try(Connection connection=DatabaseConnection.getNewConnection()) {
			try(PreparedStatement testfor=connection.prepareStatement(sql_testfor,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY)) {
				testfor.setString(1, filename);
				ResultSet result=testfor.executeQuery();
				result.last();
				exists=result.getRow()>0;
			}
			if(!exists) {
				try(PreparedStatement insert=connection.prepareStatement(sql_insert,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE)) {
					insert.setString(1,title);
					insert.setString(2,filename);
					insert.execute();
				}
			}
			
		} catch (SQLException e) {
			ANSI.printErr("Failed to add a file to the scheduled playlist",e);
			return;
		}
		ANSI.println(exists?"\n[MYSQL TRACK ADDED FOR SCHEDULE]":"\n[MYSQL TRACK ALREADY ON SCHEDULE]");
	}

}
