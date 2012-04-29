package com.emerginggames.snappersbackend;

import org.json.JSONException;
import org.json.JSONObject;

public class PromoOkMessage extends JSONObject {
	public PromoOkMessage(int promoHints) {
		try {
			this.put("type", "PromoOkMessage");
			this.put("promo_hints", promoHints);
		} catch (JSONException e) {
		}
	}

}
