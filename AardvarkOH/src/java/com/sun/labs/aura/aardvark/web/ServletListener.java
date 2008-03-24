/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.web;

import com.sun.labs.aura.aardvark.Aardvark;
import com.sun.labs.aura.aardvark.impl.AardvarkImpl;
import com.sun.labs.util.props.ConfigurationManager;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 *
 */
public class ServletListener implements ServletContextListener {

    protected Logger logger = Logger.getLogger("");
    
    public void contextInitialized(ServletContextEvent sce) {
        try {
            ServletContext context = sce.getServletContext();
            URL config = context.getResource("/aardvarkWebConfig.xml");
            logger.info("URL is " + config);
            //
            // Get the Aardvark interface
            Aardvark aardvark = getAardvark(config);
            logger.info("Got aardvark at " + aardvark);
            context.setAttribute("aardvark", aardvark);
        } catch (MalformedURLException ex) {
            logger.severe("Bad URL to config file " + ex.getMessage());
        }
    }

    public void contextDestroyed(ServletContextEvent arg0) {
        
    }

    protected Aardvark getAardvark(URL config) {
        try {
            ConfigurationManager cm = new ConfigurationManager();
            //URL configFile = new File("aardvarkWebConfig.xml").toURI().toURL();
                    //AardvarkImpl.class.getResource("aardvarkWebConfig.xml");
            cm.addProperties(config);
            return (Aardvark)cm.lookup("aardvark");
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, "Failed to get Aardvark handle", ioe);
            return null;
        }
    }
}
