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

package com.sun.labs.aura.grid.aura;

import com.sun.caroline.platform.BaseFileSystem;
import com.sun.caroline.platform.FileSystem;
import com.sun.caroline.platform.ResourceName;
import com.sun.caroline.platform.SnapshotFileSystem;
import com.sun.caroline.platform.SnapshotFileSystemConfiguration;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * A grid service that will prepare for a release by snapshotting all of our
 * important data so that we can roll back if necessary.
 */
public class PrepareForRelease extends Aura {

    @ConfigString(defaultValue="rel")
    public static final String PROP_REL_NAME = "relName";

    private String relName;
    
    @Override
    public String serviceName() {
        return "PrepareForRelease";
    }

    @Override
    public void start() {

        try {
            //
            // Snapshot the dist file system.
            BaseFileSystem bfs = (BaseFileSystem) gu.getFS("aura.dist");
            bfs.createSnapshot(ResourceName.getCSName(bfs.getName()) + "-" + relName);

            //
            // Snapshot the logs.
            bfs = (BaseFileSystem) gu.getFS("aura.logs");
            bfs.createSnapshot(ResourceName.getCSName(bfs.getName()) + "-" +
                    relName);

            //
            // Snapshot the cache filesystem.
            bfs = (BaseFileSystem) gu.getFS("cache");
            bfs.createSnapshot(ResourceName.getCSName(bfs.getName()) + "-" +
                    relName);

            snapShotReplicants(relName);
            
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error preparing for release", ex);
        }
    }

    @Override
    public void stop() {
    }

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        relName = ps.getString(PROP_REL_NAME);
    }


}
