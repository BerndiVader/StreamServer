package com.gmail.berndivader.streamserver.mysql;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.gmail.berndivader.streamserver.term.ANSI;
import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.config.Config;

public class DeleteUnlinkedMediafiles implements Callable<Boolean>{
	
	private static final String DOWNLOADABLES="SELECT `uuid`,`path` FROM `downloadables`;";
	private static final String DELETE="DELETE FROM `downloadables` WHERE `uuid` IN (%s);";
		
	public Future<Boolean>future;
	
	public DeleteUnlinkedMediafiles() {
		this.future=Helper.EXECUTOR.submit(this);
	}
	
	@Override
	public Boolean call() {
		HashMap<String,String>media=new HashMap<String,String>();
		File[]files=null;
		File[]thumbnails=null;
		
		File tempDir=new File(Config.tempPath());
		File thumbDir=new File(Config.DL_WWW_THUMBNAIL_PATH);
		if(tempDir.exists()) files=tempDir.listFiles(file->file.isFile());
		if(thumbDir.exists()) thumbnails=thumbDir.listFiles(file->file.isFile());
		
		try(Connection connection=DatabaseConnection.getNewConnection()) {
			try(PreparedStatement downloadables=connection.prepareStatement(DOWNLOADABLES,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY)) {
				ResultSet result=downloadables.executeQuery();
				while(result.next()) media.put(result.getString(1),result.getString(2));
			}
		} catch (SQLException e) {
			ANSI.printErr("Failed to collect downloadables from database.",e);
			return false;
		}
		
		String values="";
		for(Entry<String,String>entry:media.entrySet()) {
			if(Files.notExists(Path.of(entry.getValue()),LinkOption.NOFOLLOW_LINKS)) values+=values.length()==0?"'"+entry.getKey()+"'":",'"+entry.getKey()+"'";
		}
		
		File[]unlinked=Arrays.stream(files).filter(file->!media.containsValue(file.getAbsolutePath())).toArray(File[]::new);
		File[]unlinkedThumbnails=Arrays.stream(thumbnails).filter(file->!media.containsKey(file.getName().replace(".jpg",""))).toArray(File[]::new);
		
		int result=0;
		if(!values.isEmpty()) {
			String delete=String.format(DELETE,values);
			try(Connection connection=DatabaseConnection.getNewConnection()) {
				try(PreparedStatement statement=connection.prepareStatement(delete,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY)) {
					result=statement.executeUpdate();
				}
			} catch (SQLException e) {
				ANSI.printErr("Failed to delete dead entries from downloadables table.",e);
			}
		}
		ANSI.println("[YELLOW]Removed "+result+" entries from downloadables table.");
		
		Arrays.stream(unlinked).forEach(file->file.delete());
		Arrays.stream(unlinkedThumbnails).forEach(file->file.delete());
		ANSI.println("[YELLOW]Removed "+unlinked.length+" unlinked files from temp path.");
		ANSI.println("[YELLOW]Removed "+unlinkedThumbnails.length+" unlinked thumbnails from www path.");
		return true;
	}

}
