package com.gmail.berndivader.streamserver.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.gmail.berndivader.streamserver.term.ANSI;
import com.gmail.berndivader.streamserver.Helper;

public class GetNextScheduled implements Callable<String> {
	
	public Future<String>future;
	
	public GetNextScheduled() {
		
		future=Helper.EXECUTOR.submit(this);
		
	}

	@Override
	public String call() throws Exception {
		String filename=null;
		try(Connection connection=DatabaseConnection.getNewConnection()) {
			try(PreparedStatement scheduled=connection.prepareStatement("SELECT * FROM `scheduled` LIMIT 1",ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY)) {
				ResultSet result=scheduled.executeQuery();
				result.last();
				if(result.getRow()>0) {
					filename=result.getString("filename");
					int index=result.getInt("id");
					scheduled.addBatch("DELETE FROM `scheduled` WHERE `id` = "+index);
					scheduled.executeBatch();
				}
			}
		} catch (SQLException e) {
			ANSI.printErr("Get next scheduled file failed.",e);
			return null;
		}
		
		ANSI.println("\n[MYSQL GET NEXT SCHEDULED SUCCSEED]");
		return filename;
	}

}
