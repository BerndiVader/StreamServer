package com.gmail.berndivader.streamserver.youtube;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

import com.gmail.berndivader.streamserver.term.ANSI;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public abstract class Response<T> implements Callable<T> {
	

	private static final String JSON_ERROR="{\"code\":-1,\"message\":\"%s\",\"errors\":[{\"message\":\"%s\",\"domain\":\"global\",\"%s\":\"badRequest\"}],\"status\":\"%s\"}";
	private String query;
	
	public Response(String query) {
		this.query=query;
	}

	@Override
	public T call() {
		HttpUriRequest request=new HttpGet(query);
		JsonObject json;
		
		try {
			json=Youtube.HTTP_CLIENT.execute(request,new ResponseHandler<JsonObject>() {
				
				@Override
				public JsonObject handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
					StringBuilder text=new StringBuilder();
					try(BufferedReader reader=new BufferedReader(new InputStreamReader(response.getEntity().getContent(),StandardCharsets.UTF_8))) {
						String line;
						while((line=reader.readLine())!=null)  text.append(line);
					}
					return JsonParser.parseString(text.toString()).getAsJsonObject();
				}

			});
		} catch (IOException e) {
			ANSI.printErr(e.getMessage(),e);
			json=Response.buildJsonError("Connection to Youtube failed.","ConnectionError","CONNECTION_ERROR");
			
		}
		
		return !json.has("error")?handle(json):handleErr(json.get("error").getAsJsonObject());
	}
	
	private static JsonObject buildJsonError(String message,String reason,String status) {
		return JsonParser.parseString(String.format(JSON_ERROR,message,message,reason,status)).getAsJsonObject();
	}
	
	protected abstract T handle(JsonObject json);
	protected abstract T handleErr(JsonObject json);

}
