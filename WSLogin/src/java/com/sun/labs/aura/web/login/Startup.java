
package com.sun.labs.aura.web.login;

import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.service.LoginService;
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
 * Class that runs at deploy time as a servlet listener - gets hooks into
 * the aura services necessary for doing login
 */
public class Startup implements ServletContextListener {
    protected Logger logger = Logger.getLogger("");

    public void contextInitialized(ServletContextEvent sce) {
        try {
            ServletContext context = sce.getServletContext();
            URL config = context.getResource("/auraConfig.xml");
            logger.info("Config URL is " + config);

            //
            // Get the datastore and login interfaces
            try {
                ConfigurationManager cm = new ConfigurationManager();
                cm.addProperties(config);
                context.setAttribute("configManager", cm);

                DataStore dataStore = (DataStore)cm.lookup("dataStore");
                logger.info("Got data store!");
                context.setAttribute("dataStore", dataStore);

                LoginService loginSvc = (LoginService)cm.lookup("loginService");
                logger.info("Got login service!");
                context.setAttribute("loginService", loginSvc);
            } catch (IOException ioe) {
                logger.log(Level.SEVERE, "Failed to get Aura handles", ioe);
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
