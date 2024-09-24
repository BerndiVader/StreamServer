package com.gmail.berndivader.streamserver.discord.musicplayer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.discord.DiscordBot;
import com.gmail.berndivader.streamserver.term.ANSI;
import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public final class MusicPlayer {
	public static AudioPlayerManager manager;
	
	static {
		manager=new DefaultAudioPlayerManager();
		manager.getConfiguration().setOutputFormat(StandardAudioDataFormats.DISCORD_OPUS);
		manager.getConfiguration().setOpusEncodingQuality(5);
		AudioSourceManagers.registerLocalSource(manager);
	}
	
	private MusicPlayer() {}
	
	public static DiscordAudioProvider create() {
		return new DiscordAudioProvider(manager.createPlayer());
	}
	
	public static void playRandomMusic() {
		
		Helper.EXECUTOR.submit(()->{
			
			Path path=Paths.get(Config.musicPath()).normalize();
			try {
				Path[]files=Files.list(path.toAbsolutePath())
						.filter(p->p.getFileName().toString().toLowerCase().endsWith(".mp3"))
						.toArray(Path[]::new);
				
				if(files.length>0) {
					Random rand=new Random();
			        Path file=files[rand.nextInt(files.length)];
			        manager.loadItem(file.toAbsolutePath().toString(),new AudioLoadResultHandler() {
						
						@Override
						public void trackLoaded(AudioTrack track) {
							AudioPlayer player=DiscordBot.instance.provider.player();
							if(player.getPlayingTrack()==null) {
								player.playTrack(track);
							} else {
								player.scheduleTrack(track);
							}
						}
						
						@Override
						public void playlistLoaded(AudioPlaylist playlist) {
						}
						
						@Override
						public void noMatches() {
						}
						
						@Override
						public void loadFailed(FriendlyException exception) {
						}
						
					});
			        
				}
			} catch (IOException e) {
				ANSI.error(e.getMessage(),e);
			}
			
		});
		
		
		
	}
	
}
