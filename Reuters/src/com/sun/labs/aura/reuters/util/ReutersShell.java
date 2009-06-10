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
package com.sun.labs.aura.reuters.util;

/**
 *
 * @author plamere
 */
import com.sun.labs.aura.AuraService;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.ShellUtils;
import com.sun.labs.aura.service.StatService;
import com.sun.labs.util.command.CommandInterpreter;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.util.List;

/**
 * Manages the set of feed crawling threads
 * @author plamere
 */
public class ReutersShell implements AuraService, Configurable {

    private CommandInterpreter shell;
    private ShellUtils sutils;
    @ConfigComponent(type = com.sun.labs.aura.datastore.DataStore.class)
    public static final String PROP_DATA_STORE = "dataStore";
    private DataStore dataStore;
    @ConfigComponent(type = StatService.class)
    public final static String PROP_STAT_SERVICE = "statService";
    private StatService statService;

    /**
     * Starts crawling all of the feeds
     */
    public void start() {
        shell = new CommandInterpreter();
        shell.setPrompt("reuters% ");
        sutils = new ShellUtils(shell, dataStore, statService);
        sutils.setDisplayFields(new String[] {"_score", "aura-key", "headline"});

        Thread t = new Thread() {

            public void run() {
                shell.run();
                shell = null;
            }
        };
        t.start();
    }

    /**
     * Stops crawling the feeds
     */
    public void stop() {
        if(shell != null) {
            shell.close();
        }

    }

    /**
     * Reconfigures this component
     * @param ps the property sheet
     * @throws com.sun.labs.util.props.PropertyException
     */
    public void newProperties(PropertySheet ps) throws PropertyException {
        statService = (StatService) ps.getComponent(PROP_STAT_SERVICE);
        dataStore = (DataStore) ps.getComponent(PROP_DATA_STORE);
    }

    private String scoredListToString(List<Scored<String>> list) {
        StringBuilder sb = new StringBuilder();
        for(Scored<String> ss : list) {
            sb.append(String.format("(%s,%.3f)", ss.getItem(), ss.getScore()));
        }
        return sb.toString().trim();
    }
    /**
     * the configurable property for the itemstore used by this manager
     */
}
