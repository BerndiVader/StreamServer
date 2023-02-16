package com.gmail.berndivader.streamserver.discord;

import java.time.Duration;
import java.util.function.Consumer;

import com.gmail.berndivader.streamserver.StreamServer;
import com.gmail.berndivader.streamserver.console.ConsoleRunner;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import reactor.core.publisher.Mono;

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
		ConsoleRunner.println("noMatches");
		
	}

	@Override
	public void loadFailed(FriendlyException exception) {
		ConsoleRunner.println("ERROR: "+exception.getMessage());
		exception.printStackTrace();
		Mono.delay(Duration.ofSeconds(5)).doOnNext(new Consumer<Long>() {

			@Override
			public void accept(Long l) {
				StreamServer.DISCORDBOT.connectStream();
			}
			
		}).subscribe();
	}

}
