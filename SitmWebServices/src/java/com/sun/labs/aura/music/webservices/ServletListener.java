/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.util.props.ConfigurationManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
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

            URL config = context.getResource("/sitmWebConfig.xml");
            logger.info("Config URL is " + config);


            //
            // Get the Aardvark interface
            if (true) {
                try {
                    logger.info("new ConfigManager");
                    ConfigurationManager cm = new ConfigurationManager();
                    logger.info("addProps");
                    cm.addProperties(config);
                    if (false) {
                        throw new IOException();
                    }
                    logger.info("setAttr");
                    context.setAttribute("configManager", cm);

                    for (String name : cm.getComponentNames()) {
                        logger.info("available: " + name);
                    }

                    logger.info("lookupDataStore");
                    DataStore dataStore = (DataStore) cm.lookup("dataStoreHead");
                    logger.info("dataStore is " + dataStore);
                    context.setAttribute("dataStore", dataStore);
                } catch (IOException ioe) {
                    logger.info("ioe " + ioe);
                    logger.log(Level.SEVERE, "Failed to get datastore handle", ioe);
                }
            }
        } catch (MalformedURLException ex) {
            logger.severe("Bad URL to config file " + ex.getMessage());
        }
    }

    public void contextDestroyed(ServletContextEvent arg0) {
    }
}
