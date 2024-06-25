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

public class MakeDownloadable implements Callable<Boolean>{
	
	private static final String sql="INSERT INTO `downloadables` (`uuid`, `path`, timestamp) VALUES(?, ?, ?);";
	private File file;
	private Optional<UUID>optUUID=Optional.ofNullable(null);
	public Future<Boolean>future;
	
	public MakeDownloadable(File file) {
		
		this.file=file;
		this.future=Helper.EXECUTOR.submit(this);
	}
	
	public String getDownloadLink() {
		String link="";
		if(optUUID.isPresent()) link=Config.DL_URL+"/download.php?uuid="+optUUID.get().toString();
		return link;
	}

	@Override
	public Boolean call() {
		UUID uuid=UUID.randomUUID();
		try(Connection connection=DatabaseConnection.getNewConnection()) {
			try(PreparedStatement statement=connection.prepareStatement(sql,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE)) {
				statement.addBatch("START TRANSACTION;");
				statement.setString(1,uuid.toString());
				statement.setString(2,file.getAbsolutePath());
				statement.setLong(3,System.currentTimeMillis());
				statement.addBatch();
				statement.addBatch("COMMIT;");
				statement.executeBatch();
			}
		} catch (SQLException e) {
			ANSI.printErr("Failed to create downloadable media file.",e);
			return false;
		}
		optUUID=Optional.ofNullable(uuid);
		return true;
	}

}
