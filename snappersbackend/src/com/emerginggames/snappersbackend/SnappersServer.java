package com.emerginggames.snappersbackend;

import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jetty.server.Server;

public class SnappersServer {
    public static void main(String[] args) throws Exception
    {
    	PropertyConfigurator.configure("config/log4j.properties");
        Server server = new Server(8080);
        server.setHandler(new SnappersServerHandler());
        server.start();
        server.join();
    }
}
