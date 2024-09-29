package com.gmail.berndivader.streamserver.console.command.commands;

import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.annotation.Requireds;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.youtube.OAuth2;

@ConsoleCommand(name="oauth",usage="Start OAuth2 flow to grant access to Youtube.",requireds={Requireds.DATABASE})
public class OAuth2Flow extends Command {

	@Override
	public boolean execute(String[] args) {
		return OAuth2.build();
	}

}
