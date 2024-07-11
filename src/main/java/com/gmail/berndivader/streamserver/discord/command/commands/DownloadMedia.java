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
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.discord.command.Command;
import com.gmail.berndivader.streamserver.ffmpeg.InfoPacket;
import com.gmail.berndivader.streamserver.mysql.MakeDownloadable;
import com.gmail.berndivader.streamserver.term.ANSI;

import discord4j.core.event.domain.interaction.ButtonInteractEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Color;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

@DiscordCommand(name="dl",usage="Download media. [--no-default] [--dir] [--yes-playlist] --url <valid_url>")
public class DownloadMedia extends Command<Void> {

	private class ProcessCallback implements Runnable {
		
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
		
		public ProcessCallback(String line,MessageChannel channel) {
			this.line=line;
			this.channel=channel;
			uuid=UUID.randomUUID().toString();
		}

		@Override
		public void run() {
			File directory=Helper.getOrCreateMediaDir(Config.DL_MUSIC_PATH);
			if(directory==null) {
								
				channel.createMessage(msg->{
					msg.addEmbed(embed->{
						embed.setTitle("ERROR DLP!")
						.setColor(Color.RED)
						.setDescription("There was an issue with the download directory configured in Config.DL_MUSIC_PATH. It is either a file or the directory couldnt be created by the bot.");
					});
				}).subscribe();
				
				return;
			}
			
			channel.createMessage(msg->{
				msg.addEmbed(embed->{
					embed.setTitle("Prepare download media file.");
					embed.setColor(Color.GREEN);
				});
			}).doOnSuccess(message->{
				
				Entry<ProcessBuilder, Optional<InfoPacket>>entry=Helper.prepareDownloadBuilder(directory,line);
				ProcessBuilder builder=entry.getKey();
				Optional<InfoPacket>infoPacket=entry.getValue();
				status=Status.RUNNING;
				
				try {
					message.edit(msg->{
						msg.setComponents(ActionRow.of(Button.danger(uuid,"Cancel")))
						.setContent("Starting download...").addEmbed(embed->{
							embed.setTitle("Downloading....");
							infoPacket.ifPresent(info->{
								embed.setUrl(info.webpage_url);
								embed.setDescription(info.title);
								embed.setImage(info.thumbnail);
								embed.setColor(Color.BLUE);
								embed.setFooter(info.format,null);
							});
						});
					}).doOnCancel(()->{
						ANSI.printRaw("[BR]CANCELLED[BR]");
					})
					.doOnError(e->{
						ANSI.printErr(e.getMessage(),e.getCause());
					}).subscribe();
					
					Disposable listener=message.getClient().on(ButtonInteractEvent.class,event->{
						if(event.getCustomId().equals(uuid)) {
							status=Status.ABORTED;
							return event.edit(msg->{
								msg.setContent("").addEmbed(embed->{
									embed.setTitle("Aborted")
									.setDescription("Media download aborted by user.")
									.setColor(Color.RED);
								}).setComponents(new ArrayList<LayoutComponent>());
							});
						}
						return Mono.empty();
					}).subscribe();
					
					Process process=builder.start();

					InputStream input=process.getInputStream();
					long time=System.currentTimeMillis();
					
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
								infoPacket.ifPresent(info->{
									String[]temp=line.split("\"");
									if(temp.length>0) info.local_filename=temp[1];
								});
							}
							message.edit(msg->msg.setContent(line)).subscribe();
						} else if(System.currentTimeMillis()-time>Config.DL_TIMEOUT_SECONDS*1000l) {
							status=Status.TIMEOUT;
						}
					}
					
					BufferedReader error=process.errorReader();
					StringBuilder errorBuilder=new StringBuilder();
					error.lines().filter(line->!line.startsWith("WARNING")).forEach(line->errorBuilder.append(line));
					
					if(errorBuilder.length()>0) status=Status.ERROR;
					
					message.edit(msg->{
						msg.setComponents(new ArrayList<LayoutComponent>());
						msg.addEmbed(embed->{
							switch(status) {
							case TIMEOUT:
								msg.setContent("");
								embed.setTitle("TIMEOUT")
								.setDescription("Download will be terminated, because it appears, that the process is stalled since "+(long)(Config.DL_TIMEOUT_SECONDS/60)+" minutes.")
								.setColor(Color.RED);
								break;
							case FINISHED:
								msg.setContent("FINISHED");
								embed.setTitle("Download finished....");
								infoPacket.ifPresent(info->{
									embed.setUrl(info.webpage_url);
									embed.setDescription(info.title);
									embed.setImage(info.thumbnail);
									embed.setColor(Color.GREEN);
									embed.setFooter(info.format,null);

									if(info.downloadable) {
										File file=new File(builder.directory().getAbsolutePath()+"/"+info.local_filename);
										if(file.exists()&&file.isFile()&&file.canRead()) {
											MakeDownloadable downloadable= new MakeDownloadable(file);
											Optional<String>optLink=Optional.ofNullable(null);
											try {
												optLink=downloadable.future.get(2,TimeUnit.MINUTES);
											} catch (InterruptedException | ExecutionException | TimeoutException e) {
												ANSI.printErr(e.getMessage(),e);
											}
											optLink.ifPresentOrElse(link->{
												embed.addField("Downloadlink",link,false);
											},()->{
												embed.addField("Downloadlink","Failed to create download link.",false);
											});
										}
									}
									
								});
								break;
							case ERROR:
								msg.setContent("");
								embed.setTitle("ERROR")
								.setColor(Color.ORANGE)
								.setDescription("Something went wront.\n\n".concat(errorBuilder.toString()));
								break;
							default:
								msg.setContent("");
								embed.setTitle("WARNING")
								.setDescription("Mediafile already downloaded and exists.")
								.setColor(Color.ORANGE);
								break;
							}
						});
					}).subscribe();
					
					if(listener!=null&&!listener.isDisposed()) listener.dispose();
					if(process.isAlive()) process.destroyForcibly();
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}).doOnError(error->{
				ANSI.printErr("Error while downloading media in discord command.",error);
			}).subscribe();
		}
	}

	@Override
	public Mono<Void> execute(String string, MessageChannel channel) {
		
		Helper.EXECUTOR.submit(new ProcessCallback(string,channel));
		return Mono.empty();
	}
	
}
