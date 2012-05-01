package com.emerginggames.snappersbackend;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

public class SnappersServer {
	private static Logger log = Logger.getLogger(SnappersServer.class);
	
    public static void main(String[] args) throws Exception
    {
    	PropertyConfigurator.configure("config/log4j.properties");
        Server server = new Server();
        log.debug("server configuration " + Configuration.getConfiguration().getJson().toString());
        
        SslSelectChannelConnector ssl_connector = new SslSelectChannelConnector();
        ssl_connector.setPort(8080);
        ssl_connector.setThreadPool(new QueuedThreadPool(20));
        SslContextFactory cf = ssl_connector.getSslContextFactory();
        cf.setKeyStorePath("config/keystore");
        cf.setKeyStorePassword("OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4");
        cf.setKeyManagerPassword("OBF:1u2u1wml1z7s1z7a1wnl1u2g");
        server.setConnectors(new Connector[]
        		          { ssl_connector });
        server.setHandler(new SnappersServerHandler());
        server.start();
        server.join();
    }
}
