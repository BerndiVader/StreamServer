package com.gmail.berndivader.streamserver.mysql;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.term.ANSI;

public class DatabaseConnection {
	
	public final static String DB_ID="YAMPB_SQL_DB";
		
	public enum STATUS {
		OK(8000,"OK"),
		DB_CORRUPT_ERROR(8001,"DATABASE STRUCTURE CORRUPT"),
		DB_UNKNOWN_ERROR(8002,"UNKOWN DATABASE ERROR"),
		ACCESS_DENIED_ERROR(1045,"SERVER ACCESS DENIED"),
		DB_ACCESS_DENIED_ERROR(1044,"DATABASE ACCESS DENIED"),
		HOST_NOT_ALLOWED_ERROR(1130,"HOST ACCESS DENIED"),
		DB_TABLE_NOTFOUND_ERROR(1146,"DATABASE TABLE NOT FOUND"),
		CANT_CONNECT_SOCKET_ERROR(2002,"CANT CONNECT TO SOCKET"),
		CANT_CONNECT_SERVER_ERROR(2003,"CANT CONNECT TO SERVER"),
		UNKNOWN_HOST_ERROR(2005,"UNKNOWN HOST"),
		SERVER_GONE_ERROR(2006,"SERVER GONE"),
		LOST_CONNECTION_ERROR(2013,"CONNECTION LOST"),
		UNKNOWN(-1,"UNKNOWN");
		
		private final int code;
		private final String msg;
		
		STATUS(int code,String msg) {
			this.code=code;
			this.msg=msg;
		}
		
		public int code() {
			return code;
		}
		
		public String msg() {
			return msg;
		}
		
		public static STATUS fromCode(int value) {
			for (STATUS error:STATUS.values()) {
				if(error.code()==value) return error;
			}
			return STATUS.UNKNOWN;
		}
	}
	
	public static STATUS status=STATUS.UNKNOWN;
	public static DatabaseConnection instance;
	
	public DatabaseConnection() {
		ANSI.print("[CYAN]Test connection to mysql server...");
		test();
		ANSI.println(status==STATUS.OK?"[GREEN]OK![RESET]":String.format("[RED]FAILED![BR][YELLOW]%s[RESET]",status.msg()));
		
	}
	
	public static Connection getNewConnection() throws SQLException {
		if(status==STATUS.OK) return DriverManager.getConnection(Config.connectionString(),Config.MYSQL.USER,Config.MYSQL.PWD);
		return null;
	}
	
