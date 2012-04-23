package com.emerginggames.snappersbackend.db;

import java.io.*;
import java.sql.Connection;


import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class Connector {
	Connection conn = null;
	
	public static String login = null;
	public static String password = null;
	public static String jdbcUrl = null;
	public static int maxPoolSize = 0;
	public static int minPoolSize = 0;
	public static String driverName = "com.mysql.jdbc.Driver";
	public static int idleConnectionTestPeriod = 15;


	private static final Logger log = Logger.getLogger(Connector.class);

	public static ComboPooledDataSource ds = null;

	public Connector(String driverName, String url, String login,
			String password, int minPoolSize, int maxPoolSize){
		connectToDB(driverName, url, login, password, minPoolSize, maxPoolSize);
	}
	
	
	public static void initDatabase() throws JSONException {
		initDatabase("config/config.json");
	}
	
	@SuppressWarnings("deprecation")
	public static String readResourceFile(String filePath) {
		File file = new File(filePath);
		if (!file.exists())
			return "";
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		DataInputStream dis = null;
		StringBuffer res = new StringBuffer();

		try {
			fis = new FileInputStream(file);

			// Here BufferedInputStream is added for fast reading.
			bis = new BufferedInputStream(fis);
			dis = new DataInputStream(bis);

			// dis.available() returns 0 if the file does not have more lines.
			while (dis.available() != 0) {
				res.append(dis.readLine());
			}

			// dispose all the resources after using them.
			fis.close();
			bis.close();
			dis.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res.toString();
	}
	
	public static void initDatabase(String configFileName) throws JSONException {
		String conf = readResourceFile(configFileName);
		JSONObject obj;
		
		obj = new JSONObject(conf).getJSONObject("jdbc");
		login = obj.getString("login");
		password = obj.getString("password");
		driverName = obj.getString("driverName");
		jdbcUrl = obj.getString("url");
		minPoolSize = obj.getInt("minPoolSize");
		maxPoolSize = obj.getInt("maxPoolSize");
		
		log.info("Connecting to " + jdbcUrl + " as " + login);

		//c = new Connector(driverName, url, login, password, minPoolSize,
			//	maxPoolSize);
	}

	
	/**
	 * connectToDB - Connect to the MySql DB!
	 */
	private static void connectToDB(String driverName, String url, String login,
			String password, int minPoolSize, int maxPoolSize) {

		try {
			ds = new ComboPooledDataSource();
			
			ds.setDriverClass( driverName ); //loads the jdbc driver            
			ds.setJdbcUrl(url );
			ds.setUser(login);                                  
			ds.setPassword(password);    
			
			ds.setMinPoolSize(minPoolSize);
			ds.setMaxPoolSize(maxPoolSize);
			
			//ds.setTestConnectionOnCheckin(true);
			//ds.setTestConnectionOnCheckout(true);
			ds.setIdleConnectionTestPeriod(idleConnectionTestPeriod);
			
			ds.setPreferredTestQuery("select 1");

		} catch (Exception e) {
			log.error("Error when attempting to obtain DB Driver: "
					+ driverName + " on " + new Date().toString() + "with exception %s\n" + e.getMessage());
		}

		
	}

	

	public static synchronized Connection getConnection() {
		if (ds == null){
			try {
				initDatabase();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			connectToDB(driverName, 
					    jdbcUrl, login, password, 
					    minPoolSize, 
					    maxPoolSize);
			
		}
		
		Connection c = null;
		try {
			c = ds.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return c;
	}

	
	public static synchronized Connection getConnection(String configFileName) {
		if (ds == null){
			try {
				initDatabase(configFileName);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			connectToDB(driverName, 
					    jdbcUrl, login, password, 
					    minPoolSize, 
					    maxPoolSize);
			
		}
		
		Connection c = null;
		try {
			c = ds.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return c;
	}

	
	/**
	 * getNumLockedProcesses - gets the number of currently locked processes on
	 * the MySQL db
	 * 
	 * @return Number of locked processes
	 */
	public static int getNumLockedProcesses() {
		int num_locked_connections = 0;
		Connection con = null;
		PreparedStatement p_stmt = null;
		ResultSet rs = null;
		try {
			con = Connector.getConnection();
			p_stmt = con.prepareStatement("SHOW PROCESSLIST");
			rs = p_stmt.executeQuery();
			while (rs.next()) {
				if (rs.getString("State") != null
						&& rs.getString("State").equals("Locked")) {
					num_locked_connections++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				rs.close();
				p_stmt.close();
				con.close();
			} catch (java.sql.SQLException ex) {
				ex.printStackTrace();
			}
		}
		return num_locked_connections;
	}

}
