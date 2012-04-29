package com.emerginggames.snappersbackend;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jetty.server.Server;

public class SnappersServer {
	private static Logger log = Logger.getLogger(SnappersServer.class);
	
    public static void main(String[] args) throws Exception
    {
    	PropertyConfigurator.configure("config/log4j.properties");
        Server server = new Server(8080);
        log.debug("server configuration " + Configuration.getConfiguration().getJson().toString());
        server.setHandler(new SnappersServerHandler());
        server.start();
        server.join();
    }
}
