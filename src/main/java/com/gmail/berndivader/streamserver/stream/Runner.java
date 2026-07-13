package com.gmail.berndivader.streamserver.stream;

import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import com.gmail.berndivader.streamserver.Helper;

public class Runner extends TimerTask {
	
	private final long PERIOD=15l;
	
	public Runner() {
		Helper.SCHEDULED_EXECUTOR.scheduleAtFixedRate(this,0l,PERIOD,TimeUnit.SECONDS);
	}

	@Override
	public void run() {
		
	}

}
