package com.emerginggames.snappersbackend;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.emerginggames.snappersbackend.db.Dao;

public class SnappersServerHandler extends AbstractHandler
{
	private Logger log = Logger.getLogger(SnappersServerHandler.class);
	
	public void sync(HttpServletRequest request,HttpServletResponse response) throws IOException {
		StringBuffer jb = new StringBuffer();
		String line = null;
		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null)
				jb.append(line);
		} catch (Exception e) { 
			error(response, "Error parsing request data");
			return;
		}

		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(jb.toString());
			log.debug("jsonObject = " + jsonObject);
		} catch (JSONException e) {
			error(response, "Error parsing JSON request string");
			throw new IOException("Error parsing JSON request string");
		}
		
		Player player = Player.parseFromJSON(jsonObject);
		if (player == null) {
			error(response, "Invalid JSON request parameters");
			return;
		}

		SyncOkMessage msg;
		try {

			Dao dao = new Dao();
			Player dbPlayer = dao.loadPlayer(player.getFacebookId());
			if (dbPlayer == null) {
				log.debug("inserting player " + player.getFacebookId());
				dao.insertPlayer(player);
				msg = new SyncOkMessage(player.toJSON());
			} else {
				boolean needsUpdate = false;
				
				if (dbPlayer.getGifts() != null) {
					player.setGifts(dbPlayer.getGifts());
					needsUpdate = true;
				}
				
				if (dbPlayer.getXpCount() > player.getXpCount()) {
					player.setXpCount(dbPlayer.getXpCount());
					player.setHintCount(dbPlayer.getHintCount());
					player.setUserDefaults(dbPlayer.getUserDefaults());
					player.setDollarsSpent(dbPlayer.getDollarsSpent());
				} else {
					needsUpdate = true;
				}
				
				msg = new SyncOkMessage(player.toJSON());

				if (needsUpdate) {
					log.debug("updating player " + player.getFacebookId());
					dao.updatePlayer(player);
				}
			}
			dao.close();
		} catch (SQLException e) {
			log.error("SQLException: " + e);
			error(response, "database error");
			return;
		}
		response.getWriter().println(msg);
	}
	
	public void friends(HttpServletRequest request,HttpServletResponse response) throws IOException {
		String accessToken = request.getParameter("access_token");
		if (accessToken == null) {
			error(response, "invalid access token");
			return;
		}
		
		ArrayList<Friend> friends = new ArrayList<Friend>();
		ArrayList<Friend> playingFriends = new ArrayList<Friend>();
		JSONArray friendsJson = FacebookController.getFacebookController().getFacebookFriends(accessToken);
		
		for (int i = 0; i < friendsJson.length(); i++) {
			try {
				JSONObject json = friendsJson.getJSONObject(i);
				Friend friend = Friend.parseFromJSON(json);
				if (friend == null) {
					error(response, "Error parsing JSON from facebook");
					return;
				}
				friends.add(friend);
				if (friend.isInstalled())
					playingFriends.add(friend);
			} catch (JSONException e) {
				error(response, "Error parsing JSON from facebook");
				return;
			}
		}
		
		if (playingFriends.size() > 0) {
			try {
				Dao dao = new Dao();
				dao.getXpOfFriends(playingFriends);
				dao.close();
			} catch (SQLException e) {
				log.error("dao error" + e);
			}
		}
		
		Collections.sort(friends, Collections.reverseOrder());
		FriendsOkMessage msg = new FriendsOkMessage(friends);
		response.getWriter().println(msg);
	}
	
	public void gift(HttpServletRequest request,HttpServletResponse response) throws IOException {
		String accessToken = request.getParameter("access_token");
		if (accessToken == null) {
			error(response, "invalid access token");
			return;
		}

		long giftTo = 0;
		if (request.getParameter("gift_to") == null) {
			error(response, "invalid gift_to parameter");
			return;
		} else
			giftTo = Long.parseLong(request.getParameter("gift_to"));
		
		long giftFrom = FacebookController.getFacebookController().getFacebookId(accessToken);
		if (giftFrom == 0) {
			error(response, "invalid gift_from parameter");
			return;
		}
		
		if (giftFrom == giftTo) {
			error(response, "invalid gift_to parameter");
			return;
		}
		
		try {
			Dao dao = new Dao();
			if (dao.updateGift(giftFrom, giftTo)) {
				response.getWriter().println(new GiftOkMessage());
			} else {
				log.error("can't update gift");
				error(response, "unexpected error");
			}
			dao.close();
		} catch (SQLException e) {
			log.error("can't update gift");
			error(response, "unexpected error");
		}
	}

	public void invite(HttpServletRequest request,HttpServletResponse response) throws IOException {
		StringBuffer jb = new StringBuffer();
		String line = null;
		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null)
				jb.append(line);
		} catch (Exception e) { 
			error(response, "Error parsing request data");
			return;
		}

		JSONObject jsonObject;
		String accessToken;
		String message;
		long inviteTo = 0;
		try {
			jsonObject = new JSONObject(jb.toString());
			accessToken = jsonObject.getString("access_token");
			if (accessToken == null) {
				error(response, "invalid access token");
				return;
			}
			
			message = jsonObject.getString("message");
			log.debug("message: " + message);
			if (message == null) {
				error(response, "invalid message");
				return;
			}

			inviteTo = jsonObject.getLong("invite_to");

			
		} catch (JSONException e) {
			error(response, "Error parsing JSON request string");
			throw new IOException("Error parsing JSON request string");
		}
		
		long inviteFrom = FacebookController.getFacebookController().getFacebookId(accessToken);
		if (inviteFrom == 0) {
			error(response, "invalid invite_from parameter");
			return;
		}
		
		boolean ok = true;
		if (Configuration.getConfiguration().isPostingToFacebookAllowed()) {
			ok = FacebookController.getFacebookController().postOnUserWall(accessToken, message, inviteTo);
		} else
			log.info("posting to facebook not allowed");
		
		if (ok) {
			try {
				Dao dao = new Dao();
				ok = dao.updateInvite(inviteFrom);
				dao.close();
			} catch (SQLException e) {
				log.error("sql exception: " + e);
				ok = false;
				return;
			}
		}
	
		if (ok)
			response.getWriter().println(new InviteOkMessage());
		else
			error(response, "unable to send invite");
	}
	
	public void share(HttpServletRequest request,HttpServletResponse response) throws IOException {
		StringBuffer jb = new StringBuffer();
		String line = null;
		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null)
				jb.append(line);
		} catch (Exception e) { 
			error(response, "Error parsing request data");
			return;
		}

		JSONObject jsonObject;
		String accessToken;
		String message;

		try {
			jsonObject = new JSONObject(jb.toString());
			accessToken = jsonObject.getString("access_token");
			if (accessToken == null) {
				error(response, "invalid access token");
				return;
			}
			
			message = jsonObject.getString("message");
			log.debug("message: " + message);
			if (message == null) {
				error(response, "invalid message");
				return;
			}
		} catch (JSONException e) {
			error(response, "Error parsing JSON request string");
			throw new IOException("Error parsing JSON request string");
		}
		
		long myFacebookId = FacebookController.getFacebookController().getFacebookId(accessToken);
		if (myFacebookId == 0) {
			error(response, "invalid access token");
			return;
		}

		boolean ok = true;
		if (Configuration.getConfiguration().isPostingToFacebookAllowed()) {
			ok = FacebookController.getFacebookController().postOnUserWall(accessToken, message, myFacebookId);
		} else
			log.info("posting to facebook not allowed");
		
		if (ok) {
			try {
				Dao dao = new Dao();
				ok = dao.updateShare(myFacebookId);
				dao.close();
			} catch (SQLException e) {
				log.error("sql exception: " + e);
				ok = false;
				return;
			}
		}
	
		if (ok)
			response.getWriter().println(new ShareOkMessage());
		else
			error(response, "unable to share");
	}

	public void promo(HttpServletRequest request,HttpServletResponse response) throws IOException {
		
	}

	public void error(HttpServletResponse response, String description) throws IOException {
		ErrorMessage msg = new ErrorMessage(description);
		response.getWriter().println(msg);
	}

	public void handle(String target,Request baseRequest,HttpServletRequest request,HttpServletResponse response) 
        throws IOException, ServletException
    {
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);
        String methodName = request.getRequestURI().substring(1);
        if (baseRequest.getMethod().equals("POST") && methodName.equalsIgnoreCase("sync"))
        	sync(request, response);
        else if (baseRequest.getMethod().equals("GET") && methodName.equalsIgnoreCase("friends"))
        	friends(request,response);
        else if (baseRequest.getMethod().equals("GET") && methodName.equalsIgnoreCase("gift"))
        	gift(request,response);
        else if (baseRequest.getMethod().equals("POST") && methodName.equalsIgnoreCase("invite"))
        	invite(request, response);
        else if (baseRequest.getMethod().equals("POST") && methodName.equalsIgnoreCase("share"))
        	share(request, response);
        else if (baseRequest.getMethod().equals("GET") && methodName.equalsIgnoreCase("promo"))
        	promo(request, response);
        else
        	error(response, "unknown HTTP method");
    }
}