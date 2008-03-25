/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.util;

import com.sun.labs.util.SimpleLabsLogFormatter;
import com.sun.labs.util.props.ComponentRegistry;
import com.sun.labs.util.props.ConfigurationManager;
import java.io.File;
import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 *
 * @author stgreen
 */
public class DumpServices {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        //
        // Use the labs format logging.
        Logger logger = Logger.getLogger("");
        for(Handler h : logger.getHandlers()) {
            h.setFormatter(new SimpleLabsLogFormatter());
        }

        ConfigurationManager cm = new ConfigurationManager((new File(args[0])).toURI().toURL());
        ComponentRegistry cr = cm.getComponentRegistry();
        
        logger.info("Sleeping");
        Thread.sleep(20000);
        if(cr == null) {
            logger.info("No component registry");
            return;
        }
        cr.dumpJiniServices();
        // TODO code application logic here
    }

}
