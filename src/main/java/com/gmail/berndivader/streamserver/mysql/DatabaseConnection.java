package com.gmail.berndivader.streamserver.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.console.ConsoleRunner;

public class DatabaseConnection {
	
	public boolean INIT;
	public static DatabaseConnection instance;
	
	public DatabaseConnection() {
		instance=this;
		INIT=true;
		ConsoleRunner.print("Try connection to mysql database...");
		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			ConsoleRunner.println("FAILED!");
			ConsoleRunner.println(e.getMessage());
			INIT=false;
		}
		
		if(INIT) {
			try (Connection connection=getNewConnection()) {
				try(PreparedStatement statement=connection.prepareStatement("SELECT infotext FROM ytbot.info LIMIT 1",ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY)) {
					try(ResultSet result=statement.executeQuery()) {
						if(result!=null) {
							result.first();
							INIT=result.getString("infotext").equals("YouTube Broadcast Bot Database");
							ConsoleRunner.println(INIT?"DONE!":"FAILED!");
						} else {
							ConsoleRunner.println("FAILED!");
							ConsoleRunner.println("Not able to identify the database!");
							INIT=false;
						}
					}
				}
			} catch (SQLException e) {
				ConsoleRunner.println("FAILED!");
				ConsoleRunner.println(e.getMessage());
				INIT=false;
			}
		}
	}
	
	public static Connection getNewConnection() throws SQLException {
		return DriverManager.getConnection(Config.DATABASE_CONNECTION,Config.DATABASE_USER,Config.DATABASE_PWD);
	}
	
}
