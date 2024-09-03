package com.gmail.berndivader.streamserver.console.command.commands;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.annotation.Requireds;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.discord.DiscordBot;
import com.gmail.berndivader.streamserver.discord.musicplayer.MusicPlayer;
import com.gmail.berndivader.streamserver.term.ANSI;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

@ConsoleCommand(name="playmp3",usage="playmp3 [--list]|[filename] -> Play or schedule discord voice music",requireds={Requireds.DISCORDBOT,Requireds.DISCORDMUSIC})
public class PlayMusic extends Command {

	@Override
	public boolean execute(String[] args) {
		String name=args[0];
		
		if(name.startsWith("--list")) return listScheduled();
		
		Path musicPath=Paths.get(Config.musicPath()).normalize();
		Path filePath=musicPath.resolve(name).normalize();
		
		if(!filePath.startsWith(musicPath)) {
			ANSI.printWarn("Invalid file path! The file must be inside the music library directory.");
			return false;
		}
		
		File file=filePath.toFile();
		if(file.exists()) {
			Optional<String>optFile=Optional.empty();
			try {
				optFile=Optional.of(file.getCanonicalPath());
			} catch (IOException e) {
				ANSI.printErr(e.getMessage(),e);
			}
			
			optFile.ifPresent(path->{
				if(path.toLowerCase().endsWith(".mp3")) {
					MusicPlayer.manager().loadItem(path,new AudioLoadResultHandler() {
						
						@Override
						public void trackLoaded(AudioTrack track) {
							if(DiscordBot.instance.provider.player().getPlayingTrack()==null) {
								DiscordBot.instance.provider.player().playTrack(track);
							} else {
								DiscordBot.instance.provider.scheduledTracks.add(track);
							}
						}
						
						@Override
						public void playlistLoaded(AudioPlaylist playlist) {}
						
						@Override
						public void noMatches() {
							ANSI.printWarn("No matches!");
						}
						
						@Override
						public void loadFailed(FriendlyException exception) {
							ANSI.printErr(exception.getMessage(),exception);
						}
						
					});
				} else {
					ANSI.printWarn("Wrong file format. Only mp3 format allowed.");
				}
			});
			
		} else {
			ANSI.printWarn("File not found!");
		}

		return true;
	}
	
	private boolean listScheduled() {
		ANSI.println("[MAGENTA]Scheduled mp3 music files vor voice channel:");
		DiscordBot.instance.provider.scheduledTracks.forEach(track->{
			ANSI.println("[YELLOW]"+Paths.get(track.getIdentifier()).getFileName());
		});
		return true;
	}

}
