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

import com.sun.caroline.platform.BaseFileSystem;
import com.sun.caroline.platform.BaseFileSystemConfiguration;
import com.sun.labs.aura.grid.ServiceAdapter;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Map.Entry;

/**
 * View filesystem meta data
 */
public class FSMetaData extends ServiceAdapter {

    @ConfigString(defaultValue = "live-aura.dist")
    public final static String PROP_FS_NAME = "fsName";

    protected String fsName;

    public String serviceName() {
        return "FSMetaData";
    }
    
    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        fsName = ps.getString(PROP_FS_NAME);
        //fsName = "foo-replicant-0";
    }

    public void start() {
        BaseFileSystem fs;
        try {
            fs = (BaseFileSystem)grid.getFileSystem(fsName);
        } catch(RemoteException ex) {
            logger.severe("Error getting filesystem");
            return;
        }
        if(fs == null) {
            logger.severe("No such filesystem");
            return;
        }
        
        BaseFileSystemConfiguration fsc = fs.getConfiguration();
        Map<String,String> md = fsc.getMetadata();
        System.out.println("Filesystem " + fsName + " has " + md.size() + " entries");
        for (Entry<String,String> e : md.entrySet()) {
            System.out.println(String.format("%20s %20s", e.getKey(), e.getValue()));
        }
        /*
        md.put("prefix", "00");
        fsc.setMetadata(md);
        try {
            fs.changeConfiguration(fsc);
        } catch (Exception rx) {
            logger.log(Level.SEVERE, "Error setting file system metadata for " + fs.getName(), rx);
        }

        logger.info("Done");
        */
    }

    public void stop() {
    }


}
