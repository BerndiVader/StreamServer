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

@DiscordCommand(name="dlp",usage="Download media. [--no-default] [--dir] [--yes-playlist] --url <valid_url>")
public class DownloadMedia extends Command<Void> {
	
	private class ProcessCallback implements Runnable {
		
		private String line;
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
			StringBuilder filename=new StringBuilder();
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
				if(line.startsWith("--no-default")) {
					line=line.substring(12);
					builder.command("yt-dlp"
							,"--progress-delta","2"
							,"--restrict-filenames"
							,"--output","%(title).200s.%(ext)s"
					);
				} else {
					builder.command("yt-dlp"
							,"--progress-delta","2"
							,"--ignore-errors"
							,"--extract-audio"
							,"--format","bestaudio"
							,"--audio-format","mp3"
							,"--audio-quality","160K"
							,"--output","%(title).200s.%(ext)s"
							,"--restrict-filenames"
							,"--no-playlist"
					);
				}
				
				String[]temp=line.split(" --");
				for(int i=0;i<temp.length;i++) {
					if(temp[i].isEmpty()) continue;
					if(!temp[i].startsWith("--")) temp[i]="--".concat(temp[i]);
					String[]parse=temp[i].split(" ",2);
					for(int j=0;j<parse.length;j++) {
						if(parse[j].equals("--url")) continue;
						if(parse[j].equals("--dir")) {
							if(parse.length==2) {
								File dir=new File(Config.DL_MUSIC_PATH.concat("/").concat(parse[j+1]));
								parse[j+1]="";
								if(!dir.exists()) dir.mkdir();
								if(dir.isDirectory()) {
									builder.directory(dir);
								} else {
									message.edit(msg->{
										msg.addEmbed(embed->{
											embed.setTitle("Warning!");
											embed.setColor(Color.BROWN);
											embed.setDescription("Warning! Download directory is a file, using default.");
										});
									}).subscribe();
								}
							}
							continue;
						}
						if(!parse[j].isEmpty()) builder.command().add(parse[j]);
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
					long time=System.currentTimeMillis();
					while(process.isAlive()) {
						if(input!=null&&input.ready()) {
							time=System.currentTimeMillis();
							String out=input.readLine();
							if(out.startsWith("[download] Destination:")) {
								filename.append(out.substring(23));
								message.edit(msg->{
									msg.removeEmbeds();
									msg.addEmbed(embed->{
										embed.setTitle("Downloading");
										embed.setDescription(filename.toString());
										embed.setColor(Color.BLUE);
									});
								}).subscribe();
							} else if(out.startsWith("[download]")) {
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
									filename.setLength(0);
								}).subscribe();
							}
						} else if(System.currentTimeMillis()-time>Config.DL_TIMEOUT_SECONDS*1000l) {
							message.edit(msg->{
								msg.setContent("").removeEmbeds().addEmbed(embed->{
									embed.setTitle("TIMEOUT");
									embed.setDescription("Download will be terminated, because it appears, that the process is stalled since "+(long)(Config.DL_TIMEOUT_SECONDS/60)+" minutes.");
									embed.setColor(Color.RED);
								});
								msg.setComponents(new ArrayList<LayoutComponent>());
								filename.setLength(0);
							}).subscribe();
							cancel=true;
						}
						if(cancel) process.destroy();
					}
					
					BufferedReader error=process.errorReader();
					if(error!=null&&error.ready()) {
						StringBuilder string=new StringBuilder();
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
					} else {
						message.edit(msg->{
							msg.removeEmbeds();
							msg.setContent("");
							msg.addEmbed(embed->{
								if(filename.length()>0) {
									embed.setTitle("Media download finished.");
									embed.setDescription(filename.toString());
									embed.setColor(Color.GREEN);
								} else {
									embed.setTitle("ERROR");
									embed.setDescription("Media download finished unexpected.");
									embed.setColor(Color.RED);
								}
							});
							msg.setComponents(new ArrayList<LayoutComponent>());
						}).subscribe();
					}
					
					if(listener!=null&&!listener.isDisposed()) listener.dispose();
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
