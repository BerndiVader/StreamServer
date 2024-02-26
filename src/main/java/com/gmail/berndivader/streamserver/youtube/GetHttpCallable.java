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

import com.gmail.berndivader.streamserver.Helper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public abstract class GetHttpCallable<T> implements Callable<T> {
	
	private String query;

	public GetHttpCallable(String query) {
		this.query=query;
	}

	@Override
	public T call() throws Exception {
		HttpUriRequest request=new HttpGet(query);
		JsonObject json=Helper.httpClient.execute(request,new ResponseHandler<JsonObject>() {

			@Override
			public JsonObject handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
				String text="";
			    try (Scanner scanner=new Scanner(response.getEntity().getContent(), StandardCharsets.UTF_8.name())) {
			        text=scanner.useDelimiter("\\A").next();
			    }
				return JsonParser.parseString(text).getAsJsonObject();
			}
		});
		
		return handle(json);
	}
	
	protected abstract T handle(JsonObject json);
	protected abstract T handleErr(JsonObject json);

}
