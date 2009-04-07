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

package com.sun.labs.aura.grid.sitm;

import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.grid.ServiceAdapter;
import com.sun.labs.aura.music.sample.LastFMLoader;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.rmi.RemoteException;
import java.util.Timer;


/**
 *
 * @author Will Holcomb <william.holcomb@sun.com>
 */
public class LastFMTest extends ServiceAdapter {
    @ConfigComponent(type = com.sun.labs.aura.datastore.DataStore.class)
    public static final String PROP_DATA_STORE = "dataStore";
    
    @ConfigString(defaultValue = "")
    public final static String PROP_INPUT = "inputFile";

    String inFilename;
    private DataStore dataStore;
    LastFMLoader loader = new LastFMLoader();

    public String serviceName() {
        return "LastFMTest";
    }

    /**
     * Configre the test
     */
    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        dataStore = (DataStore)ps.getComponent(PROP_DATA_STORE);
        inFilename = ps.getString(PROP_INPUT);
    }

    public void start() {
        logger.info("DataStore: " + dataStore);
        loader.setLogger(logger);
        
        Reader input = null;
        Timer loggerTimer = null;
        
        try {
            input = new FileReader(inFilename);

            loggerTimer = new Timer();
            loggerTimer.scheduleAtFixedRate(loader.getLoggerTimerTask(), 0, 3000);
            loader.loadLastFMUsers(input, dataStore, "<sep>");
        } catch (RemoteException ex) {
            logger.severe(ex.getMessage());
        } catch (FileNotFoundException ex) {
            logger.severe("Could not find file: " + inFilename);
        } catch (IOException ex) {
            logger.severe("Could not close: " + ex);
        } catch (AuraException ex) {
            logger.severe("Aura Error: " + ex);
        } finally {
            if(input != null) {
                try {
                    input.close();
                } catch (IOException ex) {
                    logger.severe("Could not close: " + ex);
                }
            }
            loggerTimer.cancel();    
        }
    }

    public void stop() {
    }
}
