package com.gmail.berndivader.streamserver.discord.musicplayer;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.discord.DiscordBot;
import com.gmail.berndivader.streamserver.term.ANSI;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.player.event.PlayerPauseEvent;
import com.sedmelluq.discord.lavaplayer.player.event.PlayerResumeEvent;
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent;
import com.sedmelluq.discord.lavaplayer.player.event.TrackExceptionEvent;
import com.sedmelluq.discord.lavaplayer.player.event.TrackStartEvent;
import com.sedmelluq.discord.lavaplayer.player.event.TrackStuckEvent;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

public class TrackScheduler extends AudioEventAdapter {
	
	private final Map<Class<? extends AudioEvent>,Consumer<AudioEvent>>handlers=new HashMap<Class<? extends AudioEvent>,Consumer<AudioEvent>>();
	
	public TrackScheduler() {
		handlers.put(PlayerPauseEvent.class,event->onPlayerPause(event.player));
		handlers.put(PlayerResumeEvent.class,event->onPlayerResume(event.player));
		handlers.put(TrackStartEvent.class,event->{
			TrackStartEvent start=(TrackStartEvent)event;
			onTrackStart(start.player,start.track);
		});
		handlers.put(TrackEndEvent.class,event->{
			TrackEndEvent end=(TrackEndEvent)event;
			onTrackEnd(end.player,end.track,end.endReason);
		});
		handlers.put(TrackExceptionEvent.class,event->{
			TrackExceptionEvent exception=(TrackExceptionEvent)event;
			onTrackException(exception.player,exception.track,exception.exception);
		});
		handlers.put(TrackStuckEvent.class,event->{
			TrackStuckEvent stuck=(TrackStuckEvent)event;
	        onTrackStuck(stuck.player,stuck.track,stuck.thresholdMs,stuck.stackTrace);
		});
	}
		
	@Override
	public void onPlayerPause(AudioPlayer player) {
	}
	
	@Override
	public void onPlayerResume(AudioPlayer player) {
	}

	@Override
	public void onTrackStart(AudioPlayer player,AudioTrack track) {
		DiscordBot.instance.voiceChannel.createMessage(Paths.get(track.getIdentifier()).getFileName().toString()).subscribe();
	}

	@Override
	public void onTrackEnd(AudioPlayer player,AudioTrack track,AudioTrackEndReason endReason) {
		
		AudioTrack next=player.getScheduledTrack();
		if(next==null) next=DiscordBot.instance.provider.scheduledTracks.poll();
		if(next!=null) {
			player.playTrack(next);
		} else if(Config.DISCORD_MUSIC_AUTOPLAY) {
			MusicPlayer.playRandomMusic();
		}
		
	}

	@Override
	public void onTrackException(AudioPlayer player,AudioTrack track,FriendlyException exception) {
	}

	@Override
	public void onTrackStuck(AudioPlayer player,AudioTrack track,long thresholdMs,StackTraceElement[]stackTrace) {
		onTrackStuck(player,track,thresholdMs);
	}

	@Override
	public void onTrackStuck(AudioPlayer player,AudioTrack track,long thresholdMs) {
	}

	@Override
	public void onEvent(AudioEvent event) {
		
		Optional.ofNullable(handlers.get(event.getClass())).ifPresentOrElse(handler->handler.accept(event),()->{
			if(Config.DEBUG) ANSI.printWarn(String.format("Unhandled AudioEvent occured: [GREEN]%s[RESET]",event.getClass().getSimpleName()));
		});

	}

}
