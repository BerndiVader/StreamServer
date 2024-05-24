package com.gmail.berndivader.streamserver.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.gmail.berndivader.streamserver.term.ANSI;
import com.gmail.berndivader.streamserver.Helper;

public class GetAllScheduled implements Callable<ArrayList<String>> {
	
	public Future<ArrayList<String>>future;
	
	public GetAllScheduled() {
		
		future=Helper.executor.submit(this);
		
	}

	@Override
	public ArrayList<String> call() throws Exception {
		ArrayList<String>files=new ArrayList<>();
		try(Connection connection=DatabaseConnection.getNewConnection()) {
			try(PreparedStatement scheduled=connection.prepareStatement("SELECT * FROM `scheduled`",ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY)) {
				ResultSet result=scheduled.executeQuery();
				result.beforeFirst();
				while(result.next()) {
					files.add(result.getString("filename"));
				}
			}
		} catch (SQLException e) {
			ANSI.printErr(e.getMessage());
			ANSI.printErr("[MYSQL GET SCHEDULED LIST FAILED]");
			return null;
		}
		
		ANSI.println("\n[MYSQL GET SCHEDULED LIST SUCCSEED]");
		return files;
	}

}
