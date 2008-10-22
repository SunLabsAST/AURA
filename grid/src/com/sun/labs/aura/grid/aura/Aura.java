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
import com.sun.labs.aura.util.StatService;
import com.sun.labs.util.props.ConfigInteger;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * A base class for Aura setup and teardown.
 */
public abstract class Aura extends ServiceAdapter {

    @ConfigString(defaultValue =
    "/com/sun/labs/aura/resource/replicantSlowDumpConfig.xml")
    public static final String PROP_REPLICANT_CONFIG = "replicantConfig";

    @ConfigInteger(defaultValue=16)
    public static final String PROP_DEFAULT_NUM_REPLICANTS = "defaultNumReplicants";

    private int defaultNumReplicants;

    protected String replicantConfig;

    /**
     * A map from prefixes to the file systems for those prefixes.
     */
    protected Map<String, FileSystem> repFSMap = new HashMap<String, FileSystem>();

    /**
     * Creates the replicant filesystems in a clean startup.  Initially 16 file
     * systems are created.  This number may change over time as filesystems are
     * added by splitting.
     * @throws java.lang.Exception
     */
    public void createReplicantFileSystems() throws Exception {
        for(int i = 0; i < defaultNumReplicants; i++) {
            createReplicantFileSystem(DSBitSet.parse(i).setPrefixLength(4).toString());
        }
    }
    
    /**
     * Creates a replicant file system for a given prefix.
     * @param prefix
     * @throws java.lang.Exception
     */
    public void createReplicantFileSystem(String prefix) throws Exception {
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
        fsConfig.setMetadata(md);
        ((BaseFileSystem) fs).changeConfiguration(fsConfig);
        repFSMap.put(prefix, fs);
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
            
            mdv = md.get("prefix");
            if(mdv == null) {
                logger.warning("Replicant filesystem with no prefix metadata: " + fs.getName());
                continue;
            } else {
                logger.info("Got filesystem with prefix: " + mdv);
            }
            
            repFSMap.put(mdv, fs);
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
            "dataStoreHeadStarter"
        };

        ProcessConfiguration pc = gu.getProcessConfig(DataStoreHead.class.getName(), 
                cmdLine, getDataStoreHeadName(instanceNumber));
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
            "replicantStarter"
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
            "dataStoreHeadStarter"
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
            "gpmStarter"
        };

        return gu.getProcessConfig(ProcessManager.class.getName(), 
                cmdLine, getProcessManagerName());
    }

    protected ProcessConfiguration getPartitionClusterConfig(String prefix)
            throws Exception {
        return getPartitionClusterConfig(prefix, true);
    }
    
    protected ProcessConfiguration getPartitionClusterConfig(String prefix, boolean register)
            throws Exception {
        String[] cmdLine = new String[]{
            "-DauraHome=" + GridUtil.auraDistMntPnt,
            "-DauraGroup=" + instance + "-aura",
            "-Dprefix=" + prefix,
            "-jar",
            GridUtil.auraDistMntPnt + "/dist/grid.jar",
            "/com/sun/labs/aura/resource/partitionClusterConfig.xml",
            register ? "partitionClusterStarter" : "noRegPartitionClusterStarter"
        };

        ProcessConfiguration pc = gu.getProcessConfig(PartitionCluster.class.getName(), cmdLine, getPartitionName(prefix), null, false);
        Map<String,String> md = pc.getMetadata();
        md.put("prefix", prefix);
        md.put("monitor", "true");
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
            "partitionClusterStarter"
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
            replicantConfig,
            "replicantStarter"
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
            "statServiceStarter"
        };

        return gu.getProcessConfig(StatService.class.getName(), 
                cmdLine, getStatServiceName());
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        replicantConfig = ps.getString(PROP_REPLICANT_CONFIG);
        defaultNumReplicants = ps.getInt(PROP_DEFAULT_NUM_REPLICANTS);
        try {
            getReplicantFileSystems();
        } catch(Exception ex) {
            logger.log(Level.SEVERE, "Error getting filesystems", ex);
        }
    }
}
