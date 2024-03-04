package com.gmail.berndivader.streamserver.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ConsoleCommand {
	public String name();
	public String usage();
}


		