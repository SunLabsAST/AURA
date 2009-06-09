/*
 * Copyright 2007-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
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
        DataManager dm = (DataManager) sce.getServletContext().getAttribute("DataManager");
        if (dm!=null) {
            dm.onDestroy();
        }
    }
}
