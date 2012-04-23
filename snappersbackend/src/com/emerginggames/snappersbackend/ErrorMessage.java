package com.emerginggames.snappersbackend;

import org.json.JSONException;
import org.json.JSONObject;

public class ErrorMessage extends JSONObject {

	public ErrorMessage(String description) {
		try {
			this.put("type", "ErrorMessage");
			if (description != null)
				this.put("data", description);
		} catch (JSONException e) {
		}
	}
	
}
