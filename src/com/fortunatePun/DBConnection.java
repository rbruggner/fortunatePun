package com.fortunatePun;


import com.google.appengine.api.backends.BackendService;
import com.google.appengine.api.utils.SystemProperty;
import com.google.apphosting.api.ApiProxy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection{
	
	public Connection connection;
	private String db_url;
	
	public DBConnection(){
		if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production){
			try {
				Class.forName("com.mysql.jdbc.GoogleDriver");
				this.db_url = "jdbc:google:mysql://fortunatepun:datastore/fortunatepun?user=root";
			} catch (ClassNotFoundException e) {
				System.err.println("Can't find class com.mysql.jdbc.GoogleDriver");
				e.printStackTrace();
			}
		} else {
			this.db_url = "jdbc:mysql://173.194.109.208:3306/fortunatepun?user=root&password=thatspunny";
		}
	}
	
	public void connect(){
		try {
			this.connection = DriverManager.getConnection(this.db_url);
		} catch (SQLException e) {
			System.err.println("Failed to connect to database:" + this.db_url + ":" + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void disconnect(){
		try {
			this.connection.close();
		} catch (SQLException e) {
			System.err.println("Failed to close database connection:" + e.getMessage());
			e.printStackTrace();
		}
	}
	
}