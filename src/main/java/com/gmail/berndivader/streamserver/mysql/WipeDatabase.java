package com.gmail.berndivader.streamserver.mysql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.term.ANSI;

public class WipeDatabase implements Callable<Boolean> {
	
	public Future<Boolean>future;
	
	public WipeDatabase() {
		
		future=Helper.EXECUTOR.submit(this);
		
	}

	@Override
	public Boolean call() {
		try(Connection connection=DatabaseConnection.getNewConnection()) {
			connection.setAutoCommit(false);
			try(Statement wipe=connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY)) {
				wipe.addBatch("DELETE FROM `current`;");
				wipe.addBatch("DELETE FROM `playlist`;");
				wipe.addBatch("DELETE FROM `scheduled`;");
				wipe.addBatch("DELETE FROM `downloadables`;");
				wipe.executeBatch();
				connection.commit();
			} catch(SQLException e) {
				connection.rollback();
				throw e;
			}
		} catch (SQLException e) {
			ANSI.error("Reset database failed.",e);
			return false;
		}
		return true;
	}
	
}