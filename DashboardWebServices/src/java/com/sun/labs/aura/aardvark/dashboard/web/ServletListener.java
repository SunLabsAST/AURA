/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.aardvark.dashboard.web;

import com.sun.labs.aura.aardvark.Aardvark;
import com.sun.labs.aura.datastore.DataStore;
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

            URL config = context.getResource("/dashboardWebConfig.xml");
            logger.info("Config URL is " + config);

            //
            // Get the Aardvark interface
            try {
                ConfigurationManager cm = new ConfigurationManager();
                cm.addProperties(config);
                context.setAttribute("configManager", cm);

                Aardvark aardvark = (Aardvark) cm.lookup("aardvark");
                context.setAttribute("aardvark", aardvark);
                DataStore dataStore = (DataStore)cm.lookup("dataStore");
                context.setAttribute("dataStore", dataStore);

            } catch (IOException ioe) {
                logger.log(Level.SEVERE, "Failed to get Aardvark handle", ioe);
            }
        } catch (MalformedURLException ex) {
            logger.severe("Bad URL to config file " + ex.getMessage());
        }
    }

    public void contextDestroyed(ServletContextEvent arg0) {

    }
}
