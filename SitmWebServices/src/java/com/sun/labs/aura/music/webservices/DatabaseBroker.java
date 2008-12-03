/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.webservices;

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
public class DatabaseBroker implements ServletContextListener {
    private final static String CONFIG_FILE_NAME = "/sitmWebConfig.xml";
    private final static String ATTRIBUTE_NAME = "databaseBroker";
    private MusicDatabase mdb = null;
    private ItemFormatterManager ifm;
    private StatsManager statsManager;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Logger logger = Logger.getLogger("");
        try {
            ServletContext context = sce.getServletContext();
            URL config = context.getResource(CONFIG_FILE_NAME);
            try {
                ConfigurationManager cm = new ConfigurationManager();
                cm.addProperties(config);
                context.setAttribute(ATTRIBUTE_NAME, this);
                mdb = new MusicDatabase(cm);
                ifm = new ItemFormatterManager(mdb);
                statsManager = new StatsManager();
            } catch (IOException ioe) {
                logger.log(Level.SEVERE, "Can't load configuration " + config, ioe);
            }
        } catch (AuraException ex) {
            logger.severe("Can't connect to MusicDatabase " + ex.getMessage());
        } catch (MalformedURLException ex) {
            logger.severe("Bad URL to config file " + ex.getMessage());
        }
    }

    private MusicDatabase getMusicDatabase() {
        return mdb;
    }

    private ItemFormatterManager getItemFormatterManager() {
        return ifm;
    }

    private StatsManager getStatsManager() {
        return statsManager;
    }

    public static MusicDatabase getMusicDatabase(ServletContext sc) {
        DatabaseBroker sl = (DatabaseBroker) sc.getAttribute(ATTRIBUTE_NAME);
        if (sl != null) {
            return sl.getMusicDatabase();
        }
        return null;
    }

    public static DatabaseBroker getDatabaseBroker(ServletContext sc) {
        return (DatabaseBroker) sc.getAttribute(ATTRIBUTE_NAME);
    }

    public static ItemFormatterManager getItemFormatterManager(ServletContext sc) {
        DatabaseBroker sl = (DatabaseBroker) sc.getAttribute(ATTRIBUTE_NAME);
        if (sl != null) {
            return sl.getItemFormatterManager();
        }
        return null;
    }

    public static StatsManager getStatsManager(ServletContext sc) {
        DatabaseBroker databaseBroker = (DatabaseBroker) sc.getAttribute(ATTRIBUTE_NAME);
        if (databaseBroker != null) {
            return databaseBroker.getStatsManager();
        }
        return null;
    }


    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        MusicDatabase mdb = getMusicDatabase(sce.getServletContext());
        if(mdb != null) {
            mdb.shutdown();
        }
    }
}