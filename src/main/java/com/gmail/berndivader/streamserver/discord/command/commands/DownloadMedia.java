package com.gmail.berndivader.streamserver.discord.command.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.Optional;
import java.util.UUID;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.annotation.DiscordCommand;
import com.gmail.berndivader.streamserver.annotation.Requireds;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.discord.ButtonAction;
import com.gmail.berndivader.streamserver.discord.ButtonAction.ID;
import com.gmail.berndivader.streamserver.discord.command.Command;
import com.gmail.berndivader.streamserver.discord.permission.Permission;
import com.gmail.berndivader.streamserver.discord.permission.Permissions;
import com.gmail.berndivader.streamserver.discord.permission.User.Rank;
import com.gmail.berndivader.streamserver.ffmpeg.InfoPacket;
import com.gmail.berndivader.streamserver.mysql.MakeDownloadable;
import com.gmail.berndivader.streamserver.term.ANSI;

import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateFields.Field;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.core.spec.MessageEditSpec;
import discord4j.rest.util.Color;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

@Permission(required=Rank.MEMBER)
@DiscordCommand(name="dl",usage="Download media: .dl [--music][--temp][--link][--click] <valid_url>",requireds={Requireds.DATABASE})
public class DownloadMedia extends Command<Void> {

	private class ProcessRunnable implements Runnable {
		
		private enum Status {
			NONE,
			RUNNING,
			ABORTED,
			TIMEOUT,
			EXISTS,
			ERROR,
			WARNING,
			FINISHED
		}
		
		private String line;
		private final MessageChannel channel;
		private final String uuid;
		
		private Status status=Status.NONE;
		
		public ProcessRunnable(String line,MessageChannel channel) {
			this.line=line;
			this.channel=channel;
			uuid=UUID.randomUUID().toString();
		}

