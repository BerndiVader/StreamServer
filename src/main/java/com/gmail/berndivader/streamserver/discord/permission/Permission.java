package com.gmail.berndivader.streamserver.discord.permission;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.gmail.berndivader.streamserver.discord.permission.User.Rank;

@Retention(RetentionPolicy.RUNTIME)
public @interface Permission {
	public Rank required() default Rank.GUEST;	
}
