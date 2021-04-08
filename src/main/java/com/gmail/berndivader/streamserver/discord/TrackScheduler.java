package com.gmail.berndivader.streamserver.discord;

import com.gmail.berndivader.streamserver.ConsoleRunner;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class TrackScheduler implements AudioLoadResultHandler {
	
	final AudioPlayer player;
	
	public TrackScheduler(final AudioPlayer audioPlayer) {
		player=audioPlayer;
	}

	@Override
	public void trackLoaded(AudioTrack track) {
		player.playTrack(track);
	}

	@Override
	public void playlistLoaded(AudioPlaylist playlist) {
		ConsoleRunner.println("playlist");
		
	}

	@Override
	public void noMatches() {
		ConsoleRunner.println("noatches");
		
	}

	@Override
	public void loadFailed(FriendlyException exception) {
		ConsoleRunner.println("error: "+exception.getMessage());
		// TODO Auto-generated method stub
		
	}

}