	public static STATUS testInstall() {
		Future<STATUS>future=Helper.EXECUTOR.submit(()->{
			
			STATUS test=STATUS.UNKNOWN;
			
			try {
				Class.forName("com.mysql.cj.jdbc.Driver");
				try(Connection connection=DriverManager.getConnection(Config.connectionString(),Config.MYSQL.USER,Config.MYSQL.PWD)) {
					Properties properties=connection.getClientInfo();
					connection.close();
					if(properties!=null) test=STATUS.OK;
				} catch (SQLException e) {
					test=STATUS.fromCode(e.getErrorCode());
					ANSI.error(status.msg(),e);
				}
			} catch (ClassNotFoundException e) {
				test=STATUS.CANT_CONNECT_SOCKET_ERROR;
				ANSI.error(status.msg(),e);
			}
			return test;
		});
		
		STATUS test=STATUS.UNKNOWN;
		
		try {
			test=future.get(Config.MYSQL.TIMEOUT_SECONDS,TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			test=STATUS.CANT_CONNECT_SERVER_ERROR;
			future.cancel(true);
		}
		
		return test;
	}
	
	public static boolean test() {
		Future<STATUS>future=Helper.EXECUTOR.submit(()->{
			
			STATUS test=STATUS.UNKNOWN;
			
			try {
				Class.forName("com.mysql.cj.jdbc.Driver");
				try(Connection connection=DriverManager.getConnection(Config.connectionString(),Config.MYSQL.USER,Config.MYSQL.PWD)) {
					try(PreparedStatement statement=connection.prepareStatement(String.format("SELECT infotext FROM %s.info LIMIT 1",Config.MYSQL.NAME),ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY)) {
						try(ResultSet result=statement.executeQuery()) {
							if(result.first()) {
								if(result.getString("infotext").equals(DB_ID)) {
									test=STATUS.OK;
								} else {
									test=STATUS.DB_CORRUPT_ERROR;
								}
							} else {
								test=STATUS.DB_CORRUPT_ERROR;
							}
						}
					} catch (SQLException e) {
						test=STATUS.fromCode(e.getErrorCode());
						ANSI.error(test.msg(),e);
					}
				} catch (SQLException e) {
					test=STATUS.fromCode(e.getErrorCode());
					ANSI.error(test.msg(),e);
				}
			} catch (ClassNotFoundException e) {
				test=STATUS.CANT_CONNECT_SOCKET_ERROR;
				ANSI.error(test.msg(),e);
			}
			return test;
		});
		
		try {
			status=future.get(Config.MYSQL.TIMEOUT_SECONDS,TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			status=STATUS.CANT_CONNECT_SERVER_ERROR;
			future.cancel(true);
		}
		
		return status==STATUS.OK;
	}
	
	public static boolean setup() {
		if(status==STATUS.CANT_CONNECT_SERVER_ERROR) {
			ANSI.warn("Failed to connect to MYSQL Server. Not able to install.");
			return false;
		}
		
		try(Connection connection=DriverManager.getConnection(Config.connectionString(),Config.MYSQL.USER,Config.MYSQL.PWD)) {
			connection.setAutoCommit(false);
			try(Statement statement=connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY)) {
				
				statement.addBatch("DROP TABLE IF EXISTS `current`;");
				statement.addBatch("DROP TABLE IF EXISTS `info`;");
				statement.addBatch("DROP TABLE IF EXISTS `playlist`;");
				statement.addBatch("DROP TABLE IF EXISTS `scheduled`;");
				statement.addBatch("DROP TABLE IF EXISTS `downloadables`;");
				statement.addBatch("DROP TABLE IF EXISTS `oauth2`;");				
				statement.addBatch("CREATE TABLE `current` (`title` VARCHAR(255), `ffprobe` TEXT);");
				statement.addBatch("CREATE TABLE `info` (`infotext` VARCHAR(50));");
				statement.addBatch("CREATE TABLE `playlist` (`title` VARCHAR(255), `ffprobe` TEXT, `filepath` VARCHAR(255));");
				statement.addBatch("CREATE TABLE `scheduled` (`id` INT(11) AUTO_INCREMENT, `title` VARCHAR(255), `filename` VARCHAR(255), PRIMARY KEY (`id`));");
				statement.addBatch("CREATE TABLE `downloadables` (`uuid` VARCHAR(36) NOT NULL, `path` VARCHAR(255) NOT NULL, `timestamp` BIGINT NOT NULL, `downloads` INT NOT NULL, `temp` TINYINT(1) NOT NULL, `ffprobe` TEXT NOT NULL);");
				statement.addBatch("CREATE TABLE `oauth2` (`state` VARCHAR(36) NOT NULL, `code` VARCHAR(255) NOT NULL);");				
				statement.addBatch(String.format("INSERT INTO `info` VALUES('%s');",DB_ID));

				try {
					int[]results=statement.executeBatch();
					connection.commit();
					String out;
					for(int i=0;i<results.length;i++) {
						out="[YELLOW]Batchline "+i+" execute ";
						switch (results[i]) {
						case Statement.SUCCESS_NO_INFO:
							out+="succeeded.";
							break;
						case Statement.EXECUTE_FAILED:
							out+="failed!";
							break;
						default:
							out+="suceeded with "+results[i]+" rows affected.";
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
			ANSI.error(STATUS.fromCode(e.getErrorCode()).msg(),e);
			return false;
		}
		
		return true;
	}
	
}
