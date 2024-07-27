package com.gmail.berndivader.streamserver.console.command.commands;

import java.util.List;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.term.ANSI;

@ConsoleCommand(name="list",usage="list [--media|--music|--temp|--playlist|--custom] regex")
public class ListFiles extends Command {
		
	@Override
	public boolean execute(String[] arguments) {

		String arg=arguments[0];
		String path=Config.DL_ROOT_PATH;
		boolean sub=false;
		
		if(arg.toUpperCase().startsWith("--MEDIA")) {
			path=Config.mediaPath();
			arg=arg.toUpperCase().replaceFirst("--MEDIA","").trim();
		} else if(arg.toUpperCase().startsWith("--MUSIC")) {
			path=Config.musicPath();
			arg=arg.toUpperCase().replaceFirst("--MUSIC","").trim();
		} else if(arg.toUpperCase().startsWith("--TEMP")) {
			path=Config.tempPath();
			arg=arg.toUpperCase().replaceFirst("--TEMP","").trim();
		} else if(arg.toUpperCase().startsWith("--PLAYLIST")) {
			path=Config.PLAYLIST_PATH;
			arg=arg.toUpperCase().replaceFirst("--PLAYLIST","").trim();
		} else if(arg.toUpperCase().startsWith("--CUSTOM")) {
			path=Config.PLAYLIST_PATH_CUSTOM;
			arg=arg.toUpperCase().replaceFirst("--CUSTOM","").trim();
		} else {
			sub=true;
		}

		List<String>files=Helper.getFilesByPath(path,sub,arg.isEmpty()?"*.*":arg);
		files.stream().forEach(ANSI::println);
		return true;
	}

}
