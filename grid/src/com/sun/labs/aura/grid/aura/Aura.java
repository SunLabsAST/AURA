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
import java.util.regex.Pattern;

/**
 * A base class for Aura setup and teardown.
 */
public abstract class Aura extends ServiceAdapter {

    @ConfigString(defaultValue =
    "/com/sun/labs/aura/resource/replicantSlowDumpConfig.xml")
    public static final String PROP_REPLICANT_CONFIG = "replicantConfig";

    protected String replicantConfig;

    @ConfigInteger(defaultValue = 4)
    public static final String PROP_N_NODES = "nNodes";

    private int nNodes;

    protected String[] prefixCodeList;

    private Map<String, FileSystem> repFSMap = new HashMap<String, FileSystem>();
    
    public void getReplicantFileSystems() throws Exception {

        //
        // Set up the file systems for each replicant
        for(String currPrefix : prefixCodeList) {
            logger.info("Making fs for prefix " + currPrefix);
            FileSystem fs = gu.getFS(getReplicantName(currPrefix));
            
            //
            // Add metadata for the prefix being handled.
            BaseFileSystemConfiguration fsConfig = ((BaseFileSystem) fs).getConfiguration();
            Map<String,String> md = fsConfig.getMetadata();
            logger.info("fs metadata: " + md);
            md.put("prefix", currPrefix);
            fsConfig.setMetadata(md);
            ((BaseFileSystem) fs).changeConfiguration(fsConfig);
            
            repFSMap.put(currPrefix, fs);
            logger.info("Made fs for prefix " + currPrefix);
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

        return gu.getProcessConfig(DataStoreHead.class.getName(), 
                cmdLine, getDataStoreHeadName(instanceNumber));
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
        String[] cmdLine = new String[]{
            "-DauraHome=" + GridUtil.auraDistMntPnt,
            "-DauraGroup=" + instance + "-aura",
            "-Dprefix=" + prefix,
            "-jar",
            GridUtil.auraDistMntPnt + "/dist/grid.jar",
            "/com/sun/labs/aura/resource/partitionClusterConfig.xml",
            "partitionClusterStarter"
        };

        ProcessConfiguration pc = gu.getProcessConfig(PartitionCluster.class.getName(), cmdLine, getPartitionName(prefix), null, true);
        Map<String,String> md = pc.getMetadata();
        md.put("prefix", prefix);
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
        logger.info("Instance: " + instance);

        //
        // Figure out how many nodes are in our data store and create the prefixes.
        nNodes = ps.getInt(PROP_N_NODES);
        if(Integer.highestOneBit(nNodes) != Integer.lowestOneBit(nNodes)) {
            throw new PropertyException(ps.getInstanceName(), PROP_N_NODES,
                    "nNodes must be a power of 2");
        }
        int numBits = Integer.toString(nNodes, 2).length() - 1;
        prefixCodeList = new String[nNodes];
        for(int i = 0; i < nNodes; i++) {
            DSBitSet prefixBits = DSBitSet.parse(i);
            prefixBits.setPrefixLength(numBits);
            prefixCodeList[i] = prefixBits.toString();
        }
        replicantConfig = ps.getString(PROP_REPLICANT_CONFIG);
    }
}
