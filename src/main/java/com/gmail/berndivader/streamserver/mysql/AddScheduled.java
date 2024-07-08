package com.gmail.berndivader.streamserver.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.term.ANSI;

public class AddScheduled implements Callable<Boolean> {
	
	private static final String SQL_INSERT="INSERT INTO `scheduled` (`title`, `filename`) VALUES(?, ?);";
	private static final String SQL_TESTFOR="SELECT `filename` from `scheduled` where `filename` = ?";
	private String title,filename;
	public Future<Boolean>future;
	
	public AddScheduled(String filename) {
		
		this.title=filename.substring(0,filename.length()-4);
		this.filename=filename;
		
		future=Helper.EXECUTOR.submit(this);
	}

	@Override
	public Boolean call() {
		
		boolean exists=false;
		try(Connection connection=DatabaseConnection.getNewConnection()) {
			try(PreparedStatement testfor=connection.prepareStatement(SQL_TESTFOR,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY)) {
				testfor.setString(1, filename);
				ResultSet result=testfor.executeQuery();
				result.last();
				exists=result.getRow()>0;
			}
			if(!exists) {
				try(PreparedStatement insert=connection.prepareStatement(SQL_INSERT,ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY)) {
					insert.setString(1,title);
					insert.setString(2,filename);
					insert.execute();
				}
			}
			
		} catch (SQLException e) {
			ANSI.printErr("Failed to add a file to the scheduled playlist",e);
			return false;
		}
		if(Config.DEBUG) ANSI.println(exists?"\n[MYSQL TRACK ADDED FOR SCHEDULE]":"\n[MYSQL TRACK ALREADY ON SCHEDULE]");
		return true;
	}

}
