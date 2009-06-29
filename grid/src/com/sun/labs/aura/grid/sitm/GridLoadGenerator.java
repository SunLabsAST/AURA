/*
 * Copyright 2005-2009 Sun Microsystems, Inc. All Rights Reserved.
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

import com.sun.labs.aura.grid.ServiceAdapter;
import com.sun.labs.aura.music.test.LoadGenerator;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.PropertySheet;
import java.io.IOException;
import java.util.logging.Level;

/**
 * A service that wraps the LoadGenerator for easy deploying onto the grid
 */
public class GridLoadGenerator extends ServiceAdapter {

    @ConfigString(defaultValue="2000")
    public final static String PROP_USERS = "numUsers";
    protected String numUsers;

    @ConfigString(defaultValue="100")
    public final static String PROP_THREADS = "numThreads";
    protected String numThreads;

    @ConfigString(defaultValue="300")
    public final static String PROP_TIME = "time";
    protected String time;

    @Override
    public String serviceName() {
        return "LoadGen";
    }

    @Override
    public void start() {
        //
        // Instantiate and run a load test
        String[] args = {
            "-users",
            numUsers,
            "-threads",
            numThreads,
            "-time",
            time,
            "-comm",
            "rmi",
            "-summary"
        };
        try {
            LoadGenerator.main(args);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Loadtest failed", e);
        }
    }

    @Override
    public void stop() {
        
    }

    @Override
    public void newProperties(PropertySheet ps) {
        super.newProperties(ps);
        numUsers = ps.getString(PROP_USERS);
        numThreads = ps.getString(PROP_THREADS);
        time = ps.getString(PROP_TIME);
    }

}
