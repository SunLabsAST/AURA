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

package com.sun.labs.aura.util;

import com.sun.labs.aura.service.StatService;
import com.sun.labs.aura.AuraService;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.util.command.CommandInterpreter;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.util.logging.Logger;

/**
 * A simple wrapper that starts up a shell with the aura commands
 */
public class AuraShell implements AuraService, Configurable {
    @ConfigComponent(type = DataStore.class)
    public final static String PROP_DATA_STORE = "dataStoreHead";

    @ConfigComponent(type = StatService.class)
    public final static String PROP_STAT_SERVICE = "statService";

    private DataStore dataStore;
    private CommandInterpreter shell;    
    @SuppressWarnings(value="URF_UNREAD_FIELD")
    private Logger logger;
    @SuppressWarnings(value="URF_UNREAD_FIELD")
    private ShellUtils sutils;
    private StatService statService;

    
    public void start() {
        shell = new CommandInterpreter();
        shell.setPrompt("aura% ");
        sutils = new ShellUtils(shell, dataStore, statService);
        Thread t = new Thread() {

            public void run() {
                shell.run();
                shell = null;
            }
        };
        t.start();
    }
    
    public void stop() {
        if (shell != null) {
            shell.close();
        }
    }
    
    /**
     * Reconfigures this component
     * @param ps the property sheet
     * @throws com.sun.labs.util.props.PropertyException
     */
    public void newProperties(PropertySheet ps) throws PropertyException {
        dataStore = (DataStore) ps.getComponent(PROP_DATA_STORE);
        statService = (StatService) ps.getComponent(PROP_STAT_SERVICE);
        logger = ps.getLogger();
    }

}
