package com.gmail.berndivader.streamserver.console.command.commands;

import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.annotation.Requireds;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.ffmpeg.BroadcastRunner;
import com.gmail.berndivader.streamserver.youtube.PrivacyStatus;

@ConsoleCommand(
		name="createlive",
		usage="(title, description, privacy) - Try to reinitate and start livebroadcast on Youtube. No args will use default settings. "
				+"If string contains ',' use doublequotes.",
		requireds={Requireds.BROADCASTRUNNER,Requireds.DATABASE}
	)

public class StartLiveStream extends Command {

	@Override
	public boolean execute(String[] args) {
		
		String[]opts=new String[0];
		
		if(!args[0].isEmpty()) {
			opts=args[0].split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)",-1);
			
			for(int i=0;i<opts.length;i++) {
				String opt=opts[i];
				if(opt.startsWith("\"")) opt=opt.substring(1);
				if(opt.endsWith("\"")) opt=opt.substring(0,opt.length()-1);
				opts[i]=opt;
			}
			
		}
				
		String title=opts.length>0?opts[0]:Config.BROADCASTER.BROADCAST_DEFAULT_TITLE;
		String description=opts.length>1?opts[1]:Config.BROADCASTER.BROADCAST_DEFAULT_DESCRIPTION;
		String priv=opts.length>2?opts[2].toUpperCase():Config.BROADCASTER.BROADCAST_DEFAULT_PRIVACY.toUpperCase();
		
		PrivacyStatus privacy=PrivacyStatus.isEnum(priv)?PrivacyStatus.valueOf(priv):PrivacyStatus.UNLISTED;
		
		
		BroadcastRunner.checkOrReInitiateLiveBroadcast(title,description,privacy);
		
		return true;
	}

}
