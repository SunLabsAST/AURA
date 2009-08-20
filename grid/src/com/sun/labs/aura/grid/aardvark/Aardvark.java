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

package com.sun.labs.aura.grid.aardvark;

import com.sun.caroline.platform.ProcessConfiguration;
import com.sun.caroline.platform.ProcessRegistrationFilter;
//import com.sun.labs.aura.aardvark.impl.crawler.FeedManager;
//import com.sun.labs.aura.aardvark.impl.crawler.FeedScheduler;
import com.sun.labs.aura.grid.util.GridUtil;
import com.sun.labs.aura.grid.ServiceAdapter;
import com.sun.labs.aura.recommender.RecommenderManager;
import com.sun.labs.util.props.ConfigInteger;
import com.sun.labs.util.props.ConfigurationManager;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.util.regex.Pattern;

/**
 * A base class for aardvark operations on the grid.
 */
public abstract class Aardvark extends ServiceAdapter {

    protected ConfigurationManager cm;

    /**
     * The number of crawler processes to start.
     */
    @ConfigInteger(defaultValue = 6)
    public static final String PROP_NUM_CRAWLERS = "numCrawlers";

    protected int numCrawlers;

    public String getFMName(int n) {
        return "feedMgr-" + n;
    }

    public String getRecName() {
        return "recommender";
    }

    public String getSchedName() {
        return "feedSched";
    }

    public String getAAName() {
        return "aardvark";
    }

    protected ProcessConfiguration getFeedSchedulerConfig() throws Exception {
        throw new UnsupportedOperationException("Functionality has been disabled");
        /*
        String[] cmdLine = new String[]{
            "-Xmx2g",
            "-DauraHome=" + GridUtil.auraDistMntPnt,
            "-DauraGroup=" + instance + "-aura",
            "-DcacheDir=" + GridUtil.cacheFSMntPnt,
            "-jar",
            GridUtil.auraDistMntPnt + "/dist/grid.jar",
            "/com/sun/labs/aura/aardvark/resource/feedSchedulerConfig.xml",
            "feedSchedulerStarter"
        };

        // create a configuration and set relevant properties
        return gu.getProcessConfig(FeedScheduler.class.getName(), cmdLine, getSchedName());
         */
    }

    protected ProcessConfiguration getFeedManagerConfig(int n)
            throws Exception {
        throw new UnsupportedOperationException("Functionality has been disabled");
        /*
        String[] cmdLine = new String[]{
            "-DauraHome=" + GridUtil.auraDistMntPnt,
            "-DauraGroup=" + instance + "-aura",
            "-jar",
            GridUtil.auraDistMntPnt + "/dist/grid.jar",
            "/com/sun/labs/aura/aardvark/resource/feedManagerConfig.xml",
            "feedManagerStarter"
        };

        // create a configuration and set relevant properties
        ProcessConfiguration pc = gu.getProcessConfig(FeedManager.class.getName(), 
                cmdLine, getFMName(n));

        // don't overlap with other replicants
        pc.setLocationConstraint(
                new ProcessRegistrationFilter.NameMatch(
                Pattern.compile(instance + ".*-feedMgr-.*")));

        return pc;
         */
    }

    protected ProcessConfiguration getRecommenderConfig() throws Exception {
        String[] cmdLine = new String[]{
            "-DauraHome=" + GridUtil.auraDistMntPnt,
            "-DauraGroup=" + instance + "-aura",
            "-jar",
            GridUtil.auraDistMntPnt + "/dist/grid.jar",
            "/com/sun/labs/aura/aardvark/resource/recommenderManagerConfig.xml",
            "recommenderManagerStarter"
        };

        return gu.getProcessConfig(RecommenderManager.class.getName(), 
                cmdLine, getRecName());
    }

    protected ProcessConfiguration getAardvarkConfig() throws Exception {
        String[] cmdLine = new String[]{
            "-DauraHome=" + GridUtil.auraDistMntPnt,
            "-DauraGroup=" + instance + "-aura",
            "-jar",
            GridUtil.auraDistMntPnt + "/dist/grid.jar",
            "/com/sun/labs/aura/aardvark/resource/aardvarkConfig.xml",
            "aardvarkStarter"
        };

        // create a configuration and set relevant properties
        return gu.getProcessConfig(Aardvark.class.getName(), 
                cmdLine, getAAName());
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        cm = ps.getConfigurationManager();
        numCrawlers = ps.getInt(PROP_NUM_CRAWLERS);
    }
}
