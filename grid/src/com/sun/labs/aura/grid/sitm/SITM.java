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

import com.sun.caroline.platform.ProcessConfiguration;
import com.sun.labs.aura.grid.util.GridUtil;
import com.sun.labs.aura.grid.ServiceAdapter;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.io.File;

/**
 * A base class for starting and stopping the SITM services.
 */
public abstract class SITM extends ServiceAdapter {

    public String getCrawlerControllerName() {
        return "crawlerController";
    }

    public String getArtistCrawlerName() {
        return "artistCrawler";
    }

    public String getListenerCrawlerName() {
        return "listenerCrawler";
    }

    public String getTagCrawlerName() {
        return "tagCrawler";
    }

    public ProcessConfiguration getCrawlerControllerConfig() throws Exception {
        String[] cmdLine = new String[]{
            "-Xmx2g",
            "-DauraHome=" + GridUtil.auraDistMntPnt,
            "-DauraGroup=" + instance + "-aura",
            "-DcacheDir=" + GridUtil.cacheFSMntPnt,
            "-jar",
            GridUtil.auraDistMntPnt + "/dist/grid.jar",
            "/com/sun/labs/aura/music/resource/crawlerControllerConfig.xml",
            "starter",
            String.format("%s/sitm/crawlerController.%%g.out", GridUtil.logFSMntPnt)
        };

        return gu.getProcessConfig(cmdLine, getCrawlerControllerName());
    }

    public ProcessConfiguration getArtistCrawlerConfig() throws Exception {
        String[] cmdLine = new String[]{
            "-Xmx2g",
            "-DauraHome=" + GridUtil.auraDistMntPnt,
            "-DauraGroup=" + instance + "-aura",
            "-DcacheDir=" + GridUtil.cacheFSMntPnt,
            "-jar",
            GridUtil.auraDistMntPnt + "/dist/grid.jar",
            "/com/sun/labs/aura/music/resource/artistCrawlerConfig.xml",
            "starter",
            String.format("%s/sitm/artistCrawler.%%g.out", GridUtil.logFSMntPnt)
        };

        return gu.getProcessConfig(cmdLine, getArtistCrawlerName());
    }

    public ProcessConfiguration getListenerCrawlerConfig() throws Exception {
        String[] cmdLine = new String[]{
            "-Xmx2g",
            "-DauraHome=" + GridUtil.auraDistMntPnt,
            "-DauraGroup=" + instance + "-aura",
            "-DcacheDir=" + GridUtil.cacheFSMntPnt,
            "-jar",
            GridUtil.auraDistMntPnt + "/dist/grid.jar",
            "/com/sun/labs/aura/music/resource/listenerCrawlerConfig.xml",
            "starter",
            String.format("%s/sitm/listenerCrawler.%%g.out", GridUtil.logFSMntPnt)
        };

        return gu.getProcessConfig(cmdLine, getListenerCrawlerName());
    }

    public ProcessConfiguration getTagCrawlerConfig() throws Exception {
        String[] cmdLine = new String[]{
            "-Xmx2g",
            "-DauraHome=" + GridUtil.auraDistMntPnt,
            "-DauraGroup=" + instance + "-aura",
            "-DcacheDir=" + GridUtil.cacheFSMntPnt,
            "-jar",
            GridUtil.auraDistMntPnt + "/dist/grid.jar",
            "/com/sun/labs/aura/music/resource/tagCrawlerConfig.xml",
            "starter",
            String.format("%s/sitm/tagCrawler.%%g.out", GridUtil.logFSMntPnt)
        };

        return gu.getProcessConfig(cmdLine, getTagCrawlerName());
    }

    @Override
    public String serviceName() {
        return "SITM";
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);

        //
        // Make grid directories that we'll need.
        for(String dir : new String[]{"sitm", "other"}) {
            (new File(GridUtil.logFSMntPnt + "/" + dir)).mkdir();
        }

    }
}
