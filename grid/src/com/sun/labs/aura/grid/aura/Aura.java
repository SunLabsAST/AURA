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
import com.sun.caroline.platform.BaseFileSystemConfiguration;
import com.sun.labs.aura.grid.util.GridUtil;
import com.sun.caroline.platform.FileSystem;
import com.sun.caroline.platform.FileSystemMountParameters;
import com.sun.caroline.platform.ProcessConfiguration;
import com.sun.caroline.platform.ProcessRegistrationFilter;
import com.sun.labs.aura.datastore.impl.DSBitSet;
import com.sun.labs.aura.datastore.impl.DataStoreHead;
import com.sun.labs.aura.datastore.impl.PartitionCluster;
import com.sun.labs.aura.datastore.impl.ProcessManager;
import com.sun.labs.aura.datastore.impl.Replicant;
import com.sun.labs.aura.grid.ServiceAdapter;
import com.sun.labs.aura.service.LoginService;
import com.sun.labs.aura.service.StatService;
import com.sun.labs.util.props.ConfigInteger;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * A base class for Aura setup and teardown.
 */
public abstract class Aura extends ServiceAdapter {

    @ConfigString(defaultValue =
    "/com/sun/labs/aura/resource/replicantSlowDumpConfig.xml")
    public static final String PROP_REPLICANT_CONFIG = "replicantConfig";

    protected String replicantConfig;

    @ConfigString(defaultValue="dataStoreHeadStarter")
    public static final String PROP_DATA_STORE_STARTER = "dataStoreStarter";

    private String dataStoreStarter;

    @ConfigInteger(defaultValue=16)
    public static final String PROP_DEFAULT_NUM_REPLICANTS = "defaultNumReplicants";

    private int defaultNumReplicants;

    /**
     * A map from prefixes to the file systems for those prefixes.  This map
     * stores all the active prefixes/filesystems.
     */
    protected Map<String, FileSystem> repFSMap = new HashMap<String, FileSystem>();

    protected Map<String, FileSystem> ownedFSMap = new HashMap<String, FileSystem>();

    protected FileSystem loginSvcFS = null;

    public void createLoginSvcFileSystem(String owner) throws Exception {
        logger.info("Creating LoginService fs");
        loginSvcFS = gu.getFS(getLoginServiceName(), true);
    }

    /**
     * Creates the replicant filesystems in a clean startup.  Initially 16 file
     * systems are created.  This number may change over time as filesystems are
     * added by splitting.
     * @throws java.lang.Exception
     */
    public void createReplicantFileSystems() throws Exception {
        int numBits = Integer.toString(defaultNumReplicants, 2).length() - 1;
        for(int i = 0; i < defaultNumReplicants; i++) {
            createReplicantFileSystem(DSBitSet.parse(i).setPrefixLength(numBits).toString());
        }
    }
    
    /**
     * Creates a replicant file system for a given prefix.
     * @param prefix
     * @throws java.lang.Exception
     */
    public void createReplicantFileSystem(String prefix) throws Exception {
        createReplicantFileSystem(prefix, null);
    }
    
    public void createReplicantFileSystem(String prefix, String owner) throws Exception {
        logger.info("Creating replicant fs for " + prefix);
        FileSystem fs = gu.getFS(getReplicantName(prefix), true);

        //
        // Add metadata to taste.
        BaseFileSystemConfiguration fsConfig = ((BaseFileSystem) fs).
                getConfiguration();
        Map<String, String> md = fsConfig.getMetadata();
        md.put("instance", instance);
        md.put("type", "replicant");
        md.put("prefix", prefix);
        if (owner != null && !owner.isEmpty()) {
            ownedFSMap.put(prefix, fs);
            md.put("owner", owner);
        } else {
            repFSMap.put(prefix, fs);
        }
        fsConfig.setMetadata(md);
        ((BaseFileSystem) fs).changeConfiguration(fsConfig);
    }

    /**
     * Gets the replicant filesystems that currently exist on the grid.
     * @throws java.lang.Exception
     */
    public void getReplicantFileSystems() throws Exception {

        for(FileSystem fs : gu.getGrid().findAllFileSystems()) {
            if(!(fs instanceof BaseFileSystem)) {
                continue;
            }

            //
            // Add metadata for the prefix being handled.
            BaseFileSystemConfiguration fsConfig = ((BaseFileSystem) fs).getConfiguration();
            Map<String,String> md = fsConfig.getMetadata();
            
            String mdv = md.get("instance");
            if(mdv == null || !mdv.equals(instance)) {
                continue;
            }
            
            mdv = md.get("type");
            if(mdv == null || !mdv.equals("replicant")) {
                continue;
            }
            
            String prefix = md.get("prefix");
            if(prefix == null) {
                logger.warning("Replicant filesystem with no prefix metadata: " + fs.getName());
                continue;
            }
            
            mdv = md.get("owner");
            if (mdv == null) {
                repFSMap.put(prefix, fs);
            } else {
                ownedFSMap.put(prefix, fs);
            }
        }
    }

