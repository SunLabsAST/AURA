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

package com.sun.labs.aura.grid.util;

import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.impl.Replicant;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 */
public class RMISerialTest extends RMIParallelTest {

    //
    // Set up the data.
    public RMISerialTest() {
        super();
    }

    @Override
    public String serviceName() {
        return "RMITest";
    }

    protected List<Scored<Item>> runGet() {
        List<Scored<Item>> ret = new ArrayList<Scored<Item>>();
        getTime.start();
        List<Getter> callers = new ArrayList();
        for(Map.Entry<Replicant, List<Scored<String>>> e : repMap.entrySet()) {
            try {
                Getter g = new Getter(e.getKey(), e.getValue());
                g.call();
                callers.add(g);
            } catch(Exception ex) {
                logger.log(Level.SEVERE, "Error getting", ex);
            }
        }

        getTime.stop();
        logger.info(String.format(" get took %.3f",
                getTime.getLastTimeMillis()));
        double max = 0;
        double maxRep = 0;
        double maxOH = 0;
        StringBuilder sb = new StringBuilder();
        for(Getter g : callers) {
            sb.append(String.format(" %s took %.3f oh %.3f",
                    g.prefix, g.nw.getTimeMillis(), g.overhead));
            max = Math.max(max, g.nw.getTimeMillis());
            maxRep = Math.max(maxRep, g.repTime);
            maxOH = Math.max(maxOH, g.overhead);
        }
        logger.info(sb.toString());
        logger.info(String.format(" max: %.3f maxRep: %.3f maxOH: %.3f %.3f",
                max, maxRep, maxOH, getTime.getLastTimeMillis() - maxRep));
        return ret;
    }

    protected List<Scored<String>> runFindSimilar() {
        fsTime.start();
        List<Scored<String>> ret = new ArrayList();
        List<FindSimilarer> callers = new ArrayList();
        for(Replicant r : repMap.keySet()) {
            try {
            FindSimilarer fs = new FindSimilarer(r, dv);
            ret.addAll(fs.call());
            callers.add(fs);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Error running fs", ex);
            }

        }
        fsTime.stop();
        logger.info(String.format(" fs took %.3f",
                fsTime.getLastTimeMillis()));
        double max = 0;
        double maxRep = 0;
        double maxOH = 0;
        StringBuilder sb = new StringBuilder();
        for(FindSimilarer fs : callers) {
            sb.append(String.format(" %s took %.3f oh %.3f",
                    fs.prefix, fs.nw.getTimeMillis(), fs.overhead));
            max = Math.max(max, fs.nw.getTimeMillis());
            maxRep = Math.max(maxRep, fs.repTime);
            maxOH = Math.max(maxOH, fs.overhead);
        }
        logger.info(sb.toString());
        logger.info(String.format(" max: %.3f maxRep: %.3f maxOH: %.3f %.3f",
                max, maxRep, maxOH, fsTime.getLastTimeMillis() - maxRep));
        return ret;
    }

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
    }
}
