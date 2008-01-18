/*
 *   Copyright 2008 Sun Microsystems, Inc. All rights reserved
 *   Use is subject to license terms.
 */
package com.sun.labs.aura.aardvark;

import com.sun.labs.util.LabsLogFormatter;
import com.sun.labs.util.props.Component;
import com.sun.labs.util.props.ComponentRegistry;
import com.sun.labs.util.props.ConfigComponentList;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.ConfigurationManager;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * A class that will start and stop an aardvark service.  The configuration for
 * this class will point to a configuration that can be used to create 
 * and start the service.
 */
public class AardvarkServiceStarter implements Configurable {

    private List<AardvarkService> services;

    private ConfigurationManager cm;

    public void stopServices() {
        //
        // If the services were registered with a service registry, then we need
        // to unregister them.
        ComponentRegistry cr = cm.getComponentRegistry();
        if(cr != null) {
            cr.unregister();
        }
        for(AardvarkService s : services) {
            s.stop();
        }
    }
    /**
     * A configuration property for the component to load to initialize the
     * service.
     */
    @ConfigComponentList(type = com.sun.labs.aura.aardvark.AardvarkService.class)
    public static final String PROP_SERVICE_COMPONENTS = "serviceComponents";

    public void newProperties(PropertySheet ps) throws PropertyException {

        cm = ps.getPropertyManager();

        //
        // Get the names of the components we're to start, then start them.
        services =
                (List<AardvarkService>) ps.getComponentList(PROP_SERVICE_COMPONENTS);
        for(AardvarkService service : services) {
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
        System.err.println("Usage: com.sun.labs.aura.aardvark.AardvarkServiceStarter <config> <component name>");
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
            h.setFormatter(new LabsLogFormatter());
        }

        try {
            ConfigurationManager cm =
                    new ConfigurationManager((new File(args[0])).toURI().toURL());
            AardvarkServiceStarter starter =
                    (AardvarkServiceStarter) cm.lookup(args[1]);

            //
            // Sleep until we're killed.
            Thread.sleep(Long.MAX_VALUE);
        } catch(IOException ex) {
            System.err.println("Error parsing configuration file: " + ex);
            usage();
        } catch(PropertyException ex) {
            System.err.println("Error parsing configuration file: " + ex);
            usage();
        } catch(InterruptedException ie) {
        }
    }
}
