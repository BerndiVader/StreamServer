package com.gmail.berndivader.streamserver.discord.musicplayer;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;

import discord4j.voice.AudioProvider;

public class DiscordAudioProvider extends AudioProvider {
	private final MutableAudioFrame frame=new MutableAudioFrame();
	private final AudioPlayer player;
	public ConcurrentLinkedDeque<AudioTrack>scheduledTracks=new ConcurrentLinkedDeque<AudioTrack>();

	public DiscordAudioProvider(AudioPlayer player) {
		super(ByteBuffer.allocate(StandardAudioDataFormats.DISCORD_OPUS.maximumChunkSize()));
		this.player=player;
		frame.setBuffer(getBuffer());
	}
	
	@Override
	public boolean provide() {
		boolean provided=player.provide(frame);
		if (provided) getBuffer().flip();
		return provided;
	}
	
	public AudioPlayer player() {
		return player;
	}
	
}
