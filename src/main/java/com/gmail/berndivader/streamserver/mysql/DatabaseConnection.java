package com.gmail.berndivader.streamserver.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.term.ANSI;

public class DatabaseConnection {
	
	public static boolean INIT=false;
	public static DatabaseConnection instance;
	
	public DatabaseConnection() {
		DatabaseConnection.instance=this;
		ANSI.print("[BLUE]Test connection to mysql server...");
		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			ANSI.println("[GREEN]OK[/GREEN]");
			INIT=true;
		} catch (ClassNotFoundException e) {
			ANSI.println("[RED]FAILED[/RED][BR][YELLOW]Database functions disabled.[/YELLOW]");
			ANSI.printErr("Missing jdbc driver.",e);
			INIT=false;
		}
		
		if(INIT) {
			try (Connection connection=getNewConnection()) {
				try(PreparedStatement statement=connection.prepareStatement("SELECT infotext FROM ytbot.info LIMIT 1",ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY)) {
					try(ResultSet result=statement.executeQuery()) {
						if(result!=null) {
							result.first();
							INIT=result.getString("infotext").equals("YouTube Broadcast Bot Database");
							if(INIT) {
								ANSI.println("[BR][GREEN]Database found.[/GREEN]");
							} else {
								ANSI.printWarn("Not able to identify the database!");
							}
						} else {
							ANSI.printWarn("Not able to identify the database!");
							INIT=false;
						}
					}
				}
			} catch (SQLException e) {
				ANSI.printErr("Connection to database failed!",e);
				INIT=false;
			}
		}
		if(INIT) {
			try(Connection connection=getNewConnection()) {
				try(Statement statement=connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY)) {
					statement.addBatch("START TRANSACTION;");
					statement.addBatch("CREATE TABLE IF NOT EXISTS `downloadables` (`uuid` VARCHAR(36) NOT NULL, `path` VARCHAR(256) NOT NULL, `timestamp` BIGINT NOT NULL, `downloads` INT NOT NULL, `ffprobe` VARCHAR(4095));");
					statement.addBatch("COMMIT;");
					statement.executeBatch();
				}
			} catch (SQLException e) {
				ANSI.printErr("Failed to check for downloadables table.",e);
				INIT=false;
			}
		}
	}
	
	public static Connection getNewConnection() throws SQLException {
		return DriverManager.getConnection(Config.DATABASE_CONNECTION,Config.DATABASE_USER,Config.DATABASE_PWD);
	}
	
}
