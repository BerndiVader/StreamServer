package com.gmail.berndivader.streamserver.mysql;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.gmail.berndivader.streamserver.term.ANSI;
import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.config.Config;

public class CleanUpDownloadables implements Callable<Boolean>{
	
	private static final String SELECT_SQL="SELECT path FROM downloadables WHERE temp=true AND timestamp < UNIX_TIMESTAMP(NOW()-INTERVAL "+Config.DL_INTERVAL_VALUE+" "+Config.DL_INTERVAL_FORMAT+");";
	private static final String DELETE_SQL="DELETE FROM downloadables WHERE temp=true AND timestamp < UNIX_TIMESTAMP(NOW()-INTERVAL "+Config.DL_INTERVAL_VALUE+" "+Config.DL_INTERVAL_FORMAT+");";
	public Future<Boolean>future;
	
	public CleanUpDownloadables() {
		future=Helper.EXECUTOR.submit(this);
	}
	
	@Override
	public Boolean call() {
		try(Connection connection=DatabaseConnection.getNewConnection()) {
			connection.setAutoCommit(false);
			try(PreparedStatement statement=connection.prepareStatement(SELECT_SQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY)) {
				ResultSet result=statement.executeQuery();
				int count=0;
				if(result.first()) {
					do {
						File file=new File(result.getString("path"));
						if(file.exists()) if(!file.delete()&&Config.DEBUG) ANSI.printWarn("Failed to delete temp download file: "+file.getName());
						count++;
					} while(result.next());
				}
				if(Config.DEBUG) ANSI.println(count+" files deleted from disc.");
			}
			try(PreparedStatement statement=connection.prepareStatement(DELETE_SQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY)) {
				int dels=statement.executeUpdate();
				if(Config.DEBUG) ANSI.println(dels+" files deleted from database.");
			} catch(SQLException e) {
				connection.rollback();
				throw e;
			}
		} catch (SQLException e) {
			ANSI.printErr("Failed to cleanup downloadable database and files",e);
			return false;
		}
		return true;
	}

}
