package com.gmail.berndivader.streamserver.discord.musicplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;

public final class MusicPlayer {
	private static AudioPlayerManager manager;
	
	static {
		manager=new DefaultAudioPlayerManager();
		AudioSourceManagers.registerLocalSource(manager);
	}
	
	private MusicPlayer() {}
	
	public static DiscordAudioProvider create() {
		return new DiscordAudioProvider(manager.createPlayer());
	}
	
	public static AudioPlayerManager manager() {
		return manager;
	}
	
	

}