    public String getDataStoreHeadName(int instanceNumber) {
        return "dsHead-" + instanceNumber;
    }
    
    public String getAIOVMName() {
        return "aiovm";
    }

    public String getPartitionName(String prefix) {
        return "part-" + prefix;
    }

    public String getReplicantName(String prefix) {
        return "replicant-" + prefix;
    }

    public String getStatServiceName() {
        return "statSrv";
    }

    public String getLoginServiceName() {
        return "loginSvc";
    }

    public String getReggieName() {
        return "reggie";
    }
    
    public String getProcessManagerName() {
        return "pm";
    }
    
    protected ProcessConfiguration getReggieConfig() throws Exception {
        String[] cmdLine = new String[]{
            "-DauraGroup=" + instance + "-aura",
            "-Djava.security.policy=" + GridUtil.auraDistMntPnt +
            "/jini/jsk-all.policy",
            "-Djava.util.logging.config.file=" + GridUtil.auraDistMntPnt +
            "/jini/logging.properties",
            "-jar",
            GridUtil.auraDistMntPnt + "/jini/lib/start.jar",
            GridUtil.auraDistMntPnt + "/jini/nobrowse.config"
        };

        return gu.getProcessConfig("reggie", cmdLine, getReggieName());
    }
    
    protected ProcessConfiguration getAIOVMConfig() throws Exception {
        String[] cmdLine = new String[]{
            "-Xmx3g",
            "-DauraHome=/files/data",
            "-DauraGroup=" + instance + "-aura",
            "-DauraDistDir=" + GridUtil.auraDistMntPnt +"/dist",
            "-DstartingDataDir=" + GridUtil.auraDistMntPnt +
            "/classifier/starting.idx",
            "-jar",
            GridUtil.auraDistMntPnt + "/dist/grid.jar",
            "/com/sun/labs/aura/resource/exportedAIOVMDSConfig.xml",
            "aiovm-starter"
        };
        
        FileSystem dsfs = gu.getFS(getAIOVMName());
        List<FileSystemMountParameters> extraMounts =
                Collections.singletonList(new FileSystemMountParameters(
                dsfs.getUUID(),
                "data"));
        return gu.getProcessConfig("AIOVM", cmdLine, getAIOVMName(),
                extraMounts);
    }
    

    protected ProcessConfiguration getDataStoreHeadConfig() throws Exception {
        return getDataStoreHeadConfig(1);
    }
    
    protected ProcessConfiguration getDataStoreHeadConfig(int instanceNumber) throws Exception {
        String[] cmdLine = new String[]{
            "-Xmx2G",
            "-DauraGroup=" + instance + "-aura",
            "-DauraHome=" + GridUtil.auraDistMntPnt,
            "-jar",
            GridUtil.auraDistMntPnt + "/dist/grid.jar",
            "/com/sun/labs/aura/resource/dataStoreHeadConfig.xml",
            dataStoreStarter,
            String.format("%s/dshead/dshead-%d.%%g.out", GridUtil.logFSMntPnt, instanceNumber)
        };

        (new File(GridUtil.logFSMntPnt + "/dshead")).mkdir();

        ProcessConfiguration pc = gu.getProcessConfig(DataStoreHead.class.getName(), 
                cmdLine, getDataStoreHeadName(instanceNumber));
        // don't overlap with other data store heads.
        pc.setLocationConstraint(
                new ProcessRegistrationFilter.NameMatch(
                Pattern.compile(instance + ".*-dshead-.*")));
        Map<String, String> md = pc.getMetadata();
        md.put("monitor", "true");
        pc.setMetadata(md);
        return pc;
    }
    
