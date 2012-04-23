package com.emerginggames.snappersbackend;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
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
				msg = new SyncOkMessage(null);
			} else {
				if (dbPlayer.getXpCount() > player.getXpCount()) {
					player.setXpCount(dbPlayer.getXpCount());
					player.setHintCount(dbPlayer.getHintCount());
					player.setUserDefaults(dbPlayer.getUserDefaults());
					msg = new SyncOkMessage(player.toJSON());
				} else {
					log.debug("updating player " + player.getFacebookId());
					msg = new SyncOkMessage(null);
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
        else if (baseRequest.getMethod().equals("GET") && methodName.equalsIgnoreCase("promo"))
        	promo(request, response);
        else
        	error(response, "unknown HTTP method");
    }
}