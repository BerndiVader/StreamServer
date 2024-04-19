package com.gmail.berndivader.streamserver.discord.command.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.annotation.DiscordCommand;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.console.ConsoleRunner;
import com.gmail.berndivader.streamserver.discord.command.Command;

import discord4j.core.event.domain.interaction.ButtonInteractEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Color;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

@DiscordCommand(name="dlp",usage="Download media. [--yes-playlist] --url <valid_url>")
public class DownloadMedia extends Command<Void> {
	
	private class ProcessCallback implements Runnable {
		
		private final String line;
		private final MessageChannel channel;
		private final String uuid;
		private boolean cancel=false;
		
		public ProcessCallback(String line,MessageChannel channel) {
			this.line=line;
			this.channel=channel;
			uuid=UUID.randomUUID().toString();
		}

		@Override
		public void run() {
			File directory=new File(Config.DL_MUSIC_PATH);
			if(!directory.exists()) {
				directory.mkdir();
			}
			if(!directory.exists()||directory.isFile()) {
								
				channel.createMessage(msg->{
					msg.addEmbed(embed->{
						embed.setTitle("ERROR DLP!");
						embed.setColor(Color.RED);
						embed.setDescription("There was an issue with the download directory configured in Config.DL_MUSIC_PATH. It is either a file or the directory couldnt be created by the bot.");
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
				ProcessBuilder builder=new ProcessBuilder();
				builder.directory(directory);
				builder.command("yt-dlp"
						,"--ignore-errors"
						,"--extract-audio"
						,"--format","bestaudio"
						,"--audio-format","mp3"
						,"--audio-quality","160K"
						,"--output","%(title)s.%(ext)s"
						,"--restrict-filenames"
						,"--no-playlist"
				);
				
				String[]temp=line.split(" --");
				for(int i=0;i<temp.length;i++) {
					if(!temp[i].startsWith("--")) temp[i]="--".concat(temp[i]);
					String[]parse=temp[i].split(" ",2);
					for(int j=0;j<parse.length;j++) {
						if(!parse[j].equals("--url")) {
							builder.command().add(parse[j]);
						}
					}
				}
				
				try {
					message.edit(msg->{
						msg.setComponents(ActionRow.of(Button.danger(uuid,"Cancel")));
						msg.setContent("Starting download...");
					}).subscribe();
					
					Disposable listener=message.getClient().on(ButtonInteractEvent.class,event->{
						if((cancel=event.getCustomId().equals(uuid))) {
							return event.edit(msg->{
								msg.setContent("");
								msg.addEmbed(embed->{
									embed.setTitle("Aborted");
									embed.setDescription("Media download aborted by user.");
									embed.setColor(Color.RED);
								});
								msg.setComponents(new ArrayList<LayoutComponent>());
							});
						}
						return Mono.empty();
					}).subscribe();
					
					Process process=builder.start();

					BufferedReader input=process.inputReader();
					while(process.isAlive()) {
						if(input!=null&&input.ready()) {
							String out=input.readLine();
							if(out.startsWith("[download] Destination:")) {
								String filename=out.substring(23);
								message.edit(msg->{
									msg.removeEmbeds();
									msg.addEmbed(embed->{
										embed.setTitle("Downloading");
										embed.setDescription(filename);
										embed.setColor(Color.BLUE);
									});
								}).subscribe();
							} else if(out.startsWith("[ExtractAudio] Destination:")) {
								String filename=out.substring(27);
								message.edit(msg->{
									msg.removeEmbeds();
									msg.setContent("");
									msg.addEmbed(embed->{
										embed.setTitle("Media download finished.");
										embed.setDescription(filename);
										embed.setColor(Color.GREEN);
									});
									msg.setComponents(new ArrayList<LayoutComponent>());
								}).subscribe();
							} else if(out.startsWith("[download]")||out.startsWith("[youtube:tab]")) {
								message.edit(msg->{
									msg.setContent(out);
								}).subscribe();
							} else if(out.startsWith("ERROR:")) {
								message.edit(msg->{
									msg.setContent("").removeEmbeds().addEmbed(embed->{
										embed.setTitle("ERROR");
										embed.setDescription(out);
										embed.setColor(Color.RED);
									});
									msg.setComponents(new ArrayList<LayoutComponent>());
								}).subscribe();								
							}
							if(out.endsWith("already been downloaded")) {
								String filename=out.substring(27);
								message.edit(msg->{
									msg.removeEmbeds();
									msg.setContent("");
									msg.addEmbed(embed->{
										embed.setTitle("Media already been downloaded.");
										embed.setDescription(filename);
										embed.setColor(Color.GREEN);
									});
									msg.setComponents(new ArrayList<LayoutComponent>());
								}).subscribe();
							}
						}
						if(cancel) process.destroy();
					}
					if(listener!=null&&!listener.isDisposed()) listener.dispose();

					StringBuilder string=new StringBuilder();
					BufferedReader error=process.errorReader();
					if(error!=null&&error.ready()) {
						error.lines().filter(line->{
							return line.startsWith("yt-dlp: error: ")||line.startsWith("ERROR:");
						}).forEach(line->{
							string.append(line);
						});
						if(string.length()>0) {
							message.edit(msg->{
								msg.setContent("").removeEmbeds().addEmbed(embed->{
									embed.setTitle("ERROR");
									embed.setDescription(string.toString());
									embed.setColor(Color.RED);
								});
								msg.setComponents(new ArrayList<LayoutComponent>());
							}).subscribe();
						}
					}
					
					if(process.isAlive()) process.destroyForcibly();
					
				} catch (IOException e) {
					e.printStackTrace();
				}				
			}).doOnError(error->{
				ConsoleRunner.printErr(error.getMessage());
			}).subscribe();			
		}
	}

	@Override
	public Mono<Void> execute(String string, MessageChannel channel) {
		
		Helper.executor.submit(new ProcessCallback(string,channel));
		return Mono.empty();
	}

}