    protected ProcessConfiguration getTSRConfig(String prefix) throws Exception {
        String[] cmdLine = new String[]{
            "-Xmx3g",
            "-DauraHome=" + GridUtil.auraDistMntPnt,
            "-DauraGroup=" + instance + "-aura",
            "-DstartingDataDir=" + GridUtil.auraDistMntPnt +
            "/classifier/starting.idx",
            "-Dprefix=" + prefix,
            "-DdataFS=/files/data/" + prefix,
            "-jar",
            GridUtil.auraDistMntPnt + "/dist/grid.jar",
            "/com/sun/labs/aura/util",
            "replicantStarter",
            String.format("%s/rep/rep-%s.%%g.out", GridUtil.logFSMntPnt, prefix)
        };

        List<FileSystemMountParameters> extraMounts =
                Collections.singletonList(new FileSystemMountParameters(
                repFSMap.get(prefix).getUUID(),
                "data"));
        ProcessConfiguration pc = gu.getProcessConfig(
                Replicant.class.getName(),
                cmdLine, getReplicantName(
                prefix), extraMounts);

        // don't overlap with other replicants
        pc.setLocationConstraint(
                new ProcessRegistrationFilter.NameMatch(
                Pattern.compile(instance + ".*-replicant-.*")));
        Map<String, String> md = pc.getMetadata();
        md.put("prefix", prefix);
        md.put("monitor", "true");
        pc.setMetadata(md);
        return pc;
        
    }

    protected ProcessConfiguration getDataStoreHeadDebugConfig(int instanceNumber) throws Exception {
        String[] cmdLine = new String[]{
            "-Xmx2G",
            "-Djava.util.logging.config.file=" + GridUtil.auraDistMntPnt +
            "/dist/rmilogging.properties",
            "-DauraGroup=" + instance + "-aura",
            "-DauraHome=" + GridUtil.auraDistMntPnt,
            "-jar",
            GridUtil.auraDistMntPnt + "/dist/grid.jar",
            "/com/sun/labs/aura/resource/dataStoreHeadConfig.xml",
            "dataStoreHeadStarter",
            String.format("%s/dshead/dshead-%d.%%g.out", GridUtil.logFSMntPnt, instanceNumber)
        };

        return gu.getProcessConfig(DataStoreHead.class.getName(), 
                cmdLine, getDataStoreHeadName(instanceNumber));
    }

    protected ProcessConfiguration getProcessManagerConfig()
            throws Exception {
        String[] cmdLine = new String[]{
            "-DauraHome=" + GridUtil.auraDistMntPnt,
            "-DauraGroup=" + instance + "-aura",
            "-DauraInstance=" + instance,
            "-jar",
            GridUtil.auraDistMntPnt + "/dist/grid.jar",
            "/com/sun/labs/aura/grid/aura/gpmConfig.xml",
            "gpmStarter",
            String.format("%s/pm/pm.%%g.out", GridUtil.logFSMntPnt)
        };

        return gu.getProcessConfig(ProcessManager.class.getName(), 
                cmdLine, getProcessManagerName());
    }

    protected ProcessConfiguration getPartitionClusterConfig(String prefix)
            throws Exception {
        return getPartitionClusterConfig(prefix, true, null);
    }
    
    protected ProcessConfiguration getPartitionClusterConfig(String prefix, boolean register)
            throws Exception {
        return getPartitionClusterConfig(prefix, register, null);
    }
    
    protected ProcessConfiguration getPartitionClusterConfig(String prefix, boolean register, String owner)
            throws Exception {
        String[] cmdLine = new String[]{
            "-DauraHome=" + GridUtil.auraDistMntPnt,
            "-DauraGroup=" + instance + "-aura",
            "-Dprefix=" + prefix,
            "-Downer=" + ((owner != null && !owner.isEmpty()) ? owner : "\"\""),
            "-jar",
            GridUtil.auraDistMntPnt + "/dist/grid.jar",
            "/com/sun/labs/aura/resource/partitionClusterConfig.xml",
            register ? "partitionClusterStarter" : "noRegPartitionClusterStarter",
            String.format("%s/pc/part-%s.%%g.out", GridUtil.logFSMntPnt, prefix),
        };

        ProcessConfiguration pc = gu.getProcessConfig(PartitionCluster.class.getName(),
                cmdLine, getPartitionName(prefix), null, true);
        Map<String,String> md = pc.getMetadata();
        md.put("prefix", prefix);
        md.put("monitor", "true");
        if (owner != null && !owner.isEmpty()) {
            md.put("owner", owner);
        }
        pc.setMetadata(md);
        return pc;
    }

