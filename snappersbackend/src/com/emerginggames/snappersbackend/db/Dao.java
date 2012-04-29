package com.emerginggames.snappersbackend.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.emerginggames.snappersbackend.Friend;
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
					log.error("SQLException: " + e.getMessage());
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
				p.setDollarsSpent(rst.getInt("dollars_spent"));
				p.setGifts(rst.getString("gifts"));
			}
		} catch (SQLException e) {
			log.error("SQLException: " + e.getMessage());
		} finally {
			if (rst != null)
				try {
					rst.close();
				} catch (SQLException e) {
					log.error("SQLException: " + e.getMessage());
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
					log.error("SQLException: " + e.getMessage());
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
				" dollars_spent = ?, " +
				" last_played_date = now(), " +
				" gifts = NULL" +
				" where facebook_id = ?";
		PreparedStatement stmt = null;
		boolean ok = false;
		try {
			stmt = c.prepareStatement(q);
			stmt.setInt(1, p.getHintCount());
			stmt.setInt(2, p.getXpCount());
			stmt.setInt(3, p.getXpLevel());
			stmt.setString(4, p.getUserDefaults());
			stmt.setInt(5, p.getDollarsSpent());
			stmt.setLong(6, p.getFacebookId());
			stmt.executeUpdate();
			ok = true;
		} catch (SQLException e) {
			log.error("SQLException: " + e.getMessage());
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
					log.error("SQLException: " + e.getMessage());
				}
		}		
		return ok;
	}
	
	public void getXpOfFriends(ArrayList<Friend> friends) {
		String q = "select facebook_id,xp_count from players\n" +
				" where facebook_id in (";
		HashMap<Long, Friend> friendsMap = new HashMap<Long, Friend>();
		for (int i = 0; i < friends.size(); i++) {
			Friend f = friends.get(i);
			if(i>0)
				q+=",";
			q += ""+f.getFacebookId();
			friendsMap.put(Long.valueOf(f.getFacebookId()), f);
		}
		q+=")";
		ResultSet rst = null;
		PreparedStatement stmt = null;
		
		try {
			stmt = c.prepareStatement(q);
			
			rst = stmt.executeQuery();
			while (rst.next()) {
				Friend f = friendsMap.get(Long.valueOf(rst.getLong("facebook_id")));
				f.setXpCount(rst.getInt("xp_count"));
			}
		} catch (SQLException e) {
			log.error("SQLException: " + e.getMessage());
		} finally {
			if (rst != null)
				try {
					rst.close();
				} catch (SQLException e) {
					log.error("SQLException: " + e.getMessage());
				}
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
					log.error("SQLException: " + e.getMessage());
				}
		}		
	}
	
	public boolean updateInvite(long invite_from) {
		String q = "update players set invites_sent_count = invites_sent_count + 1 where facebook_id = " + invite_from;
		PreparedStatement stmt = null;
		
		boolean ok = false;
		try {
			stmt = c.prepareStatement(q);
			stmt.executeUpdate();			
			ok = true;
		} catch (SQLException e) {
			log.error("SQLException: " + e.getMessage());
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
					log.error("SQLException: " + e.getMessage());
				}
		}		
		return ok;		
	}
	
	public boolean updateShare(long facebookId) {
		String q = "update players set shares_count = shares_count + 1 where facebook_id = " + facebookId;
		PreparedStatement stmt = null;
		
		boolean ok = false;
		try {
			stmt = c.prepareStatement(q);
			stmt.executeUpdate();			
			ok = true;
		} catch (SQLException e) {
			log.error("SQLException: " + e.getMessage());
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
					log.error("SQLException: " + e.getMessage());
				}
		}		
		return ok;		
	}
	
	public boolean updateGift(long gift_from, long gift_to) {
		String q1 = "update players set gifts_sent_count = gifts_sent_count + 1 where facebook_id = " + gift_from;
		String q2 = "update players set gifts=concat_ws(',',gifts," + gift_from + ") where facebook_id = " + gift_to;
		PreparedStatement stmt = null;
		
		boolean ok = false;
		try {
			stmt = c.prepareStatement(q1);
			stmt.executeUpdate();
			
			stmt = c.prepareStatement(q2);
			stmt.executeUpdate();
			
			ok = true;
		} catch (SQLException e) {
			log.error("SQLException: " + e.getMessage());
		} finally {
			if (stmt != null)
				try {
					stmt.close();
				} catch (SQLException e) {
					log.error("SQLException: " + e.getMessage());
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
