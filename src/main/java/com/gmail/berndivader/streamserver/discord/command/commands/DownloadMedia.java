package com.gmail.berndivader.streamserver.discord.command.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import com.gmail.berndivader.streamserver.Helper;
import com.gmail.berndivader.streamserver.Utils;
import com.gmail.berndivader.streamserver.Utils.InfoPacket;
import com.gmail.berndivader.streamserver.annotation.DiscordCommand;
import com.gmail.berndivader.streamserver.config.Config;
import com.gmail.berndivader.streamserver.discord.command.Command;
import com.gmail.berndivader.streamserver.term.ANSI;

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
			File directory=new File(Config.DL_MUSIC_PATH);
			if(!directory.exists()) {
				directory.mkdir();
			}
			if(!directory.exists()||directory.isFile()) {
								
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
				
				ProcessBuilder builder=new ProcessBuilder();
				builder.directory(directory);
				if(line.startsWith("--no-default")) {
					line=line.substring(12);
					builder.command("yt-dlp"
							,"--progress-delta","2"
							,"--restrict-filenames"
							,"--embed-metadata"
							,"--embed-thumbnail"
							,"--output","%(title).200s.%(ext)s"
					);
				} else {
					builder.command("yt-dlp"
							,"--progress-delta","2"
							,"--embed-metadata"
							,"--embed-thumbnail"
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
				String url="";
				for(int i=0;i<temp.length;i++) {
					if(temp[i].isEmpty()) continue;
					if(!temp[i].startsWith("--")) temp[i]="--".concat(temp[i]);
					String[]parse=temp[i].split(" ",2);
					for(int j=0;j<parse.length;j++) {
						if(parse[j].equals("--url")) {
							if(parse.length==2) url=parse[1];
							continue;
						}
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
											embed.setTitle("Warning!")
											.setColor(Color.BROWN)
											.setDescription("Warning! Download directory is a file, using default.");
										});
									}).subscribe();
								}
							}
							continue;
						}
						if(!parse[j].isEmpty()) builder.command().add(parse[j]);
					}
				}
				
				InfoPacket infoPacket=null;
				if(!url.isEmpty()) infoPacket=Utils.getDLPinfoPacket(url,builder.directory());
				
				try {
					message.edit(msg->{
						msg.setComponents(ActionRow.of(Button.danger(uuid,"Cancel")))
						.setContent("Starting download...");
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

					BufferedReader input=process.inputReader();
					StringBuilder filename=new StringBuilder();
					long time=System.currentTimeMillis();
					status=Status.RUNNING;
					
					while(status==Status.RUNNING) {
						if(!process.isAlive()) {
							status=Status.FINISHED;
							break;
						}
						if(input.ready()) {
							time=System.currentTimeMillis();
							String out=input.readLine();
							if(out.startsWith("[download] Destination:")) {
								filename.append(out.substring(23));
								message.edit(msg->{
									msg.removeEmbeds()
									.addEmbed(embed->{
										embed.setTitle("Downloading")
										.setDescription(filename.toString())
										.setColor(Color.BLUE);
									});
								}).subscribe();
							} else if(out.startsWith("[ExtractAudio] Destination:")) {
								filename.setLength(0);
								filename.append(out.replace("[ExtractAudio] Destination: ",""));
							} else if(out.startsWith("[download]")) {
								message.edit(msg->{
									msg.setContent(out);
								}).subscribe();
							} else if(out.startsWith("ERROR:")) {
								message.edit(msg->{
									msg.setContent("").removeEmbeds().addEmbed(embed->{
										embed.setTitle("ERROR")
										.setDescription(out)
										.setColor(Color.RED);
									});
									msg.setComponents(new ArrayList<LayoutComponent>());
								}).subscribe();
							}
						} else if(System.currentTimeMillis()-time>Config.DL_TIMEOUT_SECONDS*1000l) status=Status.TIMEOUT;
					}
					
					BufferedReader error=process.errorReader();
					StringBuilder errorBuilder=new StringBuilder();
					error.lines().filter(line->!line.startsWith("WARNING")).forEach(line->errorBuilder.append(line));
					
					if(errorBuilder.length()>0) status=Status.ERROR;
					
					message.edit(msg->{
						msg.setComponents(new ArrayList<LayoutComponent>())
						.setContent("")
						.addEmbed(embed->{
							switch(status) {
							case TIMEOUT:
								embed.setTitle("TIMEOUT")
								.setDescription("Download will be terminated, because it appears, that the process is stalled since "+(long)(Config.DL_TIMEOUT_SECONDS/60)+" minutes.")
								.setColor(Color.RED);
								break;
							case FINISHED:
								embed.setTitle("Media download finished.")
								.setDescription(filename.toString())
								.setColor(Color.GREEN);
								break;
							case ERROR:
								embed.setTitle("ERROR")
								.setColor(Color.ORANGE)
								.setDescription("Something went wront.\n\n".concat(errorBuilder.toString()));
								break;
							default:
								embed.setTitle("WARNING")
								.setDescription("Mediafile already downloaded and exists.")
								.setColor(Color.ORANGE);
								break;
							}
						});
					}).subscribe();
					
					if(listener!=null&&!listener.isDisposed()) listener.dispose();
					if(process.isAlive()) process.destroyForcibly();
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}).doOnError(error->{
				ANSI.printErr(error.getMessage());
			}).subscribe();
		}
	}

	@Override
	public Mono<Void> execute(String string, MessageChannel channel) {
		
		Helper.executor.submit(new ProcessCallback(string,channel));
		return Mono.empty();
	}
	
}
