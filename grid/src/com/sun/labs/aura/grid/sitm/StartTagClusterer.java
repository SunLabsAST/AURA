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

import com.sun.caroline.platform.FileSystem;
import com.sun.caroline.platform.FileSystemMountParameters;
import com.sun.caroline.platform.ProcessConfiguration;
import com.sun.caroline.platform.ProcessExitAction;
import com.sun.caroline.platform.ProcessRegistration;
import com.sun.labs.aura.grid.ServiceAdapter;
import com.sun.labs.aura.grid.util.GridUtil;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author mailletf
 */
public class StartTagClusterer extends ServiceAdapter {

    public static final String PROCESS_NAME = "tagClusterer";
    private static final String FILESYSTEM_NAME = "tagclusterer";

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
    }

    @Override
    public String serviceName() {
        return "TagClustererStarter";
    }

    @Override
    public void start() {
        try {

            FileSystem fs = createFileSystem();

            // Create the process
            ProcessRegistration clustererReg = gu.createProcess(
                    PROCESS_NAME,
                    getClustererConfig(fs));
            gu.startRegistration(clustererReg);

        } catch (Exception ex) {
            logger.severe(ex+" starting tag clusterer.");
            ex.printStackTrace();
        }
    }

    @Override
    public void stop() {
        
    }


    public FileSystem createFileSystem() throws Exception {
        logger.info("Creating fs");
        FileSystem fs = gu.getFS(FILESYSTEM_NAME, true);
        return fs;
    }


    private ProcessConfiguration getClustererConfig(FileSystem fs) throws Exception {
        String[] cmdLine = new String[]{
            "-d64",
            "-Xmx8G",
            "-DauraGroup=" + instance + "-aura",
            "-DauraHome=" + GridUtil.auraDistMntPnt,
            "-DfsPath=/files/tagclusterer/",
            "-jar",
            GridUtil.auraDistMntPnt + "/dist/grid.jar",
                "/com/sun/labs/aura/grid/sitm/tagclustererconfig.xml",
            "starter",
            String.format("%s/sitm/tagclusterer.%%g.out", GridUtil.logFSMntPnt)
        };

        // Mount information for output file
        List<FileSystemMountParameters> extraMounts =
                Collections.singletonList(new FileSystemMountParameters(
                fs.getUUID(),
                "tagclusterer"));

        ProcessConfiguration pc = gu.getProcessConfig(TagClusterer.class.getName(),
                cmdLine, "tagClusterer", extraMounts);
        pc.setProcessExitAction(ProcessExitAction.PARK);

        Map<String, String> md = pc.getMetadata();
        md.put("monitor", "false");
        pc.setMetadata(md);
        return pc;
    }

}
