package com.gmail.berndivader.streamserver;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.gmail.berndivader.streamserver.term.ANSI;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Helper {
	
	public static ExecutorService executor;
	public static ScheduledExecutorService scheduledExecutor;
	public static CloseableHttpClient httpClient;
	public static Gson gson;
	public static File[] files;
	public static File[] customs;
	
	static {
		files=new File[0];
		customs=new File[0];
		executor=Executors.newFixedThreadPool(10);
		scheduledExecutor=Executors.newScheduledThreadPool(5);
		httpClient=HttpClients.createMinimal();
		gson=new GsonBuilder().create();
	}
	
	public static void close() {
		ANSI.print("[WHITE]Shutdown task executor...");
		executor.shutdown();
		ANSI.println("[GREEN]DONE!");
		ANSI.print("[WHITE]Shutdown scheduled task executor...");
		scheduledExecutor.shutdown();
		ANSI.println("[GREEN]DONE![/GREEN]");
	}

}
