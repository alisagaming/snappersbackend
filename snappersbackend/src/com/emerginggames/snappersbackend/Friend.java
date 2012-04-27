package com.emerginggames.snappersbackend;

import org.json.JSONException;
import org.json.JSONObject;

public class Friend implements Comparable<Friend> {
	private long facebookId;
	private String firstName;
	private boolean installed;
	private int xpCount;
	
	public static Friend parseFromJSON(JSONObject json) {
		Friend friend = new Friend();
		try {
			if (json.has("id"))
				friend.setFacebookId(json.getLong("id"));
			else
				return null;
			
			if (json.has("first_name"))
				friend.setFirstName(json.getString("first_name"));
			
			if (json.has("installed"))
				friend.setInstalled(json.getBoolean("installed"));

		} catch (JSONException e) {
			return null;
		}
		return friend;
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		try {
			json.put("facebook_id", getFacebookId());
			json.put("first_name", getFirstName());
			if (isInstalled()) {
				json.put("installed", isInstalled());
				json.put("xp_count", getXpCount());
			}
		} catch (JSONException e) {
			return null;
		}
		return json;		
	}
	
	public int compareTo(Friend other) {
		if (this.installed) {
			if (other.installed) {
				if (this.xpCount > other.xpCount)
					return 1;
				else if (this.xpCount < other.xpCount)
					return -1;
				else
					return 0;
			} else
				return 1;
		} else {
			if (other.installed)
				return -1;
			else
				return 0;
		}
	}

	public long getFacebookId() {
		return facebookId;
	}

	public void setFacebookId(long facebookId) {
		this.facebookId = facebookId;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public boolean isInstalled() {
		return installed;
	}

	public void setInstalled(boolean installed) {
		this.installed = installed;
	}

	public int getXpCount() {
		return xpCount;
	}

	public void setXpCount(int xpCount) {
		this.xpCount = xpCount;
	}
}
