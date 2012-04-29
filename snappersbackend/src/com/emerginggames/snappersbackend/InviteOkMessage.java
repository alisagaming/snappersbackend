package com.emerginggames.snappersbackend;

import org.json.JSONException;
import org.json.JSONObject;

public class InviteOkMessage extends JSONObject {
	public InviteOkMessage() {
		try {
			this.put("type", "InviteOkMessage");
		} catch (JSONException e) {
		}
	}

}
