package com.gmail.berndivader.streamserver;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Helper {
	
	public static ExecutorService executor;
	public static ScheduledExecutorService scheduledExecutor;
	public static File[] files;
	
	static {
		executor=Executors.newFixedThreadPool(10);
		scheduledExecutor=Executors.newScheduledThreadPool(5);
	}
	
	public static void close() {
		ConsoleRunner.print("Shutdown task executor...");
		executor.shutdown();
		ConsoleRunner.println("DONE!");
		ConsoleRunner.print("Shutdown scheduled task executor...");
		scheduledExecutor.shutdown();
		ConsoleRunner.println("DONE!");
	}

}
