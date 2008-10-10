/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.util.props.Component;
import com.sun.labs.util.props.ComponentListener;
import com.sun.labs.util.props.ConfigurationManager;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 *
 */
public class DatabaseBroker implements ServletContextListener, ComponentListener {

    private final static String ATTRIBUTE_NAME = "databaseBroker";
    protected Logger logger = Logger.getLogger("");
    private ConfigurationManager cm = null;
    private MusicDatabase mdb = null;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.warning("Servlet DatabaseBroker is here!");
        try {
            ServletContext context = sce.getServletContext();
            URL config = context.getResource("/sitmWebConfig.xml");
            try {
                cm = new ConfigurationManager();
                cm.addProperties(config);
                context.setAttribute(ATTRIBUTE_NAME, this);
            } catch (IOException ioe) {
                logger.log(Level.SEVERE, "Can't load configuration " + config, ioe);
            }
        } catch (MalformedURLException ex) {
            logger.severe("Bad URL to config file " + ex.getMessage());
        }
    }

    private MusicDatabase getMusicDatabase() {
        if (mdb == null) {
            DataStore store = (DataStore) cm.lookup("dataStoreHead", this);
            if (store != null) {
                try {
                    logger.info("Servlet connecting to datastore " + store);
                    mdb = new MusicDatabase(store);
                } catch (AuraException ex) {
                    logger.log(Level.SEVERE, "Servlet failed to connect to the datastore", ex);
                }
            } else {
                logger.severe("No datastores available");
            }
        }
        return mdb;
    }

    public static MusicDatabase getMusicDatabase(ServletContext sc) {
        DatabaseBroker sl = (DatabaseBroker) sc.getAttribute(ATTRIBUTE_NAME);
        if (sl != null) {
            return sl.getMusicDatabase();
        }
        return null;
    }

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        // TBD remove the lookup listener
    }

    @Override
    public void componentAdded(Component c) {
    }

    @Override
    public void componentRemoved(Component c) {
        if (mdb != null) {
            if (c instanceof DataStore) {
                DataStore store = (DataStore) c;
                if (mdb.getDataStore() == store) {
                    mdb = null;
                }
            }
        }
    }
}