package com.emerginggames.snappersbackend;

import org.json.JSONException;
import org.json.JSONObject;

public class GiftOkMessage extends JSONObject {
	public GiftOkMessage() {
		try {
			this.put("type", "GiftOkMessage");
		} catch (JSONException e) {
		}
	}

}
