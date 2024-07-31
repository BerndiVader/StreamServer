package com.gmail.berndivader.streamserver.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.gmail.berndivader.streamserver.term.ANSI;
import com.gmail.berndivader.streamserver.Helper;

public class VerifyOAuth2 implements Callable<Boolean>{
	
	private final static String SQL="SELECT `state` FROM `oauth2` WHERE `code` LIKE ?;";
	private final static String DELETE="DELETE FROM `oauth2` WHERE code LIKE ?;";
	private final String code;
	private final String uuid;
	
	public Future<Boolean>future;
	
	public VerifyOAuth2(String code, String uuid) {
		this.code=code;
		this.uuid=uuid;
		future=Helper.EXECUTOR.submit(this);
	}

	@Override
	public Boolean call() {
		
		String state="";
		try(Connection connection=DatabaseConnection.getNewConnection()) {
			try(PreparedStatement statement=connection.prepareStatement(SQL,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY)) {
				statement.setString(1,code);
				ResultSet result=statement.executeQuery();
				while(result.next()) {
					state=result.getString("state");
					try(PreparedStatement delete=connection.prepareStatement(DELETE,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY)) {
						delete.setString(1,code);
						delete.executeUpdate();
					}
				}
			} catch(SQLException e) {
				throw e;
			}
		} catch (SQLException e) {
			ANSI.printErr("OAuth2 verification failed.",e);
			return false;
		}
		return state.equals(uuid);
		
	}

}
