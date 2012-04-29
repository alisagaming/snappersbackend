package com.emerginggames.snappersbackend;

import org.json.JSONException;
import org.json.JSONObject;

public class ShareOkMessage extends JSONObject {
	public ShareOkMessage() {
		try {
			this.put("type", "ShareOkMessage");
		} catch (JSONException e) {
		}
	}

}
