/*
 *   Copyright 2008 Sun Microsystems, Inc. All rights reserved
 *   Use is subject to license terms.
 */
package com.sun.labs.aura;

import com.sun.labs.util.SimpleLabsLogFormatter;
import com.sun.labs.util.props.ConfigBoolean;
import com.sun.labs.util.props.ConfigComponentList;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.ConfigurationManager;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class that will start and stop an aardvark service.  The configuration for
 * this class will point to a configuration that can be used to create 
 * and start the service.
 */
public class AuraServiceStarter implements Configurable {

    private List<AuraService> services;

    private boolean blockForShutdown;

    private ConfigurationManager cm;

    public void stopServices() {
        //
        // If the services were registered with a service registry, then we need
        // to unregister them.
        cm.shutdown();
        for(AuraService s : services) {
            s.stop();
        }
    }
    /**
     * A configuration property for the services that we will be starting and
     * stopping.
     */
    @ConfigComponentList(type = com.sun.labs.aura.AuraService.class)
    public static final String PROP_SERVICE_COMPONENTS = "serviceComponents";

    /**
     * A configuration property indicating whether we should wait to be killed
     * to shutdown the services
     */
    @ConfigBoolean(defaultValue = true)
    public static final String PROP_BLOCK_FOR_SHUTDOWN = "blockForShutdown";

    public void newProperties(PropertySheet ps) throws PropertyException {

        cm = ps.getConfigurationManager();

        blockForShutdown = ps.getBoolean(PROP_BLOCK_FOR_SHUTDOWN);

        //
        // Get the names of the components we're to start, then start them.
        services =
                (List<AuraService>) ps.getComponentList(PROP_SERVICE_COMPONENTS);
        for(AuraService service : services) {
            service.start();
        }



        //
        // Add a shutdown hook to stop the services.
        Runtime.getRuntime().addShutdownHook(new Thread() {

            public void run() {
                stopServices();
            }
        });

    }

    public static void usage() {
        System.err.println(
                "Usage: com.sun.labs.aura.AuraServiceStarter <config> <component name>");
        System.err.println(
                "  Some useful global properties are auraHome and auraDistDir");
        System.err.println("  auraHome defaults to /aura.");
        System.err.println(
                "  auraDistDir defaults to the current working directory");
    }

    /**
     * A main program to read the configuration for the service starter and
     * start the service.
     * @param args
     */
    public static void main(String[] args) {
        if(args.length < 2) {
            usage();
            return;
        }

        //
        // Use the labs format logging.
        Logger rl = Logger.getLogger("");
        for(Handler h : rl.getHandlers()) {
            h.setLevel(Level.ALL);
            h.setFormatter(new SimpleLabsLogFormatter());
        }

        AuraServiceStarter starter = null;
        String configFile = args[0];
        try {
            //
            // See if we can get a resource for the configuration file first.
            // This is mostly a convenience.
            URL cu = AuraServiceStarter.class.getResource(configFile);
            if(cu == null) {
                cu = (new File(configFile)).toURI().toURL();
            }
            ConfigurationManager cm = new ConfigurationManager(cu);
            starter = (AuraServiceStarter) cm.lookup(args[1]);
            
            if(starter == null) {
                System.err.println("Unknown starter: " + args[1]);
            }
            
            //
            // Block until we're killed if we're supposed to.
            if(starter.blockForShutdown) {
                Thread.sleep(Long.MAX_VALUE);
            }
        } catch(IOException ex) {
            System.err.println("Error parsing configuration file: " + configFile);
            ex.printStackTrace();
            usage();
        } catch(PropertyException ex) {
            System.err.println("Error parsing configuration file: " + configFile);
            ex.printStackTrace();
            usage();
        } catch(InterruptedException ie) {
        } catch(Exception e) {
            System.err.println("Other error: " + e);
            e.printStackTrace();
            usage();
        } finally {
            if(starter != null) {
                starter.stopServices();
            }
        }
    }
}
