package com.sun.labs.aura.grid.aura;

import com.sun.labs.aura.grid.util.GridUtil;
import com.sun.caroline.platform.FileSystem;
import com.sun.caroline.platform.FileSystemMountParameters;
import com.sun.caroline.platform.ProcessConfiguration;
import com.sun.caroline.platform.ProcessRegistrationFilter;
import com.sun.labs.aura.datastore.impl.DSBitSet;
import com.sun.labs.aura.grid.ServiceAdapter;
import com.sun.labs.util.props.ConfigInteger;
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
            repFSMap.put(currPrefix,
                    gu.getFS(getReplicantName(currPrefix)));
            logger.info("Made fs for prefix " + currPrefix);
        }
    }

    public String getDataStoreHeadName() {
        return "dsHead";
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

        return gu.getProcessConfig(cmdLine, getReggieName());
    }

    protected ProcessConfiguration getDataStoreHeadConfig() throws Exception {
        String[] cmdLine = new String[]{
            "-Xmx2G",
            "-DauraGroup=" + instance + "-aura",
            "-DauraHome=" + GridUtil.auraDistMntPnt,
            "-jar",
            GridUtil.auraDistMntPnt + "/dist/grid.jar",
            "/com/sun/labs/aura/aardvark/resource/dataStoreHeadConfig.xml",
            "dataStoreHeadStarter"
        };

        return gu.getProcessConfig(cmdLine, getDataStoreHeadName());
    }

    protected ProcessConfiguration getPartitionClusterConfig(String prefix)
            throws Exception {
        String[] cmdLine = new String[]{
            "-DauraHome=" + GridUtil.auraDistMntPnt,
            "-DauraGroup=" + instance + "-aura",
            "-Dprefix=" + prefix,
            "-jar",
            GridUtil.auraDistMntPnt + "/dist/grid.jar",
            "/com/sun/labs/aura/aardvark/resource/partitionClusterConfig.xml",
            "partitionClusterStarter"
        };

        return gu.getProcessConfig(cmdLine, getPartitionName(prefix));
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
        ProcessConfiguration pc = gu.getProcessConfig(cmdLine, getReplicantName(
                prefix), extraMounts);

        // don't overlap with other replicants
        pc.setLocationConstraint(
                new ProcessRegistrationFilter.NameMatch(
                Pattern.compile(instance + ".*-replicant-.*")));

        return pc;
    }

    protected ProcessConfiguration getStatServiceConfig() throws Exception {
        String[] cmdLine = new String[]{
            "-DauraHome=" + GridUtil.auraDistMntPnt,
            "-DauraGroup=" + instance + "-aura",
            "-jar",
            GridUtil.auraDistMntPnt + "/dist/grid.jar",
            "/com/sun/labs/aura/aardvark/resource/statServiceConfig.xml",
            "statServiceStarter"
        };

        return gu.getProcessConfig(cmdLine, getStatServiceName());
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);

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
    }
}