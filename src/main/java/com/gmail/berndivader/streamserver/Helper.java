package com.gmail.berndivader.streamserver;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Helper {
	
	public static ExecutorService executor;
	public static File[] files;
	
	static {
		executor=Executors.newFixedThreadPool(10);
	}
	
	public static void close() {
		executor.shutdown();
	}

}
