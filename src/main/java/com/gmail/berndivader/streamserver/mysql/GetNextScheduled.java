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
				if(result.next()) {
					filename=result.getString("filename");
					int index=result.getInt("id");
					try(PreparedStatement delete=connection.prepareStatement("DELETE FROM `scheduled` WHERE `id`=?")) {
						delete.setInt(1,index);
						delete.executeUpdate();
					}
				}
			}
		} catch (SQLException e) {
			ANSI.error("Get next scheduled file failed.",e);
			return null;
		}
		
		if(Config.DEBUG) ANSI.info("[MYSQL GET NEXT SCHEDULED SUCCSEED]");
		return filename;
	}

}
