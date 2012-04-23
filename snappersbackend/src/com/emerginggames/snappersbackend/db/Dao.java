package com.emerginggames.snappersbackend.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.log4j.Logger;

import com.emerginggames.snappersbackend.Player;


public class Dao {
	protected Connection c = null;
	Logger log = Logger.getLogger(Dao.class);
	
	
	protected Dao(boolean autoCommit) throws SQLException{
		c = Connector.getConnection();
		try{
			if (autoCommit)
				c.setAutoCommit(true);
			else
				c.setAutoCommit(false);
		}
		catch (Exception e){
			if (c == null){
				e.printStackTrace();
				log.info(e.getMessage());
				throw new SQLException(e);
			}
			log.info("dao conneciton stale, reconnecting");
			
			c = Connector.getConnection();

			if (autoCommit)
				c.setAutoCommit(true);
			else
				c.setAutoCommit(false);
		}
	}

	public Dao() throws SQLException{
		c = Connector.getConnection();
		try{
			c.setAutoCommit(true);
		}catch (Exception e){//try again if connection stale!
			if (c == null){
				e.printStackTrace();
				log.info(e.getMessage());
				throw new SQLException(e);
			}
			log.info("dao conneciton stale, reconnecting");
			c = Connector.getConnection();
			c.setAutoCommit(true);
		}
	}
	
	protected Dao(Dao d){
		this.c = d.c;
	}
	
	public void rollback(){
		try {
			c.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
			log.error("Failed to rollback connection");
		}
	}
	
	public void close(){
		try {
			if (c.getAutoCommit() == false)
				close(true);
			else
				c.close();
		} catch (SQLException e) {
			e.printStackTrace();
			log.error("Failed to close connection c gracefully");
		}
	}
	
	public Player insertPlayer(Player p) {
		String q = "insert into players (facebook_id, name, full_name, email, gender, registration_date, last_played_date, xp_level, xp_count, hint_count, user_defaults)"+
				" values (?, ?, ?, ?, ?, now(), now(), ?, ?, ?, ?)";
		PreparedStatement stmt = null;

		try {
			stmt = c.prepareStatement(q, Statement.RETURN_GENERATED_KEYS);
			stmt.setLong(1, p.getFacebookId());
			stmt.setString(2, p.getName());
			stmt.setString(3, p.getFullName());
			stmt.setString(4, p.getEmail());
			stmt.setString(5, p.getGender());
			stmt.setInt(6, p.getXpLevel());
			stmt.setInt(7, p.getXpCount());
			stmt.setInt(8, p.getHintCount());
			stmt.setString(9, p.getUserDefaults());
			stmt.executeUpdate();
			ResultSet rs = stmt.getGeneratedKeys();
			if (rs.next()) {
				p.setPlayerId(rs.getInt(1));
			}
		} catch (SQLException e) {
			log.error("SQLException: " + e.getMessage());
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}		
		return p;		
	}
	
	public Player loadPlayer(long facebookId) {
		String q = "select * from players\n" +
				" where facebook_id = ?";
		ResultSet rst = null;
		PreparedStatement stmt = null;
		Player p = null;
		
		try {
			stmt = c.prepareStatement(q);
			stmt.setLong(1, facebookId);
			
			rst = stmt.executeQuery();
			if (rst.next()) {
				p = new Player();
				p.setFacebookId(facebookId);
				p.setName(rst.getString("name"));
				p.setFullName(rst.getString("full_name"));
				p.setEmail(rst.getString("email"));
				p.setCountry(rst.getString("country"));
				p.setGender(rst.getString("gender"));
				p.setRegistrationDate(rst.getDate("registration_date"));
				p.setLastPlayedDate(rst.getDate("last_played_date"));
				p.setXpCount(rst.getInt("xp_count"));
				p.setXpLevel(rst.getInt("xp_level"));
				p.setHintCount(rst.getInt("hint_count"));
				p.setUserDefaults(rst.getString("user_defaults"));
			}
		} catch (SQLException e) {
			log.error("SQLException: " + e.getMessage());
		} finally {
			if (rst != null)
				try {
					rst.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
		return p;
	}

	public boolean updatePlayer(Player p) {
		String q = "update players set " +
				" hint_count = ?, " +
				" xp_count = ?, " +
				" xp_level = ?, " +
				" user_defaults = ?, " +
				" last_played_date = now() " +
				" where facebook_id = ?";
		ResultSet rst = null;
		PreparedStatement stmt = null;
		boolean ok = false;
		try {
			stmt = c.prepareStatement(q);
			stmt.setInt(1, p.getHintCount());
			stmt.setInt(2, p.getXpCount());
			stmt.setInt(3, p.getXpLevel());
			stmt.setString(4, p.getUserDefaults());
			stmt.setLong(5, p.getFacebookId());
			stmt.executeUpdate();
			ok = true;
		} catch (SQLException e) {
			log.error("SQLException: " + e.getMessage());
		} finally {
			if (rst != null)
				try {
					rst.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}		
		return ok;
	}	
	
	public void close(boolean commit){
		try{
			if (commit)
				c.commit();
			else
				c.rollback();
		
			c.close();
		}
		catch (SQLException se){
			se.printStackTrace();
			log.info("failed to close connection");
		}
	}
	

}
