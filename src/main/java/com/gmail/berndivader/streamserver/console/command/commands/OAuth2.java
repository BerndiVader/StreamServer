package com.gmail.berndivader.streamserver.console.command.commands;

import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.youtube.Youtube;

@ConsoleCommand(name="oauth",usage="Start OAuth2 flow to grant access to Youtube.")
public class OAuth2 extends Command {

	@Override
	public boolean execute(String[] args) {
		return Youtube.OAuth2Flow();
	}

}
