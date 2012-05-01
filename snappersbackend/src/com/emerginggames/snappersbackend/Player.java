package com.emerginggames.snappersbackend;

import java.util.Date;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class Player {
	private static Logger log = Logger.getLogger(Player.class);
	
	private final static int LevelPacksCount = 5;
	
	private int playerId;
	private long facebookId;
	private String name;
	private String fullName;
	private String email;
	private String country;
	private String gender;
	private Date registrationDate;
	private Date lastPlayedDate;
	private int hintCount;
	private int xpCount;
	private int xpLevel;
	private String promoCodeUsed;
	private String userDefaults;
	private String accessToken;
	private String gifts;
	private int dollarsSpent;
	
	public static Player parseFromJSON(JSONObject json) {
		Player player = new Player();
		try {
			if (json.has("access_token"))
				player.setAccessToken(json.getString("access_token"));
			else
				return null;

			if (json.has("xp_count"))
				player.setXpCount(json.getInt("xp_count"));
			else
				return null;

			if (json.has("xp_level"))
				player.setXpLevel(json.getInt("xp_level"));
			else
				return null;

			if (json.has("hint_count"))
				player.setHintCount(json.getInt("hint_count"));
			else
				return null;

			if (json.has("dollars_spent"))
				player.setDollarsSpent(json.getInt("dollars_spent"));
			else
				return null;

			JSONObject maxUnlockedLevelForPackDict = new JSONObject();
			for (int i = 1; i <= LevelPacksCount; i++) {
				if (json.has("max_unlocked_level_for_pack"+i)) {
					maxUnlockedLevelForPackDict.put(""+i, json.getInt("max_unlocked_level_for_pack"+i));
				}
			}
			player.setUserDefaults(maxUnlockedLevelForPackDict.toString());

		} catch (JSONException e) {
			return null;
		}
		
		if (!player.validateAccessToken())
			return null;
		
		return player;
	}
	
	private boolean validateAccessToken() {
		if (accessToken == null)
			return false;
		
		try {
			JSONObject json = FacebookController.getFacebookController().getFacebookUserProfile(accessToken);
			if (json == null)
				return false;
			if (json.has("uid"))
				setFacebookId(json.getLong("uid"));
			else
				return false;

			if (json.has("first_name"))
				setName(json.getString("first_name"));

			if (json.has("name"))
				setFullName(json.getString("name"));

			if (json.has("email"))
				setEmail(json.getString("email"));
			
			if (json.has("sex"))
				setGender(json.getString("sex"));

		} catch (JSONException e) {
			return false;
		}

		return true;
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("code", FacebookController.getFacebookController().getCodeForFacebookId(facebookId));
			json.put("hint_count", getHintCount());
			json.put("xp_count", getXpCount());
			json.put("xp_level", getXpLevel());
			json.put("gifts", getGifts());
			json.put("dollars_spent", getDollarsSpent());
			JSONObject maxUnlockedLevelForPackDict = new JSONObject(getUserDefaults());
			for (int i = 1; i <= LevelPacksCount; i++) {
				if (maxUnlockedLevelForPackDict.has(""+i)) {
					json.put("max_unlocked_level_for_pack"+i, maxUnlockedLevelForPackDict.getInt(""+i));
				}
			}
		} catch (JSONException e) {
			log.error("JSON Exception " + e);
			return null;
		}
		return json;
	}

	public int getPlayerId() {
		return playerId;
	}

	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

	public long getFacebookId() {
		return facebookId;
	}

	public void setFacebookId(long facebookId) {
		this.facebookId = facebookId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public Date getRegistrationDate() {
		return registrationDate;
	}

	public void setRegistrationDate(Date registrationDate) {
		this.registrationDate = registrationDate;
	}

	public Date getLastPlayedDate() {
		return lastPlayedDate;
	}

	public void setLastPlayedDate(Date lastPlayedDate) {
		this.lastPlayedDate = lastPlayedDate;
	}

	public int getHintCount() {
		return hintCount;
	}

	public void setHintCount(int hintCount) {
		this.hintCount = hintCount;
	}

	public int getXpCount() {
		return xpCount;
	}

	public void setXpCount(int xpCount) {
		this.xpCount = xpCount;
	}

	public int getXpLevel() {
		return xpLevel;
	}

	public void setXpLevel(int xpLevel) {
		this.xpLevel = xpLevel;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getUserDefaults() {
		return userDefaults;
	}

	public void setUserDefaults(String userDefaults) {
		this.userDefaults = userDefaults;
	}

	public String getGifts() {
		return gifts;
	}

	public void setGifts(String gifts) {
		this.gifts = gifts;
	}

	public int getDollarsSpent() {
		return dollarsSpent;
	}

	public void setDollarsSpent(int dollarsSpent) {
		this.dollarsSpent = dollarsSpent;
	}

	public String getPromoCodeUsed() {
		return promoCodeUsed;
	}

	public void setPromoCodeUsed(String promoCodeUsed) {
		this.promoCodeUsed = promoCodeUsed;
	}

}
