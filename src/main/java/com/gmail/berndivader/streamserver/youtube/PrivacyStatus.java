package com.gmail.berndivader.streamserver.youtube;

public enum PrivacyStatus {
	PUBLIC,UNLISTED,PRIVATE;
	
	public String getName() {
		return this.name().toLowerCase();
	}
	
	public static boolean isEnum(String value) {
		value=value.toUpperCase();
		for(PrivacyStatus e:PrivacyStatus.class.getEnumConstants()) {
			if(e.name().equals(value)) return true;
		}
		return false;
	}

}
