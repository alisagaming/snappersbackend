package com.emerginggames.snappersbackend;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.*;

public class FacebookController {
	private final Logger log = Logger.getLogger(FacebookController.class);
	private static FacebookController instance = null;

	public synchronized static FacebookController getFacebookController() {
		if (instance == null)
			instance = new FacebookController();
		return instance;
	}
	
	@Test
	public void testMe () {
		System.out.println(getFacebookUserProfile("BAAC9ioV7jsIBAMT8ZCdW8i00y0XsrQVZB62qYNwjRD7PzltNGxIRhMPvAlCLYJBTYCdGWgPm7eZAtQLbkA99tShGVpWH68Vy7AJxuaDMqU64W9EH4VFFCPxsyLLDuYZD"));
	}

	public JSONObject getFacebookUserProfile(String accessToken) {
		String query = "SELECT uid,first_name,name,email,sex FROM user WHERE uid=me()";
		String request = "https://api.facebook.com/method/fql.query?" + 
				"query=" + query.replaceAll(" ", "%20") +
				"&format=json&access_token=" + accessToken;
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(request);

		JSONObject res = null;
		try {
			// Create a response handler
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String responseBody = httpClient.execute(httpget, responseHandler);
			JSONArray array = new JSONArray(responseBody);
			assert array.length() == 1 : "invalid json array";
			res = array.getJSONObject(0);
		} catch (Exception e) {
			// do nothing
			log.error("failed to get facebook profile for token " + accessToken);
		}
		finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			httpClient.getConnectionManager().shutdown();
		}


		return res;
	}
}
