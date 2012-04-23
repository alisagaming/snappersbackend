package com.emerginggames.snappersbackend;

import org.json.JSONException;
import org.json.JSONObject;

public class SyncOkMessage extends JSONObject {
	
	public SyncOkMessage(JSONObject playerData) {
		try {
			this.put("type", "SyncOkMessage");
			if (playerData != null)
				this.put("data", playerData);
		} catch (JSONException e) {
		}
	}
}
