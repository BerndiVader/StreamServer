package com.gmail.berndivader.streamserver.console.command.commands;

import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.discord.permission.User;
import com.gmail.berndivader.streamserver.discord.permission.User.Rank;
import com.gmail.berndivader.streamserver.term.ANSI;

@ConsoleCommand(name="userperm",usage="[add MEMBERID NAME GUEST|MEMBER|MOD|ADMIN] [del MEMBERID]")
public class UserPermissions extends Command {

	@Override
	public boolean execute(String[] args) {
		
		String line=args[0];
		if(line.startsWith("add")) {
			String add=line.replaceFirst("add","").trim();
			String[]parameters=add.split(" ");
			if(parameters.length==3) {
				
				try {
					Long memberId=Long.valueOf(parameters[0]);
					String name=parameters[1];
					Rank rank=Rank.valueOf(parameters[2].toUpperCase());
					User user=new User(name,rank);
					Config.DISCORD_PERMITTED_USERS.merge(memberId,user,(oldUser,newUser)->newUser);
					ANSI.println("[GREEN]User permissions successfully added/updated.[PROMPT]");
					return true;
				} catch (NumberFormatException e) {
					ANSI.warn("Illegal member id. Please enter a valid Long value.");
				} catch (IllegalArgumentException e) {
					ANSI.warn("Illegal Rank value found. Use GUEST,MEMBER,MOD or ADMIN");
				}
				
			} else {
				ANSI.warn("Not enough parameters present.");
			}
			ANSI.error("Failed to add user permissions.",new RuntimeException());
			
		} else if(line.startsWith("del")) {
			String del=line.replaceFirst("del","").trim();
			if(!del.isEmpty()) {
				try {
					Long id=Long.valueOf(del);
					if(Config.DISCORD_PERMITTED_USERS.containsKey(id)) {
						Config.DISCORD_PERMITTED_USERS.remove(id);
						ANSI.println("[GREEN]Removed user from permission list.");
					} else {
						ANSI.warn("No user with given id found.");
					}
				} catch(NumberFormatException e) {
					ANSI.warn("Failed to parse member id. Please ensure its a valid number.");
				}
			} else {
				ANSI.warn("Missing member id.");
			}
		} else {
			ANSI.println("[MAGENTA]Permitted members:");
			Config.DISCORD_PERMITTED_USERS.forEach((id,user)->{
				ANSI.println("[YELLOW]Name:[BLUE]"+user.name+" [YELLOW]Id:[BLUE]"+id+" [YELLOW]Rank:[BLUE]"+user.rank.name());
			});
		}
		Config.saveConfig();
		ANSI.prompt();
		return true;
	}

}
