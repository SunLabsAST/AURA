/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.admin.server;

import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.util.props.ConfigurationManager;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
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
            URL config = context.getResource("/sitmAdminConfig.xml");
            logger.info("Config URL is " + config);

            //
            // Get the Aardvark interface
            try {
                logger.info("new ConfigManager");
                ConfigurationManager cm = new ConfigurationManager();
                logger.info("addProps");

                cm.addProperties(config);
                logger.info("addProps OK");

                logger.info("setAttr");
                context.setAttribute("configManager", cm);

                try {
                    MusicDatabase mdb = new MusicDatabase(cm);
                    List<Artist> artists = mdb.artistGetMostPopular(1);
                    if (artists.size() > 0) {
                        logger.info("Most popular artist is " + artists.get(0).getName());
                    }
                    context.setAttribute("MusicDatabase", mdb);
                } catch (AuraException ex) {
                    logger.severe("AuraException : "+ex.getMessage());
                }
            } catch (IOException ioe) {
                logger.log(Level.SEVERE, "Failed to get handle", ioe);
            }
        } catch (MalformedURLException ex) {
            logger.severe("Bad URL to config file " + ex.getMessage());
        }
    }

    public void contextDestroyed(ServletContextEvent arg0) {   
    }
}
