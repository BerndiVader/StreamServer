package com.gmail.berndivader.streamserver.mysql;

import java.io.File;
import java.io.IOException;
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
	
	private static final String INSERT="INSERT INTO `downloadables` (`uuid`, `path`, timestamp, downloads, temp, `ffprobe`) VALUES(?, ?, ?, ?, ?, ?);";
	private static final String TEST_FOR="SELECT `uuid` FROM `downloadables` WHERE `path`=?;";
	private static final String UPDATE="UPDATE `downloadables` SET timestamp=?, `ffprobe`=? WHERE `path`=?;";
	
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
		FFProbePacket ffprobe=FFProbePacket.build(file);
		boolean exists=false;
		
		String path=file.getAbsolutePath();
		try {
			path=file.getCanonicalPath();
		} catch (IOException e) {
			ANSI.error(e.getMessage(),e);
		}
		
		try(Connection connection=DatabaseConnection.getNewConnection()) {
			try(PreparedStatement test_for=connection.prepareStatement(TEST_FOR,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY)) {
				test_for.setString(1,path);
				ResultSet result=test_for.executeQuery();
				if(exists=result.next()) uuid=UUID.fromString(result.getString("uuid"));
			}
			if(exists) {
				try(PreparedStatement update=connection.prepareStatement(UPDATE,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY)) {
					update.setLong(1,System.currentTimeMillis()/1000l);
					update.setString(2,ffprobe.toString());
					update.setString(3,file.getAbsolutePath());
					update.executeUpdate();
				}
			} else {
				try(PreparedStatement insert=connection.prepareStatement(INSERT,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY)) {
					insert.setString(1,uuid.toString());
					insert.setString(2,path);
					insert.setLong(3,System.currentTimeMillis()/1000l);
					insert.setInt(4,0);
					insert.setBoolean(5,temp);
					insert.setString(6,ffprobe.toString());
					insert.executeUpdate();
				}
			}
		} catch (SQLException e) {
			ANSI.error("Failed to create or update downloadable media file.",e);
			return Optional.ofNullable(null);
		}
		File thumbnail=new File(Config.DL_WWW_THUMBNAIL_PATH);
		if(!thumbnail.exists()) thumbnail.mkdirs();
		thumbnail=new File(Config.DL_WWW_THUMBNAIL_PATH,uuid.toString()+".jpg");
		Helper.extractImageFromMedia(file,thumbnail);
		return Optional.of(Config.DL_URL+"/download.php?uuid="+uuid.toString());
	}

}
