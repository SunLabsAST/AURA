package com.sun.labs.aura.grid;

import com.sun.caroline.platform.CustomerNetworkConfiguration;
import com.sun.caroline.platform.DuplicateNameException;
import com.sun.caroline.platform.FileSystem;
import com.sun.caroline.platform.FileSystemMountParameters;
import com.sun.caroline.platform.Network;
import com.sun.caroline.platform.NetworkAllocationException;
import com.sun.caroline.platform.ProcessConfiguration;
import com.sun.caroline.platform.ProcessExitAction;
import com.sun.caroline.platform.ProcessRegistrationFilter;
import com.sun.labs.aura.datastore.impl.DSBitSet;
import com.sun.labs.util.props.ConfigInteger;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * A base class for Aura setup and teardown.
 */
public abstract class Aura extends ServiceAdapter {

    @ConfigInteger(defaultValue = 4)
    public static final String PROP_N_NODES = "nNodes";

    private int nNodes;

    protected String[] prefixCodeList;

    private FileSystem auraDist;
    
    private FileSystem logFS;
    
    private FileSystem cacheFS;

    private Map<String, FileSystem> repFSMap = new HashMap<String,FileSystem>();
    
    private Network network;

    public void getAuraFilesystems() throws Exception {

        GridUtil.getFS(grid, "sys.packages");
        auraDist = GridUtil.getAuraDistFS(grid, instance);
        logFS = GridUtil.getAuraLogFS(grid, instance);
        cacheFS = GridUtil.getCacheFS(grid, instance);
                
        //
        // Set up the file systems for each replicant
        for(String currPrefix : prefixCodeList) {
            logger.info("Making fs for prefix " + currPrefix);
            repFSMap.put(instance + "-" + currPrefix,
                    GridUtil.getFS(grid, getReplicantName(currPrefix)));
            logger.info("Made fs for prefix " + currPrefix);
        }
    }
    
