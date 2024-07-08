package com.gmail.berndivader.streamserver.mysql;

import java.sql.BatchUpdateException;
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
	
	public static boolean setupMySql() {
		if(!INIT) return false;
		
		try(Connection connection=getNewConnection()) {
			try(Statement statement=connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY)) {
				statement.addBatch("START TRANSACTION;");
				statement.addBatch("CREATE TABLE IF NOT EXISTS `current` (`uuid` VARCHAR(512), `info` VARCHAR(512));");
				statement.addBatch("CREATE TABLE IF NOT EXISTS `info` (`infotext` VARCHAR(50));");
				statement.addBatch("CREATE TABLE IF NOT EXISTS `playlist` (`title` VARCHAR(512), `info` VARCHAR(512), `filepath` VARCHAR(512));");
				statement.addBatch("CREATE TABLE if NOT EXISTS `scheduled` (`id` INT(11) AUTO_INCREMENT, `title` VARCHAR(512), `filename` VARCHAR(512), PRIMARY KEY (`id`));");
				statement.addBatch("CREATE TABLE IF NOT EXISTS `downloadables` (`uuid` VARCHAR(36) NOT NULL, `path` VARCHAR(256) NOT NULL, `timestamp` BIGINT NOT NULL, `downloads` INT NOT NULL, `ffprobe` VARCHAR(4095));");
				statement.addBatch("TRUNCATE `current`; TRUNCATE `info`; TRUNCATE `playlist`; TRUNCATE `scheduled`; TRUNCATE `downloadables`;");
				statement.addBatch("COMMIT;");
				
				try {
					int[]results=statement.executeBatch();
					String out;
					for(int i=0;i<results.length;i++) {
						out="[YELLOW]Batchline "+i+" execute ";
						switch (results[i]) {
						case Statement.SUCCESS_NO_INFO:
							out+="succeeded with unknown changed rows.";
							break;
						case Statement.EXECUTE_FAILED:
							out+="failed!";
							break;
						default:
							out+="suceeded with "+results[i]+" rows changed.";
							break;
						}
						ANSI.println(out+"[RESET]");
					}

				} catch (BatchUpdateException e) {
					ANSI.printErr(e.getMessage(),e);
				}
				
			}
			
		} catch (SQLException e) {
			ANSI.printErr("Something went wrong while setup mysql.",e);
			return false;
		}
		
		return true;
	}
	
}
