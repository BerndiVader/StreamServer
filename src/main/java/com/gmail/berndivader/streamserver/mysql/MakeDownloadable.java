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
import com.gmail.berndivader.streamserver.ffmpeg.FFProbePacket;

public class MakeDownloadable implements Callable<Optional<String>>{
	
	private static final String sql="INSERT INTO `downloadables` (`uuid`, `path`, timestamp, downloads, temp, `ffprobe`) VALUES(?, ?, ?, ?, ?, ?);";
	private final File file;
	private final boolean temp;
	public Future<Optional<String>>future;
	
	public MakeDownloadable(File file,boolean temp) {
		this.temp=temp;
		this.file=file;
		this.future=Helper.EXECUTOR.submit(this);
	}
	
	@Override
	public Optional<String> call() {
		UUID uuid=UUID.randomUUID();
		FFProbePacket ffprobe=Helper.createProbePacket(file);
		
		try(Connection connection=DatabaseConnection.getNewConnection()) {
			connection.setAutoCommit(false);
			try(PreparedStatement statement=connection.prepareStatement(sql,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY)) {
				statement.addBatch("START TRANSACTION;");
				statement.setString(1,uuid.toString());
				statement.setString(2,file.getAbsolutePath());
				statement.setLong(3,System.currentTimeMillis()/1000l);
				statement.setInt(4,0);
				statement.setBoolean(5,temp);
				statement.setString(6,ffprobe.toString());
				statement.addBatch();
				statement.addBatch("COMMIT;");
				statement.executeBatch();
			} catch(SQLException e) {
				ANSI.printErr("Failed to execute batch.",e);
				connection.rollback();
				throw e;
			}
		} catch (SQLException e) {
			ANSI.printErr("Failed to create downloadable media file.",e);
			return Optional.ofNullable(null);
		}
		File thumbnail=new File(Config.DL_WWW_THUMBNAIL_PATH);
		if(!thumbnail.exists()) thumbnail.mkdirs();
		thumbnail=new File(Config.DL_WWW_THUMBNAIL_PATH,uuid.toString()+".jpg");
		Helper.extractImageFromMedia(file,thumbnail);
		return Optional.of(Config.DL_URL+"/download.php?uuid="+uuid.toString());
	}

}
