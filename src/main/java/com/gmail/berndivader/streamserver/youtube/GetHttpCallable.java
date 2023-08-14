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

public abstract class GetHttpCallable implements Callable<Boolean> {
	
	private String query;

	public GetHttpCallable(String query) {
		this.query=query;
	}

	@Override
	public Boolean call() throws Exception {
		HttpUriRequest request=new HttpGet(query);
		JsonObject json=Helper.httpClient.execute(request,new ResponseHandler<JsonObject>() {
			

			@Override
			public JsonObject handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
				
				String text="";
			    try (Scanner scanner = new Scanner(response.getEntity().getContent(), StandardCharsets.UTF_8.name())) {
			        text = scanner.useDelimiter("\\A").next();
			    }
				JsonObject json=JsonParser.parseString(text).getAsJsonObject();
			    return json;
			}
		});
		
		if(json.get("error")==null) {
			return handle(json);
		}
		
		return handleErr(json);
	}
	
	protected abstract boolean handle(JsonObject json);
	
	protected boolean handleErr(JsonObject json) {
		
		return false;
	}

}
