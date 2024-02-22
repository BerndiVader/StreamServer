package com.gmail.berndivader.streamserver.console.command.commands;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.gmail.berndivader.streamserver.annotation.ConsoleCommand;
import com.gmail.berndivader.streamserver.console.ConsoleRunner;
import com.gmail.berndivader.streamserver.console.command.Command;
import com.gmail.berndivader.streamserver.youtube.Youtube;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@ConsoleCommand(name="streamby")
public class LivestreamsById extends Command{

	@Override
	public boolean execute(String[] args) {
		JsonObject json=null;
		for(int i=0;i<args.length;i++) {
			if(args[i].length()==0) continue;
			Future<JsonObject>future=Youtube.livestreamsByChannelId(args[i]);
			try {
				json=future.get(15l,TimeUnit.SECONDS);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				e.printStackTrace();
				return false;
			}
			if(json.get("error")==null) {
				if(json.get("items").isJsonArray()) {
					JsonArray array=json.get("items").getAsJsonArray();
					int size=array.size();
					for(int j=0;j<size;j++) {
						JsonObject item=array.get(j).getAsJsonObject();
						ConsoleRunner.println(item.toString());
					}
				}
			}else {
				
			}
		}
		return true;
	}

}
