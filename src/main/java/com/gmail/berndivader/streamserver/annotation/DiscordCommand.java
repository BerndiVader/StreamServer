package com.gmail.berndivader.streamserver.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface DiscordCommand {
	public String name();
	public String usage();
	public Requireds[] requireds() default {Requireds.NONE};
}


		