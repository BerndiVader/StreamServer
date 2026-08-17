package com.gmail.berndivader.streamserver.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.term.ANSI;

public class GetPlaylist implements Callable <ArrayList<String>>{
	
	public Future<ArrayList<String>>future;
	
	public GetPlaylist() {
		future=Helper.EXECUTOR.submit(this);
	}

	@Override
	public ArrayList<String> call() throws Exception {
		
		ArrayList<String>files=new ArrayList<>();
		try(Connection connection=DatabaseConnection.getNewConnection()) {
			try(PreparedStatement statement=connection.prepareStatement("SELECT `filepath` FROM `playlist`",ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY)) {
				ResultSet result=statement.executeQuery();
				while(result.next()) {
					files.add(result.getString("filepath"));
				}
			}
		} catch(SQLException e) {
			ANSI.error("Get playlist failed.",e);
		}
		
		return files;
	}

}
