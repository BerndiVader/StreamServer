package com.gmail.berndivader.streamserver.discord.permission;

public class User {

	public static enum Rank {GUEST,MEMBER,MOD,ADMIN}
	
	public String name;
	public Rank rank;
	
	public User(String name,Rank rank) {
		this.name=name;
		this.rank=rank;
	}
	
}