package com.gmail.berndivader.streamserver.discord;

import java.nio.ByteBuffer;

import com.gmail.berndivader.streamserver.ConsoleRunner;
import com.gmail.berndivader.streamserver.StreamServer;
import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent;
import com.sedmelluq.discord.lavaplayer.player.event.TrackExceptionEvent;
import com.sedmelluq.discord.lavaplayer.player.event.TrackStuckEvent;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;

import discord4j.voice.AudioProvider;

public class LavaPlayerAudioProvider extends AudioProvider {
	
	final AudioPlayer player;
	final MutableAudioFrame frame=new MutableAudioFrame();
	
	public LavaPlayerAudioProvider(final AudioPlayer audioPlayer) {
		super(ByteBuffer.allocate(StandardAudioDataFormats.DISCORD_OPUS.maximumChunkSize()));
		
		frame.setBuffer(getBuffer());
		player=audioPlayer;
		
		player.addListener(new AudioEventListener() {
			
			@Override
			public void onEvent(AudioEvent event) {
				
				if(event instanceof TrackEndEvent) {
					ConsoleRunner.println("Track ended, try to reconnect to stream.");
					StreamServer.DISCORDBOT.connectStream();
				} else if(event instanceof TrackStuckEvent) {
					ConsoleRunner.println("Track stucked, try to reconnect to stream.");
					StreamServer.DISCORDBOT.audioPlayer.stopTrack();
				} else if(event instanceof TrackExceptionEvent) {
					ConsoleRunner.println(((TrackExceptionEvent) event).exception.getMessage());
					ConsoleRunner.println("Track exception occured, try to reconnect to stream.");
					StreamServer.DISCORDBOT.audioPlayer.stopTrack();
					StreamServer.DISCORDBOT.connectStream();
				}
			}
		});
		
	}

	@Override
	public boolean provide() {
		final boolean didProvide=player.provide(frame);
		if(didProvide) {
			getBuffer().flip();
		}
		return didProvide;
	}

}