    protected ProcessConfiguration getPartitionClusterDebugConfig(String prefix)
            throws Exception {
        String[] cmdLine = new String[]{
            "-Djava.util.logging.config.file=" + GridUtil.auraDistMntPnt +
            "/dist/rmilogging.properties",
            "-verbose:gc",
            "-XX:+PrintGCTimeStamps",
            "-XX:+PrintGCDetails",
            "-DauraHome=" + GridUtil.auraDistMntPnt,
            "-DauraGroup=" + instance + "-aura",
            "-Dprefix=" + prefix,
            "-jar",
            GridUtil.auraDistMntPnt + "/dist/grid.jar",
            "/com/sun/labs/aura/resource/partitionClusterConfig.xml",
            "partitionClusterStarter",
            String.format("%s/pc/part-%s.%%g.out", GridUtil.logFSMntPnt, prefix)
        };
        
        ProcessConfiguration pc = gu.getProcessConfig(PartitionCluster.class.getName(),
                cmdLine, getPartitionName(
                prefix), null, true);
        Map<String, String> md = pc.getMetadata();
        md.put("prefix", prefix);
        md.put("monitor", "true");
        pc.setMetadata(md);
        return pc;
    }

    protected ProcessConfiguration getReplicantConfig(String replicantConfig,
            String prefix)
            throws Exception {

        String deleteProp = System.getProperty("deleteIndexDir");

        String[] cmdLine;
        
        if(deleteProp == null) {
            cmdLine = new String[]{
                        "-Xmx3g",
                        "-DauraHome=" + GridUtil.auraDistMntPnt,
                        "-DauraGroup=" + instance + "-aura",
                        "-DstartingDataDir=" + GridUtil.auraDistMntPnt +
                        "/classifier/starting.idx",
                        "-Dprefix=" + prefix,
                        "-DdataFS=/files/data",
                        "-jar",
                        GridUtil.auraDistMntPnt + "/dist/grid.jar",
                        replicantConfig,
                        "replicantStarter",
                        String.format("%s/rep/rep-%s.%%g.out",
                                      GridUtil.logFSMntPnt, prefix)
                    };
        } else {
            cmdLine = new String[]{
                        "-Xmx3g",
                        "-DauraHome=" + GridUtil.auraDistMntPnt,
                        "-DauraGroup=" + instance + "-aura",
                        "-DdeleteIndexDir=" + deleteProp,
                        "-DstartingDataDir=" + GridUtil.auraDistMntPnt +
                        "/classifier/starting.idx",
                        "-Dprefix=" + prefix,
                        "-DdataFS=/files/data",
                        "-jar",
                        GridUtil.auraDistMntPnt + "/dist/grid.jar",
                        replicantConfig,
                        "replicantStarter",
                        String.format("%s/rep/rep-%s.%%g.out",
                                      GridUtil.logFSMntPnt, prefix)
            };

        }

        FileSystem fs = repFSMap.get(prefix);
        if (fs == null) {
            fs = ownedFSMap.get(prefix);
        }
        List<FileSystemMountParameters> extraMounts =
                Collections.singletonList(new FileSystemMountParameters(
                fs.getUUID(),
                "data"));
        ProcessConfiguration pc = gu.getProcessConfig(
                Replicant.class.getName(),
                cmdLine, getReplicantName(
                prefix), extraMounts);

        // don't overlap with other replicants
        pc.setLocationConstraint(
                new ProcessRegistrationFilter.NameMatch(
                Pattern.compile(instance + ".*-replicant-.*")));
        Map<String,String> md = pc.getMetadata();
        md.put("prefix", prefix);
        md.put("monitor", "true");
        pc.setMetadata(md);
        return pc;
    }

    /**
     * Gets a config for a combined partition cluster and replicant, for
     * efficiency's sake.
     *
     * @param prefix the prefix that the replicant and PC should have.
     * @return a process configuration for the replicant/PC combo
     * @throws java.lang.Exception
     */
    protected ProcessConfiguration getPCReplicantConfig(String prefix)
            throws Exception {
        String[] cmdLine = new String[]{
            "-Xmx3g",
            "-DauraHome=" + GridUtil.auraDistMntPnt,
            "-DauraGroup=" + instance + "-aura",
            "-DstartingDataDir=" + GridUtil.auraDistMntPnt +
            "/classifier/starting.idx",
            "-Dprefix=" + prefix,
            "-DdataFS=/files/data",
            "-jar",
            GridUtil.auraDistMntPnt + "/dist/grid.jar",
            "/com/sun/labs/aura/resource/repPCConfig.xml",
            "repPCStarter",
            String.format("%s/rep/rep-%s.%%g.out", GridUtil.logFSMntPnt, prefix)
        };

        FileSystem fs = repFSMap.get(prefix);
        if (fs == null) {
            fs = ownedFSMap.get(prefix);
        }
        List<FileSystemMountParameters> extraMounts =
                Collections.singletonList(new FileSystemMountParameters(
                fs.getUUID(),
                "data"));
        ProcessConfiguration pc = gu.getProcessConfig(
                Replicant.class.getName(),
                cmdLine, getReplicantName(
                prefix), extraMounts);

        // don't overlap with other replicants
        pc.setLocationConstraint(
                new ProcessRegistrationFilter.NameMatch(
                Pattern.compile(instance + ".*-replicant-.*")));
        Map<String,String> md = pc.getMetadata();
        md.put("prefix", prefix);
        md.put("monitor", "true");
        pc.setMetadata(md);
        return pc;
    }