    protected void createAuraNetwork() throws Exception {
        try {
            // Try to create a customer network for the test
            network = grid.createNetwork(instance + "-auraNet", 512,
                    new CustomerNetworkConfiguration());
            System.out.println("Created network " + network.getName());
        } catch(DuplicateNameException e) {
            // Reuse an existing network
            network = grid.getNetwork(instance + "-auraNet");
            System.out.println("Network already exists, reusing " + network.
                    getName());
        } catch(NetworkAllocationException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    public String getDataStoreHeadName() {
        return instance + "-dsHead";
    }

    public String getPartitionName(String prefix) {
        return instance + "-part-" + prefix;
    }

    public String getReplicantName(String prefix) {
        return instance + "-replicant-" + prefix;
    }

    public String getStatServiceName() {
        return instance + "-statSrv";
    }
    
    public String getReggieName() {
        return instance + "-reggie";
    }
    
    /**
     * Gets a basic process configuration 
     * @param cwd the working directory for the configuration
     * @param logFile the name of the log file to use
     * @param extraMounts any extra file system mounts beyond the dist file system
     * and log file system.
     * @return a process configuration.
     */
    protected ProcessConfiguration getProcessConfig(
            String cwd,
            String logFile, 
            Collection<FileSystemMountParameters> extraMounts) {
        ProcessConfiguration pc = new ProcessConfiguration();
        pc.setSystemSinks(GridUtil.logFSMntPnt + "/" + logFile, false);

        Collection<FileSystemMountParameters> mountParams =
                new ArrayList<FileSystemMountParameters>();

        mountParams.add(
                new FileSystemMountParameters(auraDist.getUUID(),
                new File(GridUtil.auraDistMntPnt).getName()));
        mountParams.add(
                new FileSystemMountParameters(logFS.getUUID(),
                new File(GridUtil.logFSMntPnt).getName()));
        mountParams.addAll(extraMounts);
        pc.setFileSystems(mountParams);
        pc.setWorkingDirectory(cwd);
        return pc;
    }

    protected ProcessConfiguration getReggieConfig() throws Exception {
        String cmdLine =
                "-Djava.security.policy=" + GridUtil.auraDistMntPnt +
                "/jini/jsk-all.policy" +
                " -Djava.util.logging.config.file=" + GridUtil.auraDistMntPnt +
                "/jini/logging.properties" +
                " -jar " + GridUtil.auraDistMntPnt + "/jini/lib/start.jar" +
                " " + GridUtil.auraDistMntPnt + "/jini/nobrowse.config";

        // create a configuration and set relevant properties
        ProcessConfiguration pc = getProcessConfig(GridUtil.auraDistMntPnt + "/jini", "reggie.out", Collections.EMPTY_LIST);
        pc.setCommandLine(cmdLine.trim().split(" "));

        // Set the addresses for the process
        List<UUID> addresses = new ArrayList<UUID>();
        addresses.add(GridUtil.getAddressFor(grid, network, instance + "-reggie").getUUID());

        pc.setNetworkAddresses(addresses);
        pc.setProcessExitAction(ProcessExitAction.PARK);

        return pc;
    }

    protected ProcessConfiguration getDataStoreHeadConfig() throws Exception {
        String cmdLine =
                "-Xmx2G" +
                " -DauraHome=" + GridUtil.auraDistMntPnt +
                " -jar " + GridUtil.auraDistMntPnt + "/dist/aardvark.jar" +
                " /com/sun/labs/aura/aardvark/resource/dataStoreHeadConfig.xml" +
                " dataStoreHeadStarter";

        // create a configuration and set relevant properties
        ProcessConfiguration pc = getProcessConfig(GridUtil.logFSMntPnt, "dsHead.out", 
            Collections.EMPTY_LIST);
        pc.setCommandLine(cmdLine.trim().split(" "));
        pc.setSystemSinks(GridUtil.logFSMntPnt + "/dsHead.out", false);


        // Set the addresses for the process
        List<UUID> addresses = new ArrayList<UUID>();
        addresses.add(GridUtil.getAddressFor(grid, network, instance + "-dsHead").getUUID());

        pc.setNetworkAddresses(addresses);
        pc.setProcessExitAction(ProcessExitAction.PARK);

        return pc;
    }

    protected ProcessConfiguration getPartitionClusterConfig(String prefix)
            throws Exception {
        String cmdLine =
                "-DauraHome=" + GridUtil.auraDistMntPnt +
                " -Dprefix=" + prefix +
                " -jar " + GridUtil.auraDistMntPnt + "/dist/aardvark.jar" +
                " /com/sun/labs/aura/aardvark/resource/partitionClusterConfig.xml" +
                " partitionClusterStarter";

        // create a configuration and set relevant properties
        ProcessConfiguration pc = getProcessConfig(GridUtil.logFSMntPnt, "pc-" + prefix + ".out", Collections.EMPTY_LIST);
        pc.setCommandLine(cmdLine.trim().split(" "));

        // Set the addresses for the process
        List<UUID> addresses = new ArrayList<UUID>();
        addresses.add(GridUtil.getAddressFor(grid, network, instance + "-part-" + prefix).getUUID());

        pc.setNetworkAddresses(addresses);
        pc.setProcessExitAction(ProcessExitAction.PARK);

        return pc;
    }

    protected ProcessConfiguration getReplicantConfig(String replicantConfig, String prefix)
            throws Exception {
        String cmdLine =
                "-Xmx3g" +
                " -DauraHome=" + GridUtil.auraDistMntPnt +
                " -DstartingDataDir=" + GridUtil.auraDistMntPnt +
                "/classifier/starting.idx" +
                " -Dprefix=" + prefix +
                " -DdataFS=/files/data/" + prefix +
                " -jar " + GridUtil.auraDistMntPnt + "/dist/aardvark.jar" +
                " " + replicantConfig +
                " replicantStarter";

        // create a configuration and set relevant properties
        List<FileSystemMountParameters> extraMounts = 
                Collections.singletonList(new FileSystemMountParameters(
                repFSMap.get(instance + "-" + prefix).getUUID(),
                "data"));
        ProcessConfiguration pc = getProcessConfig(GridUtil.logFSMntPnt, "rep-" + prefix + ".out", extraMounts);
        pc.setCommandLine(cmdLine.trim().split(" "));

        // Set the addresses for the process
        List<UUID> addresses = new ArrayList<UUID>();
        addresses.add(GridUtil.getAddressFor(grid, network, instance + "-rep-" + prefix).getUUID());

        pc.setNetworkAddresses(addresses);
        pc.setProcessExitAction(ProcessExitAction.PARK);

        // don't overlap with other replicants
        pc.setLocationConstraint(
                new ProcessRegistrationFilter.NameMatch(
                Pattern.compile(instance + ".*-rep-.*")));

        return pc;
    }

    protected ProcessConfiguration getStatServiceConfig() throws Exception {
        String cmdLine =
                "-DauraHome=" + GridUtil.auraDistMntPnt +
                " -jar " + GridUtil.auraDistMntPnt + "/dist/aardvark.jar" +
                " /com/sun/labs/aura/aardvark/resource/statServiceConfig.xml" +
                " statServiceStarter";

        // create a configuration and set relevant properties
        ProcessConfiguration pc = getProcessConfig(GridUtil.logFSMntPnt, "statService.out", Collections.EMPTY_LIST);
        pc.setCommandLine(cmdLine.trim().split(" "));

        // Set the addresses for the process
        List<UUID> addresses = new ArrayList<UUID>();
        addresses.add(GridUtil.getAddressFor(grid, network, instance + "-statSrv").getUUID());

        pc.setNetworkAddresses(addresses);
        pc.setProcessExitAction(ProcessExitAction.PARK);

        return pc;
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
