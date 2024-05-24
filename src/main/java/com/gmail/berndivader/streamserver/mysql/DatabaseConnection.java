package com.gmail.berndivader.streamserver.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.term.ANSI;

public class DatabaseConnection {
	
	public boolean INIT;
	public static DatabaseConnection instance;
	
	public DatabaseConnection() {
		instance=this;
		INIT=true;
		ANSI.print("Try connection to mysql database...");
		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			ANSI.printErr("FAILED!");
			ANSI.printErr(e.getMessage());
			INIT=false;
		}
		
		if(INIT) {
			try (Connection connection=getNewConnection()) {
				try(PreparedStatement statement=connection.prepareStatement("SELECT infotext FROM ytbot.info LIMIT 1",ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY)) {
					try(ResultSet result=statement.executeQuery()) {
						if(result!=null) {
							result.first();
							INIT=result.getString("infotext").equals("YouTube Broadcast Bot Database");
							ANSI.println(INIT?"DONE!":"FAILED!");
						} else {
							ANSI.printErr("FAILED!");
							ANSI.printErr("Not able to identify the database!");
							INIT=false;
						}
					}
				}
			} catch (SQLException e) {
				ANSI.printErr("FAILED!");
				ANSI.printErr(e.getMessage());
				INIT=false;
			}
		}
	}
	
	public static Connection getNewConnection() throws SQLException {
		return DriverManager.getConnection(Config.DATABASE_CONNECTION,Config.DATABASE_USER,Config.DATABASE_PWD);
	}
	
}
