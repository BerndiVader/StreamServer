package com.gmail.berndivader.streamserver.discord;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.function.Consumer;

import com.gmail.berndivader.streamserver.console.ConsoleRunner;
import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent;
import com.sedmelluq.discord.lavaplayer.player.event.TrackExceptionEvent;
import com.sedmelluq.discord.lavaplayer.player.event.TrackStuckEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;

import discord4j.voice.AudioProvider;
import reactor.core.publisher.Mono;

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
					TrackEndEvent e=(TrackEndEvent)event;
					if(!e.endReason.equals(AudioTrackEndReason.REPLACED)) {
						ConsoleRunner.println("Track ended, try to reconnect to stream.");
						delayedConnect();
					}
				} else if(event instanceof TrackStuckEvent) {
					ConsoleRunner.println("Track stucked, try to reconnect to stream.");
					delayedConnect();
				} else if(event instanceof TrackExceptionEvent) {
					ConsoleRunner.println(((TrackExceptionEvent) event).exception.getMessage());
					ConsoleRunner.println("Track exception occured, try to reconnect to stream.");
					delayedConnect();
				}
			}
		});
		
	}
	
	public void delayedConnect() {
		Mono.delay(Duration.ofSeconds(5)).doOnNext(new Consumer<Long>() {

			@Override
			public void accept(Long l) {
				DiscordBot.instance.connectStream();
				ConsoleRunner.println("Try to reconnect Audiostream");
			}
			
		}).subscribe();
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
