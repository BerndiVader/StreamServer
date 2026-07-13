package com.gmail.berndivader.streamserver.console.command.commands;

import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.annotation.Requireds;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.stream.Live;
import com.gmail.berndivader.streamserver.stream.packet.api.GeneralPacket;
import com.gmail.berndivader.streamserver.term.ANSI;

@ConsoleCommand(name="mtxinfo",usage="Show info from MediaMTX server.",requireds={Requireds.LIVESTREAM})
public class MediaMtxInfo extends Command {

	@Override
	public boolean execute(String[] args) {
		
		GeneralPacket general=Live.api.getGeneral();
		if(general!=null) {
			
			String.format("%s",general.version);
			
			String info=String.format("[BLUE]MediaMTX Version: [YELLOW]%s[BR][BLUE]Started at: [YELLOW]%s[RESET]",general.version,general.started);
			
			ANSI.println("[GREEN]MediaMTX Info:");
			ANSI.println("[WHITE]=====================");
			ANSI.println(info);
		}
		
		return true;
		
	}

}
