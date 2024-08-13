package com.gmail.berndivader.streamserver.discord.permission;


import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.discord.permission.User.Rank;

public final class Permissions {
	private Permissions() {}
	
	public static class Users {
		private Users() {}
		
		public static boolean permitted(Long id,Rank required) {
			if(Config.DISCORD_PERMITTED_USERS.containsKey(id)) {
				User user=Config.DISCORD_PERMITTED_USERS.get(id);
				return user.rank.ordinal()>=required.ordinal();
			} else if(required==Rank.GUEST) {
				return true;
			}
			return false;
		}
	}
	
	public static class Guilds {
		private Guilds() {}
		
		public static boolean permitted(Long guildId,Long channelId) {
			return Config.DISCORD_PERMITTED_GUILDS.containsKey(guildId)
					&&Config.DISCORD_PERMITTED_GUILDS.get(guildId).channelId.contains(channelId);
		}
			
	}
	
}
