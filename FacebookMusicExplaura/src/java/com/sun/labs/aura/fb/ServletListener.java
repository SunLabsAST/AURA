/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.fb;

import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.util.AuraException;
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
 * @author jalex
 */
public class ServletListener implements ServletContextListener {
    protected Logger logger = Logger.getLogger("");

    public void contextInitialized(ServletContextEvent sce) {
        try {
            ServletContext context = sce.getServletContext();
            URL config = context.getResource("/fbWebConfig.xml");
            logger.info("Config URL is " + config);

            //
            // Get the datastore interface
            try {
                ConfigurationManager cm = new ConfigurationManager();
                cm.addProperties(config);
                context.setAttribute("configManager", cm);

                DataStore dataStore = (DataStore)cm.lookup("dataStore");
                logger.info("Got data store!");
                context.setAttribute("dataStore", dataStore);

                //
                // Create a Music Database wrapper
                MusicDatabase mdb = new MusicDatabase(cm);
                DataManager dm = new DataManager(mdb);
                context.setAttribute("dm", dm);
            } catch (IOException ioe) {
                logger.log(Level.SEVERE, "Failed to get DataStore handle", ioe);
            } catch (AuraException e) {
                logger.log(Level.SEVERE, "Failed to create MDB", e);
            }
        } catch (MalformedURLException ex) {
            logger.severe("Bad URL to config file " + ex.getMessage());
        }
    }


    public void contextDestroyed(ServletContextEvent sce) {
            ServletContext context = sce.getServletContext();
        ConfigurationManager cm =
                (ConfigurationManager) context.getAttribute("configManager");
        if(cm != null) {
            cm.shutdown();
        }
    }
}
