package com.gmail.berndivader.streamserver.mysql;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.gmail.berndivader.streamserver.term.ANSI;
import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.config.Config;

public class MakeDownloadable implements Callable<Optional<String>>{
	
	private static final String sql="INSERT INTO `downloadables` (`uuid`, `path`, timestamp, downloads) VALUES(?, ?, ?, ?);";
	private File file;
	public Future<Optional<String>>future;
	
	public MakeDownloadable(File file) {
		
		this.file=file;
		this.future=Helper.EXECUTOR.submit(this);
	}
	
	@Override
	public Optional<String> call() {
		UUID uuid=UUID.randomUUID();
		try(Connection connection=DatabaseConnection.getNewConnection()) {
			try(PreparedStatement statement=connection.prepareStatement(sql,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY)) {
				statement.addBatch("START TRANSACTION;");
				statement.setString(1,uuid.toString());
				statement.setString(2,file.getAbsolutePath());
				statement.setLong(3,System.currentTimeMillis()/1000l);
				statement.setInt(4,0);
				statement.addBatch();
				statement.addBatch("COMMIT;");
				statement.executeBatch();
			} catch(SQLException e) {
				connection.rollback();
				throw e;
			}
		} catch (SQLException e) {
			ANSI.printErr("Failed to create downloadable media file.",e);
			return Optional.ofNullable(null);
		}
		return Optional.of(Config.DL_URL+"/download.php?uuid="+uuid.toString());
	}

}
