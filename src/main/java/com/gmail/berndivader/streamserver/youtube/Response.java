package com.gmail.berndivader.streamserver.youtube;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.Callable;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public abstract class Response<T> implements Callable<T> {
	
	private String query;

	public Response(String query) {
		this.query=query;
	}

	@Override
	public T call() throws Exception {
		HttpUriRequest request=new HttpGet(query);
		JsonObject json=Youtube.HTTP_CLIENT.execute(request,new ResponseHandler<JsonObject>() {
			@Override
			public JsonObject handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
				String text="";
			    try(Scanner scanner=new Scanner(response.getEntity().getContent(), StandardCharsets.UTF_8.name())) {
			        text=scanner.useDelimiter("\\A").next();
			    }
				return JsonParser.parseString(text).getAsJsonObject();
			}
		});
		
		return !json.has("error")?handle(json):handleErr(json.get("error").getAsJsonObject());
	}
	
	protected abstract T handle(JsonObject json);
	protected abstract T handleErr(JsonObject json);

}
