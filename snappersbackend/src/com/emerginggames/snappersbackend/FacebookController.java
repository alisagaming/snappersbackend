package com.emerginggames.snappersbackend;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
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
	
	@Test
	public void testFriends() {
		System.out.println(getFacebookFriends("BAAE3QVGpaOMBALZAczL9GQSnA30ZAFMzNUYzYsCxZAclNk3ZA4lkRvZAPVudxZAxOEZCUKZBhZAUbw8m8PzEqyXfNZAp5gVVGggUEDeR9ChYfYM2nw6zkUSql1WOgmvBzZBTREZD"));
	}
	
	public JSONArray getFacebookFriends(String accessToken) {
		String request = "https://graph.facebook.com/me/friends?fields=installed,first_name&access_token=" + accessToken;
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(request);

		JSONArray res = null;
		try {
			// Create a response handler
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String responseBody = httpClient.execute(httpget, responseHandler);
			JSONObject obj = new JSONObject(responseBody);
			res = obj.getJSONArray("data");
		} catch (Exception e) {
			// do nothing
			log.error("failed to get facebook friends for token " + accessToken);
		}
		finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			httpClient.getConnectionManager().shutdown();
		}


		return res;
	}
	
	public long getFacebookId(String accessToken) {
		String request = "https://graph.facebook.com/me?fields=id&access_token=" + accessToken;
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet(request);

		long result = 0;
		try {
			// Create a response handler
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String responseBody = httpClient.execute(httpget, responseHandler);
			JSONObject obj = new JSONObject(responseBody);
			result = obj.getLong("id");
		} catch (Exception e) {
			// do nothing
			log.error("failed to get facebook id for token " + accessToken);
		}
		finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			httpClient.getConnectionManager().shutdown();
		}


		return result;
	}
	
	@Test
	public void test_postOnUserWall() {
		postOnUserWall("BAAE3QVGpaOMBALZAczL9GQSnA30ZAFMzNUYzYsCxZAclNk3ZA4lkRvZAPVudxZAxOEZCUKZBhZAUbw8m8PzEqyXfNZAp5gVVGggUEDeR9ChYfYM2nw6zkUSql1WOgmvBzZBTREZD",
				"Hi Check Out this cool app!",
				100003785121083L);
	}
	
	public boolean postOnUserWall(String accessToken, String message, long userFacebookId) {
		String request = "https://graph.facebook.com/" + userFacebookId + "/feed";
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(request);

		boolean ok = false;
		try {
		    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		    nameValuePairs.add(new BasicNameValuePair("access_token", accessToken));
		    nameValuePairs.add(new BasicNameValuePair("message", message));
		    nameValuePairs.add(new BasicNameValuePair("picture", Configuration.getConfiguration().getAppImageUrl()));
		    nameValuePairs.add(new BasicNameValuePair("link", Configuration.getConfiguration().getAppleAppStoreUrl()));
		    nameValuePairs.add(new BasicNameValuePair("name", "Snappers for iPhone app"));
		    nameValuePairs.add(new BasicNameValuePair("caption", "Download \"Snappers\" from Apple App Store"));
		    nameValuePairs.add(new BasicNameValuePair("description", "Snappers is a new addictive puzzle game for iPhone and iPad."));

		    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

		    // Create a response handler
			ResponseHandler<String> responseHandler = new BasicResponseHandler();
			String responseBody = httpClient.execute(httpPost, responseHandler);
			System.out.println("response: "+ responseBody);
			JSONObject obj = new JSONObject(responseBody);
			ok = obj.has("id");
		} catch (Exception e) {
			System.out.println("exception " + e);
			// do nothing
			log.error("failed to post on user wall " + accessToken);
		}
		finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			httpClient.getConnectionManager().shutdown();
		}


		return ok;
	}
	
	public long getFacebookIdForCode(String code) {
		int base = 58;
		String baseDigits = "abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ123456789";
		long result = 0; 
		for (int i = 0; i < code.length(); i++) {
			result *= base;
			char c = code.charAt(i);
			int digit = baseDigits.indexOf(c);
			result += digit;
		}
		return result;
	}
	
	public String getCodeForFacebookId(long facebookId) {
		int base = 58;
		String baseDigits = "abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ123456789";
		String result = "";
		while (facebookId != 0) {
			int index = (int)( facebookId % base);
			result = baseDigits.charAt(index) + result;
			facebookId = facebookId / base;
		}
		return result;
	}
	
	@Test
	public void test_code() {
	/*	java.util.Random random = new java.util.Random();
		for (int i = 0; i < 100000; i++) {
			long value = Math.abs(random.nextLong());
			String code = getCodeForFacebookId(value);
			long value2 = getFacebookIdForCode(code);
			if (value != value2) {
				System.out.println("value " + value + "\tcode " + code + "\tdecoded " + value2);
				System.out.println("********  PIZDEC!!! *****");
			}
		}*/
		
		System.out.println(getCodeForFacebookId(100001737369611L));
		//System.out.println(getFacebookIdForCode(getCodeForFacebookId(100001737369611L)));
	}
}
