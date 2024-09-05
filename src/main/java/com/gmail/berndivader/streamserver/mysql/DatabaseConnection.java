package com.gmail.berndivader.streamserver.mysql;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.term.ANSI;

public class DatabaseConnection {
	
	public enum STATUS {
		OK,
		SERVER_NOT_FOUND,
		SERVER_CONNECTION_FAILED,
		DB_NOT_FOUND,
		DB_CONNECTION_FAILED,
		UNKNOWN
	}
	
	public static STATUS status=STATUS.UNKNOWN;
	public static DatabaseConnection instance;
	
	public DatabaseConnection() {
		ANSI.print("[BLUE]Test connection to mysql server...");
		
		Future<STATUS>future=Helper.EXECUTOR.submit(()->{
			try {
				Class.forName("com.mysql.cj.jdbc.Driver");
				try (Connection connection=getNewConnection()) {
					try(PreparedStatement statement=connection.prepareStatement("SELECT infotext FROM ytbot.info LIMIT 1",ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY)) {
						try(ResultSet result=statement.executeQuery()) {
							if(result.first()) {
								if(result.getString("infotext").equals("YouTube Broadcast Bot Database")) {
									status=STATUS.OK;
								} else {
									status=STATUS.DB_NOT_FOUND;
								}
							} else {
								status=STATUS.DB_NOT_FOUND;
							}
						}
					}
				} catch (SQLException e) {
					ANSI.printErr("Connection to database failed!",e);
					status=STATUS.DB_CONNECTION_FAILED;
				}
			} catch (ClassNotFoundException e) {
				ANSI.printErr(e.getMessage(),e);
				status=STATUS.SERVER_CONNECTION_FAILED;
			}
			return status;
		});
		
		try {
			status=future.get(Config.DATABASE_TIMEOUT_SECONDS,TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			status=STATUS.SERVER_NOT_FOUND;
			future.cancel(true);
		}
		
		String response="[RED]FAILED![RESET]";
		response=response.concat("[BR][YELLOW]MYSQL CONNECTION FAILED BECAUSE OF UNKNOWN REASON.[RESET]");
		
		switch(status) {
			case OK:
				response="[GREEN]OK![RESET]";
				break;
			case SERVER_NOT_FOUND:
				response="[RED]FAILED![RESET]";
				response=response.concat("[BR][YELLOW]MYSQL SERVER NOT FOUND![RESET]");
				break;
			case SERVER_CONNECTION_FAILED:
				response="[RED]FAILED![RESET]";
				response=response.concat("[BR][YELLOW]UNABLE TO LOG INTO MYSQL SERVER.[RESET]");
				break;
			case DB_NOT_FOUND:
				response="[RED]FAILED![RESET]";
				response=response.concat("[BR][YELLOW]MYSQL DATABASE NOT FOUND ON SERVER.[RESET]");
				break;
			case DB_CONNECTION_FAILED:
				response="[RED]FAILED![RESET]";
				response=response.concat("[BR][YELLOW]UNABLE TO IDENTIFY DATABASE.[RESET]");
				break;
			case UNKNOWN:
				response="[RED]FAILED![RESET]";
				response=response.concat("[BR][YELLOW]MYSQL CONNECTION FAILED BECAUSE OF UNKNOWN REASON.[RESET]");
				break;
		}
		ANSI.println(response);
		
	}
	
	public static Connection getNewConnection() throws SQLException {
		return DriverManager.getConnection(Config.connectionString(),Config.DATABASE_USER,Config.DATABASE_PWD);
	}
	
	public static boolean setup() throws BatchUpdateException {
		if(status==STATUS.SERVER_CONNECTION_FAILED) {
			ANSI.printWarn("Failed to connect to MYSQL Server. Not able to install.");
			return false;
		}
		
		try(Connection connection=getNewConnection()) {
			connection.setAutoCommit(false);
			try(Statement statement=connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY)) {
				statement.addBatch("START TRANSACTION;");
				statement.addBatch("CREATE TABLE IF NOT EXISTS `current` (`uuid` VARCHAR(36), `ffprobe` TEXT);");
				statement.addBatch("CREATE TABLE IF NOT EXISTS `info` (`infotext` VARCHAR(50));");
				statement.addBatch("CREATE TABLE IF NOT EXISTS `playlist` (`title` VARCHAR(128), `info` VARCHAR(128), `filepath` VARCHAR(256));");
				statement.addBatch("CREATE TABLE if NOT EXISTS `scheduled` (`id` INT(11) AUTO_INCREMENT, `title` VARCHAR(128), `filename` VARCHAR(256), PRIMARY KEY (`id`));");
				statement.addBatch("CREATE TABLE IF NOT EXISTS `downloadables` (`uuid` VARCHAR(36) NOT NULL, `path` VARCHAR(256) NOT NULL, `timestamp` BIGINT NOT NULL, `downloads` INT NOT NULL, `temp` TINYINT(1) NOT NULL, `ffprobe` TEXT NOT NULL);");
				statement.addBatch("CREATE TABLE IF NOT EXISTS `oauth2` (`state` VARCHAR(36) NOT NULL, `code` VARCHAR(256) NOT NULL);");				
				statement.addBatch("TRUNCATE `current`; TRUNCATE `info`; TRUNCATE `playlist`; TRUNCATE `scheduled`; TRUNCATE `downloadables`; TRUNCATE `oauth2`;");
				statement.addBatch("INSERT INTO `info` VALUES('YouTube Broadcast Bot Database');");
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
					throw e;
				}
				
			} catch(SQLException e) {
				connection.rollback();
				throw e;
			}
			
		} catch (SQLException e) {
			ANSI.printErr("Something went wrong while setting up mysql.",e);
			return false;
		}
		
		return true;
	}
	
}
