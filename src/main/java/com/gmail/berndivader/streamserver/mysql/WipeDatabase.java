package com.gmail.berndivader.streamserver.mysql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.term.ANSI;

public class WipeDatabase implements Runnable {
	
	public WipeDatabase() throws InterruptedException, ExecutionException, TimeoutException {
		
		Future<?>future=Helper.EXECUTOR.submit(this);
		future.get(10,TimeUnit.SECONDS);
		
	}

	@Override
	public void run() {
		try(Connection connection=DatabaseConnection.getNewConnection()) {
			try(Statement wipe=connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY)) {
				wipe.addBatch("START TRANSACTION;");
				wipe.addBatch("TRUNCATE TABLE `current`;");
				wipe.addBatch("TRUNCATE TABLE `playlist`;");
				wipe.addBatch("TRUNCATE TABLE `scheduled`;");
				wipe.addBatch("COMMIT;");
				wipe.executeBatch();
			}
		} catch (SQLException e) {
			ANSI.printErr("Reset database failed.",e);
		}
	}
	
}