/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.wsitm.server;

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
 */
public class ServletListener implements ServletContextListener {

    protected Logger logger = Logger.getLogger("");

    public void contextInitialized(ServletContextEvent sce) {
        try {
            ServletContext context = sce.getServletContext();
            URL config = context.getResource("/webMusicExplauraWebConfig.xml");
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
                    context.setAttribute("MusicDatabase", new MusicDatabase(cm));
                } catch (AuraException ex) {
                    logger.severe("AuraException : " + ex.getMessage());
                }
            } catch (IOException ioe) {
                logger.log(Level.SEVERE, "Failed to get handle", ioe);
            }
        } catch (MalformedURLException ex) {
            logger.severe("Bad URL to config file " + ex.getMessage());
        }
    }

    public void contextDestroyed(ServletContextEvent sce) {
        ConfigurationManager cm = (ConfigurationManager) sce.getServletContext().
                getAttribute("configManager");
        if (cm != null) {
            cm.shutdown();
        }
    }
}
