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

package com.sun.labs.aura.grid.rp;

import com.sun.caroline.platform.FileSystemMountParameters;
import com.sun.caroline.platform.ProcessConfiguration;
import com.sun.caroline.platform.ProcessRegistration;
import com.sun.caroline.platform.RunState;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.grid.ServiceAdapter;
import com.sun.labs.aura.grid.ServiceDeployer;
import com.sun.labs.aura.grid.util.GridUtil;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.ConfigInteger;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.ConfigStringList;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.io.File;
import java.io.IOException;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class for processing (some of) the contents of a data store on a 
 * replicant-by-replicant basis.
 */
public class DataStoreProcessor extends ServiceAdapter {

    @ConfigComponent(type = com.sun.labs.aura.datastore.DataStore.class)
    public static final String PROP_DATA_STORE = "dataStore";

    private DataStore dataStore;

    @ConfigInteger(defaultValue = 4)
    public static final String PROP_NUM_PROCESSORS = "numProcessors";

    private int numProcessors;

    @ConfigString
    public static final String PROP_REPLICANT_CONFIG = "replicantConfig";

    private String replicantConfig;

    @ConfigString
    public static final String PROP_REPLICANT_STARTER = "replicantStarter";

    private String replicantStarter;

    @ConfigString
    public static final String PROP_PROCESSOR_NAME = "processorName";
    
    private String processorName;
    
    @ConfigStringList(defaultList={})
    public static final String PROP_PROCESSOR_JARS = "processorJars";

    private List<String> prefixes;

    private List<ProcessRegistration> running;
    
    /* Given the common functionality, this class should probably extend ServiceDeployer */
    private String baseCP = ServiceDeployer.baseClasspath;

    @Override
    public String serviceName() {
        return "DataStoreProcessor";
    }
    
    public DataStoreProcessor() throws IOException {
        //
        // This is probably overkill, but we'll extract the classpath from the 
        // grid jar file so that when that changes we won't have to remember
        // to fix the base class here.
        JarFile jarFile = new JarFile(GridUtil.auraDistMntPnt + "/dist/grid.jar");
        String classpath = jarFile.getManifest().getMainAttributes().getValue("Class-Path");
        ArrayList<String> jars = new ArrayList(Arrays.asList(classpath.split("\\s+")));
        jars.add(GridUtil.auraDistMntPnt + "/dist/grid.jar");
        StringBuilder sb = new StringBuilder();
        for(String jar : jars) {
            if(sb.length() > 0) {
                sb.append(":");
            }
            if(!jar.startsWith(File.separator)) {
                sb.append(GridUtil.auraDistMntPnt + File.separator + "dist" + File.separator);
            }
            sb.append(jar);
        }
        baseCP = sb.toString();
    }

    public void startProcess(String prefix) throws Exception {
        List<String> cmdLine = new ArrayList<String>();
        cmdLine.add("-DauraHome=" + GridUtil.auraDistMntPnt);
        cmdLine.add("-Dinstance=" + instance);
        cmdLine.add("-DauraGroup=" + instance + "-aura");
        cmdLine.add("-Dprefix=" + prefix);
        if(baseCP != null) {
            cmdLine.add("-cp");
            cmdLine.add(baseCP);
            logger.info("Using classpath: " + baseCP);
        }
        cmdLine.add("com.sun.labs.aura.AuraServiceStarter");
        cmdLine.add(replicantConfig);
        cmdLine.add(replicantStarter);
        String processName = processorName + "-" + prefix;
        ProcessConfiguration config =
                gu.getProcessConfig(cmdLine.toArray(new String[0]), processName);
        ProcessRegistration reg = gu.createProcess(processName, config);
        for(FileSystemMountParameters params : config.getFileSystems()) {
            logger.info("Mounting " + params.getMountPoint() + " on " + params.getUUID());
        }
        logger.info("Created registration: " + reg);
        gu.startRegistration(reg);
        running.add(reg);
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        processorName = ps.getString(PROP_PROCESSOR_NAME);
        replicantConfig = ps.getString(PROP_REPLICANT_CONFIG);
        replicantStarter = ps.getString(PROP_REPLICANT_STARTER);
        
        //
        // Add things to the classpath.
        List<String> cpa = ps.getStringList(PROP_PROCESSOR_JARS);
        for(String cpe : cpa) {
            baseCP += (":" + cpe);
        }
        
        //
        // Get the data store.
        try {
            dataStore = (DataStore)ps.getComponent(PROP_DATA_STORE);
        } catch(NullPointerException ne) {
            throw new IllegalStateException("Could not access data store");
        }
        numProcessors = ps.getInt(PROP_NUM_PROCESSORS);
        try {
            prefixes = dataStore.getPrefixes();
        } catch(RemoteException ex) {
            throw new PropertyException(ex, ps.getInstanceName(),
                    PROP_DATA_STORE, "Error getting prefixes from data store");
        }
        running = new ArrayList<ProcessRegistration>();
    }

    public void start() {

        //
        // Fill up the running processors.
        while(prefixes.size() > 0 && running.size() < numProcessors) {
            String prefix = prefixes.remove(0);
            try {
                startProcess(prefix);
            } catch(Exception e) {
                logger.log(Level.SEVERE,
                        "Error starting replicant processor for " + prefix, e);
            }
        }
        logger.info("Started processes: " + running.size());

        while(!prefixes.isEmpty() || !running.isEmpty()) {
            try {
                Thread.sleep(5000);
            } catch(InterruptedException ie) {
                return;
            }
            for(Iterator<ProcessRegistration> i = running.iterator(); i.hasNext();) {
                ProcessRegistration reg = i.next();
                try {
                    reg.refresh();
                    logger.info("Checking: " + reg.getName() + " : " + reg.getRunState().name());
                    if(reg.getRunState() == RunState.NONE) {
                        i.remove();
                        try {
                            logger.info("Destroying: " + reg.getName());
                            reg.destroy(100000);
                            logger.info("Destroyed: " + reg.getName());
                        } catch(RemoteException rx) {
                            logger.log(Level.SEVERE,
                                    "Error destroying replicant processor " + reg,
                                    rx);
                        }
                        if(!prefixes.isEmpty()) {
                            String prefix = prefixes.remove(0);
                            try {
                                startProcess(prefix);
                            } catch(Exception e) {
                                logger.log(Level.SEVERE,
                                        "Error starting replicant processor for " +
                                        prefix, e);
                           }
                        }
                    }
                } catch (RemoteException ex) {
                    logger.severe("Error refreshing registration: " + ex.getLocalizedMessage());
                }
            }
        }
    }

    public void stop() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
