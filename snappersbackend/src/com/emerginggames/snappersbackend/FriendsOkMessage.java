package com.emerginggames.snappersbackend;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FriendsOkMessage extends JSONObject {

	public FriendsOkMessage(ArrayList<Friend> friends) {
		try {
			this.put("type", "FriendsOkMessage");
			JSONArray data = new JSONArray();
			for (Friend friend: friends)
				data.put(friend.toJSON());
			if (data.length() > 0)
				this.put("data", data);
		} catch (JSONException e) {
		}
	}
}