    protected ProcessConfiguration getDebugReplicantConfig(String replicantConfig,
            String prefix)
            throws Exception {
        String[] cmdLine = new String[]{
            "-Xmx3g",
            "-Djava.util.logging.config.file=" + GridUtil.auraDistMntPnt +
            "/dist/rmilogging.properties",
            "-DauraHome=" + GridUtil.auraDistMntPnt,
            "-DauraGroup=" + instance + "-aura",
            "-DstartingDataDir=" + GridUtil.auraDistMntPnt +
            "/classifier/starting.idx",
            "-Dprefix=" + prefix,
            "-DdataFS=/files/data",
            "-jar",
            GridUtil.auraDistMntPnt + "/dist/grid.jar",
            replicantConfig,
            "replicantStarter",
            String.format("%s/rep/rep-%s.%%g.out", GridUtil.logFSMntPnt, prefix)
        };

        FileSystem fs = repFSMap.get(prefix);
        if (fs == null) {
            fs = ownedFSMap.get(prefix);
        }
        List<FileSystemMountParameters> extraMounts =
                Collections.singletonList(new FileSystemMountParameters(
                fs.getUUID(),
                "data"));
        ProcessConfiguration pc = gu.getProcessConfig(
                Replicant.class.getName(),
                cmdLine, getReplicantName(
                prefix), extraMounts);

        // don't overlap with other replicants
        pc.setLocationConstraint(
                new ProcessRegistrationFilter.NameMatch(
                Pattern.compile(instance + ".*-replicant-.*")));
        Map<String,String> md = pc.getMetadata();
        md.put("prefix", prefix);
        md.put("monitor", "true");
        pc.setMetadata(md);
        return pc;
    }

    protected ProcessConfiguration getStatServiceConfig() throws Exception {
        String[] cmdLine = new String[]{
            "-DauraHome=" + GridUtil.auraDistMntPnt,
            "-DauraGroup=" + instance + "-aura",
            "-jar",
            GridUtil.auraDistMntPnt + "/dist/grid.jar",
            "/com/sun/labs/aura/resource/statServiceConfig.xml",
            "statServiceStarter",
            String.format("%s/other/stats.%%g.out", GridUtil.logFSMntPnt)
        };

        return gu.getProcessConfig(StatService.class.getName(), 
                cmdLine, getStatServiceName());
    }

    protected ProcessConfiguration getLoginServiceConfig() throws Exception {
        String[] cmdLine = new String[]{
            "-DauraHome=" + GridUtil.auraDistMntPnt,
            "-DauraGroup=" + instance + "-aura",
            "-DdataFS=/files/data",
            "-jar",
            GridUtil.auraDistMntPnt + "/dist/grid.jar",
            "/com/sun/labs/aura/resource/loginServiceConfig.xml",
            "loginServiceStarter",
            String.format("%s/other/login.%%g.out", GridUtil.logFSMntPnt)
        };

        List<FileSystemMountParameters> extraMounts =
                Collections.singletonList(new FileSystemMountParameters(
                loginSvcFS.getUUID(),
                "data"));
        ProcessConfiguration pc = gu.getProcessConfig(
                LoginService.class.getName(),
                cmdLine, getLoginServiceName(),
                extraMounts);

        return pc;
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        replicantConfig = ps.getString(PROP_REPLICANT_CONFIG);
        defaultNumReplicants = ps.getInt(PROP_DEFAULT_NUM_REPLICANTS);
        dataStoreStarter = ps.getString(PROP_DATA_STORE_STARTER);
        try {
            getReplicantFileSystems();
        } catch(Exception ex) {
            logger.log(Level.SEVERE, "Error getting filesystems", ex);
        }

        //
        // Make grid directories that we'll need.
        for(String dir : new String[] {"dshead", "rep", "pc", "pm", "other"}) {
            (new File(GridUtil.logFSMntPnt + "/" + dir)).mkdir();
        }
    }
}