		@Override
		public void run() {
			Optional<File>opt=Helper.getOrCreateMediaDir(Config.DL_ROOT_PATH);
			if(opt.isEmpty()) {
				
				channel.createMessage(EmbedCreateSpec.builder()
						.title("ERROR")
						.color(Color.RED)
						.description("There was an issue with the download directory configured in Config.DL_ROOT_PATH. It is either a file or the directory couldnt be created by the bot.")
						.build()
				).subscribe();
				return;
				
			}
			
			File directory=opt.get();

			channel.createMessage(EmbedCreateSpec.builder()
					.title("Prepare download media file.")
					.color(Color.GREEN)
					.build()
			).doOnSuccess(message->{
				
				Entry<ProcessBuilder,InfoPacket>entry=Helper.createDownloadBuilder(directory,line);
				ProcessBuilder builder=entry.getKey();
				InfoPacket infoPacket=entry.getValue();
				status=Status.RUNNING;
				
				
				try {
					MessageEditSpec.Builder msgBuilder=MessageEditSpec.builder();
					msgBuilder.addComponent(ActionRow.of(ButtonAction.Builder.cancel(uuid),ButtonAction.Builder.schedule(ID.SCHEDULE,member.getId().asLong())))
					.contentOrNull("Starting download...")
					.addEmbed(EmbedCreateSpec.builder()
									.title(infoPacket.title)
									.url(infoPacket.webpage_url)
									.description(infoPacket.description)
									.image(infoPacket.thumbnail)
									.color(Color.BLUE)
									.footer(infoPacket.format,null)
								.build());
					
					message.edit(msgBuilder.build()).doOnCancel(()->ANSI.printRaw("[BR]CANCELLED[BR]"))
					.doOnError(e->ANSI.printErr(e.getMessage(),e.getCause())).subscribe();
					
					Disposable listener=message.getClient().on(ButtonInteractionEvent.class,event->{
						
						if(event.getCustomId().equals(uuid)) {
							long creatorId=member.getId().asLong();
							long clickerId=event.getInteraction().getId().asLong();
							if(!Permissions.Users.permitted(clickerId,Rank.ADMIN)||creatorId!=clickerId) return Mono.empty();
							status=Status.ABORTED;
							return event.edit(InteractionApplicationCommandCallbackSpec.create()
									.withContent("")
									.withEmbeds(EmbedCreateSpec.create()
											.withTitle("Aborted")
											.withDescription("Media download aborted by user.")
											.withColor(Color.RED)
											)
									.withComponents(new ArrayList<LayoutComponent>())
							);
						}
						return Mono.empty();
					}).subscribe();
					
					Process process=builder.start();
					long time=System.currentTimeMillis();
					StringBuilder errorBuilder=new StringBuilder();
					
					try(InputStream input=process.getInputStream();
						BufferedReader error=process.errorReader()) {
						
						while(status==Status.RUNNING) {
							if(!process.isAlive()) {
								status=Status.FINISHED;
								break;
							}
							int avail=input.available();
							if(avail>0) {
								time=System.currentTimeMillis();
								String line=new String(input.readNBytes(avail));
								if(line.contains("[Metadata]")) {
									String[]temp=line.split("\"");
									if(temp.length>0) infoPacket.local_filename=temp[1];
								}
								message.edit(MessageEditSpec.create().withContentOrNull(line)).subscribe();
							} else if(System.currentTimeMillis()-time>Config.DL_TIMEOUT_SECONDS*1000l) {
								status=Status.TIMEOUT;
								break;
							}
						}
						
						error.lines().filter(line->!line.startsWith("WARNING")).forEach(line->errorBuilder.append(line));
						if(errorBuilder.length()>0) status=Status.ERROR;
						
					}
										
					MessageEditSpec.Builder edit=MessageEditSpec.builder();
					edit.componentsOrNull(new ArrayList<LayoutComponent>());
					
					switch(status) {
					case TIMEOUT:
						edit.contentOrNull("")
							.addEmbed(EmbedCreateSpec.builder()
								.title("TIMOUT")
								.description("Download will be terminated, because it appears, that the process is stalled since "+(long)(Config.DL_TIMEOUT_SECONDS/60)+" minutes.")
								.color(Color.RED)
								.build());
						break;
					case FINISHED:
						edit.contentOrNull("FINISHED");
						EmbedCreateSpec.Builder embed=EmbedCreateSpec.builder()
							.title("Download finished....")
							.url(infoPacket.webpage_url)
							.description(infoPacket.title)
							.image(infoPacket.thumbnail)
							.color(Color.GREEN)
							.footer(infoPacket.format,null);
						if(infoPacket.downloadable) {
							File file=new File(builder.directory().getAbsolutePath()+"/"+infoPacket.local_filename);
							if(file.exists()&&file.isFile()&&file.canRead()) {
								MakeDownloadable downloadable=new MakeDownloadable(file,infoPacket.temp);
								Optional<String>optLink=Optional.ofNullable(null);
								try {
									optLink=downloadable.future.get(2,TimeUnit.MINUTES);
								} catch (InterruptedException | ExecutionException | TimeoutException e) {
									ANSI.printErr(e.getMessage(),e);
								}
								optLink.ifPresentOrElse(
									link->embed.addField(Field.of("Downloadlink",link,false)),
									()->embed.addField(Field.of("Downloadlink","Failed to create download link.",false))
								);
							}
						}
						edit.addEmbed(embed.build());
						break;
					case ERROR:
						edit.contentOrNull("");
						edit.addEmbed(EmbedCreateSpec.builder()
								.title("ERROR")
								.color(Color.RED)
								.description("Something went wront.\n\n".concat(errorBuilder.toString()))
								.build());
						break;
					default:
						edit.contentOrNull("");
						edit.addEmbed(EmbedCreateSpec.builder()
								.title("WARNING")
								.description("Mediafile already downloaded and exists.")
								.color(Color.ORANGE)
								.build());
						break;
					}
					
					message.edit(edit.build()).subscribe();
					
					if(listener!=null&&!listener.isDisposed()) listener.dispose();
					if(process.isAlive()) process.destroyForcibly();
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}).doOnError(error->ANSI.printErr("Error while downloading media in discord command.",error))
			.subscribe();
		}
	}

	@Override
	public Mono<Void> exec() {
		
		return Mono.fromRunnable(new ProcessRunnable(string,channel));
		
	}
	
}
