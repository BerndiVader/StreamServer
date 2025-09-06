package com.gmail.berndivader.streamserver.config;

public class MySql {
	
	public Boolean USE;
	public String HOST;
	public String PORT;
	public String NAME;
	public String USER;
	public String PWD;
	public Long TIMEOUT_SECONDS;
	
	public MySql() {
		USE=false;
		HOST="MYSQL.HOST.NAME";
		PORT="3306";
		NAME="yampb";
		USER="MYSQL-USER-NAME";
		PWD="MYSQL-PASSWORD";
		TIMEOUT_SECONDS=10l;		
	}
	
}
