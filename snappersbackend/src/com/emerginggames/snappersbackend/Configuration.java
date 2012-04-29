package com.emerginggames.snappersbackend;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

public class Configuration {
	private static Configuration instance;
	private JSONObject json;
	
	public static Configuration getConfiguration() {
		synchronized(Configuration.class) {
			if (instance == null) {
				instance = new Configuration();
				String jsonStr = Configuration.readResourceFile("config/config.json");
				try {
					instance.setJson(new JSONObject(jsonStr));
				} catch (JSONException e) {
					return null;
				}
			}
			return instance;
		}
	}
	
	public boolean isPostingToFacebookAllowed() {
		try {
			return json.getBoolean("allow_posting_to_fb");
		} catch (JSONException e) {
			return false;
		}
	}
	
	public String getAppleAppStoreUrl() {
		try {
			return json.getString("apple_appstore_url");
		} catch (JSONException e) {
			return null;
		}
	}
	
	public String getAppImageUrl() {
		try {
			return json.getString("app_image_url");
		} catch (JSONException e) {
			return null;
		}
	}

	@SuppressWarnings("deprecation")
	public static String readResourceFile(String filePath) {
		File file = new File(filePath);
		if (!file.exists())
			return "";
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		DataInputStream dis = null;
		StringBuffer res = new StringBuffer();

		try {
			fis = new FileInputStream(file);

			// Here BufferedInputStream is added for fast reading.
			bis = new BufferedInputStream(fis);
			dis = new DataInputStream(bis);

			// dis.available() returns 0 if the file does not have more lines.
			while (dis.available() != 0) {
				res.append(dis.readLine());
			}

			// dispose all the resources after using them.
			fis.close();
			bis.close();
			dis.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res.toString();
	}

	public JSONObject getJson() {
		return json;
	}

	public void setJson(JSONObject json) {
		this.json = json;
	}
	
}
