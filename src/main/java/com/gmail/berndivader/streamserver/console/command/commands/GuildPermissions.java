package com.gmail.berndivader.streamserver.console.command.commands;

import java.util.ArrayList;
import java.util.List;

import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.discord.permission.Guild;
import com.gmail.berndivader.streamserver.term.ANSI;

@ConsoleCommand(name="guildperm",usage="[add GUILDID NAME CHANNELID(S).,.,.][del GUILDID]")
public class GuildPermissions extends Command {

	@Override
	public boolean execute(String[] args) {
		
		String line=args[0];
		if(line.startsWith("add")) {
			String add=line.replaceFirst("add","").trim();
			String[]parameters=add.split(" ");
			if(parameters.length==3) {
				String[]channelIdStrings=parameters[2].split(",");
				List<Long>channelIds=new ArrayList<Long>();
				try {
					for(String id:channelIdStrings) channelIds.add(Long.valueOf(id));
					Long guildId=Long.valueOf(parameters[0]);
					String name=parameters[1];
					Guild guild=new Guild(name,channelIds.toArray(Long[]::new));
					Config.DISCORD_PERMITTED_GUILDS.merge(guildId,guild,(oldGuild,newGuild)->newGuild);
					ANSI.println("[GREEN]Guild permissions added or updated.[PROMPT]");
				} catch (NumberFormatException e) {
					ANSI.printWarn("Failed to parse guild or channel id. Please ensure they are valid numbers.");
				}
			} else {
				ANSI.printWarn("Not enough parameters present.");
			}
		} else if(line.startsWith("del")) {
			String del=line.replaceFirst("del","").trim();
			if(!del.isEmpty()) {
				try {
					Long id=Long.valueOf(del);
					if(Config.DISCORD_PERMITTED_GUILDS.containsKey(id)) {
						Config.DISCORD_PERMITTED_GUILDS.remove(id);
						ANSI.println("[GREEN]Removed guild from permission list.");
					} else {
						ANSI.printWarn("No guild with given id found.");
					}
				} catch(NumberFormatException e) {
					ANSI.printWarn("Failed to parse guild id. Please ensure its a valid number.");
				}
			} else {
				ANSI.printWarn("Missing guild id.");
			}
		} else {
			ANSI.println("[MAGENTA]Permitted guilds:");
			Config.DISCORD_PERMITTED_GUILDS.forEach((id,guild)->{
				ANSI.println("[YELLOW]Name:[BLUE]"+guild.name+" [YELLOW]Id:[BLUE]"+id);
			});
		}
		Config.saveConfig();
		ANSI.prompt();
		return true;
	}

}
