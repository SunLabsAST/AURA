
package com.sun.labs.aura.util;

import com.sun.caroline.platform.BaseFileSystemConfiguration;
import com.sun.caroline.platform.ConflictingHostNameException;
import com.sun.caroline.platform.CustomerNetworkConfiguration;
import com.sun.caroline.platform.DuplicateNameException;
import com.sun.caroline.platform.FileSystem;
import com.sun.caroline.platform.FileSystemMountParameters;
import com.sun.caroline.platform.Grid;
import com.sun.caroline.platform.GridFactory;
import com.sun.caroline.platform.HostNameBinding;
import com.sun.caroline.platform.HostNameBindingConfiguration;
import com.sun.caroline.platform.HostNameZone;
import com.sun.caroline.platform.Network;
import com.sun.caroline.platform.NetworkAddress;
import com.sun.caroline.platform.NetworkAddressAllocationException;
import com.sun.caroline.platform.NetworkAllocationException;
import com.sun.caroline.platform.ProcessConfiguration;
import com.sun.caroline.platform.ProcessExitAction;
import com.sun.caroline.platform.ProcessRegistration;
import com.sun.caroline.platform.ProcessRegistrationFilter;
import com.sun.caroline.platform.RunState;
import com.sun.caroline.platform.StorageManagementException;
import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Configures and deploys Aura components on the grid.
 * 
 */
public class GridDeploy {
    protected Grid grid = null;

    public static String GRID_URL = "https://dev.caroline.east.sun.com/";
    public static String CUST_ID  = "aura";
    public static String CUST_PW  = "corona";
    
    private HashMap<String,FileSystem> repFSMap;
    private FileSystem auraDist;
    private FileSystem logsFS;
    private String auraDistMntPnt = "/files/auraDist";
    private String logsFSMntPnt = "/files/auraLogs";
    private Network network;
    private int addrCnt = 1;
    
    private String instance = "live";
    
    //private String[] prefixCodeList = new String[] { "00", "01", "10", "11" };
    private String[] prefixCodeList = new String[] {
        "000", "001", "010", "011", "100", "101", "110", "111"
    };

    private static String usage = "GridDeploy createCode | startAura | startAardvark | createWeb";

    public static void main(String argv[]) throws Exception {
        if (argv.length == 0) {
            System.out.println("Usage: " + usage);
            return;
        }
        
        String homeDir = System.getProperty("user.home");
        File dotCaroline = new File(homeDir + "/.caroline");
        if (dotCaroline.exists()) {
            Properties props = new Properties();
            props.load(new FileInputStream(dotCaroline));
            GRID_URL = props.getProperty("gridURL");
            CUST_ID = props.getProperty("customerID");
            CUST_PW = props.getProperty("password");
        }
        
        GridDeploy gd = new GridDeploy();
        
        if (argv[0].equals("createCode")) {
            gd.createCodeInfrastructure();
        } else if (argv[0].equals("startAura")) {
            gd.createAuraInfrastructure();
            gd.createAuraProcesses();
        } else if (argv[0].equals("startAardvark")) {
            gd.createAardvarkProcesses();
        } else if (argv[0].equals("createWeb")) {
            //
            // create the resources for a glassfish install
            gd.createWebInfrastructure();
        }
    }
    
    public GridDeploy() {
        repFSMap = new HashMap<String,FileSystem>();

        try {
            System.out.println("Getting grid reference");
            grid = GridFactory.getGrid(new URL(GRID_URL), CUST_ID, CUST_PW);
        } catch (RemoteException ex) {
            Logger.getLogger("global").log(Level.SEVERE, ex.getMessage(), ex);
            System.exit(0);
        } catch (MalformedURLException ex) {
            Logger.getLogger("global").log(Level.SEVERE, ex.getMessage(), ex);
            System.exit(1);
        }
    }
    
    public void createCodeInfrastructure() throws Exception {
        //
        // sys.packages is where certain grid tools will live such as unzip
        // and the glassfish code
        getFS("sys.packages");
        auraDist = getFS(instance + "-aura.dist");
    }
    

    /**
     * Creates the infrastructure for Aura - that is, the network and the
     * file systems that it will rely on for data storage.
     * 
     * @throws java.lang.Exception
     */
    public void createAuraInfrastructure() throws Exception {
        //
        // Use createCodeInfra to get references to what should be existing
        // file systems
        createCodeInfrastructure();
        
        //
        // Set up the file systems for each replicant
        for (int i = 0; i < prefixCodeList.length; i++) {
            String currPrefix = prefixCodeList[i];
            repFSMap.put(instance + "-" + currPrefix,
                         getFS(instance + "-replicant-" + currPrefix));
        }
        
        //
        // Make a place to write all text output log files
        logsFS = getFS(instance + "-aura.logs");
        
        //
        // Set up the network that we'll use
        createAuraNetwork();
        
    }
    
    /**
     * Creates the resources needed for running a web server... basically
     * an external address
     * 
     * @throws java.lang.Exception
     */
    protected void createWebInfrastructure() throws Exception {
        NetworkAddress addr = getExternalAddressFor("www");
        System.out.println("Bound www to " + addr.getAddress());
    }
    
    
    /**
     * Assuming a fully built infrastructure, this uses the network and
     * file systems already there to start up the aura processes
     * 
     * @throws java.lang.Exception
     */
    public void createAuraProcesses() throws Exception {
        //
        // Get a reggie started up first thing
        ProcessConfiguration regConfig = getReggieConfig();
        ProcessRegistration regReg = null;
        try {
            regReg = grid.createProcessRegistration(
                    instance + "-reggie", regConfig);
        } catch (DuplicateNameException dne) {
            System.out.println("Reggie already exists, reusing");
            regReg = grid.getProcessRegistration(instance + "-reggie");
        }
        startRegistration(regReg);
        
        //
        // Next, get a data store head and start it
        ProcessConfiguration dsHeadConfig = getDataStoreHeadConfig();
        ProcessRegistration dsHeadReg = null;
        try {
            dsHeadReg = grid.createProcessRegistration(
                    instance + "-dsHead", dsHeadConfig);
        } catch (DuplicateNameException dne) {
            System.out.println("DataStoreHead already exists, reusing");
            dsHeadReg = grid.getProcessRegistration(instance + "-dsHead");
        }
        startRegistration(dsHeadReg);
        
        //
        // Now, start partition clusters for each prefix
        ProcessRegistration lastReg = null;
        for (int i = 0; i < prefixCodeList.length; i++) {
            ProcessConfiguration pcConfig =
                    getPartitionClusterConfig(prefixCodeList[i]);
            ProcessRegistration pcReg = null;
            try {
                pcReg = grid.createProcessRegistration(
                        instance + "-part-" + prefixCodeList[i], pcConfig);
            } catch (DuplicateNameException dne) {
                System.out.println("PartitionCluster-" + prefixCodeList[i] +
                        " already exists, reusing");
                pcReg = grid.getProcessRegistration(instance + "-part-" +
                        prefixCodeList[i]);
            }
            startRegistration(pcReg, false);
            lastReg = pcReg;
        }
        
        while (lastReg.getRunState() != RunState.RUNNING) {
            lastReg.waitForStateChange(1000000L);
        }
        
        //
        // Start the replicants for each prefix
        for (int i = 0; i < prefixCodeList.length; i++) {
            ProcessConfiguration repConfig =
                    getReplicantConfig(prefixCodeList[i]);
            ProcessRegistration repReg = null;
            try {
                repReg = grid.createProcessRegistration(
                        instance + "-rep-" + prefixCodeList[i], repConfig);
            } catch (DuplicateNameException dne) {
                System.out.println("Replicant-" + prefixCodeList[i] +
                        " already exists, reusing");
                repReg = grid.getProcessRegistration(instance + "-rep-" +
                        prefixCodeList[i]);
            }
            startRegistration(repReg, false);
            lastReg = repReg;
        }
        
        while (lastReg.getRunState() != RunState.RUNNING) {
            lastReg.waitForStateChange(1000000L);
        }
        
        //
        // And finally start a stat service
        ProcessConfiguration statSrvConfig = getStatServiceConfig();
        ProcessRegistration statSrvReg = null;
        try {
            statSrvReg = grid.createProcessRegistration(
                    instance + "-statSrv", statSrvConfig);
        } catch (DuplicateNameException dne) {
            System.out.println("StatService already exists, reusing");
            statSrvReg = grid.getProcessRegistration(instance + "-statSrv");
        }
        startRegistration(statSrvReg);

    }

    public void createAardvarkProcesses() throws Exception {
        //
        // Use createCodeInfra to get references to what should be existing
        // file systems and networks
        createCodeInfrastructure();
        createAuraNetwork();
        
        //
        // Make a place to write all text output log files
        logsFS = getFS(instance + "-aura.logs");

        //
        // Start the Feed Scheduler
        ProcessConfiguration feedSchedConfig = getFeedSchedulerConfig();
        ProcessRegistration feedSchedReg = null;
        try {
            feedSchedReg = grid.createProcessRegistration(
                    instance + "-feedSched", feedSchedConfig);
        } catch (DuplicateNameException dne) {
            System.out.println("FeedScheduler already exists, reusing");
            feedSchedReg = grid.getProcessRegistration(instance + "-feedSched");
        }
        startRegistration(feedSchedReg);

        //
        // Start a few feed crawlers
        ProcessRegistration lastReg = null;
        for (int i = 0; i < 6; i++) {
            ProcessConfiguration feedMgrConfig = getFeedManagerConfig(i);
            ProcessRegistration feedMgrReg = null;
            try {
                feedMgrReg = grid.createProcessRegistration(
                        instance + "-feedMgr-" + i, feedMgrConfig);
            } catch (DuplicateNameException dne) {
                System.out.println("FeedManager " + i + " already exists" +
                                   " reusing");
                feedMgrReg = grid.getProcessRegistration(
                        instance + "-feedMgr-" + i);
            }
            startRegistration(feedMgrReg, false);
            lastReg = feedMgrReg;
        }
        
        while (lastReg.getRunState() != RunState.RUNNING) {
            lastReg.waitForStateChange(1000000L);
        }
        
        //
        // Create a recommendation manager
        ProcessConfiguration recConfig = getRecommenderConfig();
        ProcessRegistration recReg = null;
        try {
            recReg = grid.createProcessRegistration(
                    instance + "-recommender", recConfig);
        } catch (DuplicateNameException dne) {
            System.out.println("Recommender already exists, reusing");
            recReg = grid.getProcessRegistration(instance + "-recommender");
        }
        startRegistration(recReg);

        //
        // And now make an Aardvark
        ProcessConfiguration aardvarkConfig = getAardvarkConfig();
        ProcessRegistration aardvarkReg = null;
        try {
            aardvarkReg = grid.createProcessRegistration(
                    instance + "-aardvark", aardvarkConfig);
        } catch (DuplicateNameException dne) {
            System.out.println("Aardvark already exists, reusing");
            aardvarkReg = grid.getProcessRegistration(instance + "-aardvark");
        }
        startRegistration(aardvarkReg);
        
    }
    
    protected ProcessConfiguration getReggieConfig() throws Exception {
        String cmdLine =
                "-Djava.security.policy=" + auraDistMntPnt + "/jini/jsk-all.policy" +
                " -jar " + auraDistMntPnt + "/jini/lib/start.jar" +
                " " + auraDistMntPnt + "/jini/nobrowse.config";
        
        // create a configuration and set relevant properties
        ProcessConfiguration pc = new ProcessConfiguration();
        pc.setCommandLine(cmdLine.trim().split(" "));
        pc.setSystemSinks(logsFSMntPnt + "/reggie.out", false);
        
        Collection<FileSystemMountParameters> mountParams = 
            new ArrayList<FileSystemMountParameters>();

        mountParams.add(
                new FileSystemMountParameters(auraDist.getUUID(), 
                                          new File(auraDistMntPnt).getName()));
        mountParams.add(
                new FileSystemMountParameters(logsFS.getUUID(),
                                          new File(logsFSMntPnt).getName()));
        pc.setFileSystems(mountParams);
        pc.setWorkingDirectory(auraDistMntPnt + "/jini");
        
        // Set the addresses for the process
        List<UUID> addresses = new ArrayList<UUID>();
        addresses.add(getAddressFor(instance + "-reggie").getUUID());
        addresses.add(getExternalAddressFor(instance + "-reggie").getUUID());
        
        pc.setNetworkAddresses(addresses);
        pc.setProcessExitAction(ProcessExitAction.DESTROY);
        
        return pc;
    }
    
    protected ProcessConfiguration getDataStoreHeadConfig() throws Exception {
        String cmdLine =
                "-DauraHome=" + auraDistMntPnt +
                " -jar " + auraDistMntPnt + "/dist/aardvark.jar" + 
                " /com/sun/labs/aura/resource/dataStoreHeadConfig.xml" +
                " dataStoreHeadStarter";
        
        // create a configuration and set relevant properties
        ProcessConfiguration pc = new ProcessConfiguration();
        pc.setCommandLine(cmdLine.trim().split(" "));
        pc.setSystemSinks(logsFSMntPnt + "/dsHead.out", false);
        
        Collection<FileSystemMountParameters> mountParams = 
            new ArrayList<FileSystemMountParameters>();

        mountParams.add(
                new FileSystemMountParameters(auraDist.getUUID(), 
                                              new File(auraDistMntPnt).getName()));
        mountParams.add(
                new FileSystemMountParameters(logsFS.getUUID(),
                                           new File(logsFSMntPnt).getName()));
        pc.setFileSystems(mountParams);
        pc.setWorkingDirectory(logsFSMntPnt);
        
        // Set the addresses for the process
        List<UUID> addresses = new ArrayList<UUID>();
        addresses.add(getAddressFor(instance + "-dsHead").getUUID());
        addresses.add(getExternalAddressFor(instance + "-dsHead").getUUID());
        
        pc.setNetworkAddresses(addresses);
        pc.setProcessExitAction(ProcessExitAction.DESTROY);
        
        return pc;
    }
    
    protected ProcessConfiguration getPartitionClusterConfig(String prefix)
            throws Exception {
        String cmdLine =
                "-DauraHome=" + auraDistMntPnt +
                " -Dprefix=" + prefix +
                " -jar " + auraDistMntPnt + "/dist/aardvark.jar" + 
                " /com/sun/labs/aura/resource/partitionClusterConfig.xml" +
                " partitionClusterStarter";
        
        // create a configuration and set relevant properties
        ProcessConfiguration pc = new ProcessConfiguration();
        pc.setCommandLine(cmdLine.trim().split(" "));
        pc.setSystemSinks(
                logsFSMntPnt + "/pc-" + prefix + ".out", false);
        
        Collection<FileSystemMountParameters> mountParams = 
            new ArrayList<FileSystemMountParameters>();

        mountParams.add(
                new FileSystemMountParameters(auraDist.getUUID(), 
                                              new File(auraDistMntPnt).getName()));
        mountParams.add(
                new FileSystemMountParameters(logsFS.getUUID(),
                                           new File(logsFSMntPnt).getName()));
        pc.setFileSystems(mountParams);
        pc.setWorkingDirectory(logsFSMntPnt);
        
        // Set the addresses for the process
        List<UUID> addresses = new ArrayList<UUID>();
        addresses.add(getAddressFor(instance + "-part-" + prefix).getUUID());
        
        pc.setNetworkAddresses(addresses);
        pc.setProcessExitAction(ProcessExitAction.DESTROY);
        
        return pc;
    }
    
    protected ProcessConfiguration getReplicantConfig(String prefix) 
            throws Exception {
        String cmdLine =
                "-DauraHome=" + auraDistMntPnt +
                " -Dprefix=" + prefix +
                " -DdataFS=/files/data/" + prefix +
                " -jar " + auraDistMntPnt + "/dist/aardvark.jar" + 
                " /com/sun/labs/aura/resource/replicantConfig.xml" +
                " replicantStarter";
        
        // create a configuration and set relevant properties
        ProcessConfiguration pc = new ProcessConfiguration();
        pc.setCommandLine(cmdLine.trim().split(" "));
        pc.setSystemSinks(
                logsFSMntPnt + "/rep-" + prefix + ".out", false);
        
        Collection<FileSystemMountParameters> mountParams = 
            new ArrayList<FileSystemMountParameters>();

        mountParams.add(
                new FileSystemMountParameters(auraDist.getUUID(), 
                                              new File(auraDistMntPnt).getName()));
        mountParams.add(
                new FileSystemMountParameters(logsFS.getUUID(),
                                           new File(logsFSMntPnt).getName()));
        mountParams.add(
                new FileSystemMountParameters(
                        repFSMap.get(instance + "-" + prefix).getUUID(),
                        "data"));
        
        pc.setFileSystems(mountParams);
        pc.setWorkingDirectory(logsFSMntPnt);
        
        // Set the addresses for the process
        List<UUID> addresses = new ArrayList<UUID>();
        addresses.add(getAddressFor(instance + "-rep-" + prefix).getUUID());
        
        pc.setNetworkAddresses(addresses);
        pc.setProcessExitAction(ProcessExitAction.DESTROY);
        
        // don't overlap with other replicants
        pc.setLocationConstraint(
                new ProcessRegistrationFilter.NameMatch(
                        Pattern.compile(instance + "-rep-*")));
        
        return pc;
    }
    
    protected ProcessConfiguration getFeedSchedulerConfig() throws Exception {
        String cmdLine =
                "-DauraHome=" + auraDistMntPnt +
                " -jar " + auraDistMntPnt + "/dist/aardvark.jar" + 
                " /com/sun/labs/aura/resource/feedSchedulerConfig.xml" +
                " feedSchedulerStarter";
        
        // create a configuration and set relevant properties
        ProcessConfiguration pc = new ProcessConfiguration();
        pc.setCommandLine(cmdLine.trim().split(" "));
        pc.setSystemSinks(logsFSMntPnt + "/feedSched.out", false);
        
        Collection<FileSystemMountParameters> mountParams = 
            new ArrayList<FileSystemMountParameters>();

        mountParams.add(
                new FileSystemMountParameters(auraDist.getUUID(), 
                                              new File(auraDistMntPnt).getName()));
        mountParams.add(
                new FileSystemMountParameters(logsFS.getUUID(),
                                           new File(logsFSMntPnt).getName()));
        pc.setFileSystems(mountParams);
        pc.setWorkingDirectory(logsFSMntPnt);
        
        // Set the addresses for the process
        List<UUID> addresses = new ArrayList<UUID>();
        addresses.add(getAddressFor(instance + "-feedSched").getUUID());
        
        pc.setNetworkAddresses(addresses);
        pc.setProcessExitAction(ProcessExitAction.DESTROY);
        
        return pc;
    }
    
    protected ProcessConfiguration getFeedManagerConfig(int n)
            throws Exception {
        String cmdLine =
                "-DauraHome=" + auraDistMntPnt +
                " -jar " + auraDistMntPnt + "/dist/aardvark.jar" + 
                " /com/sun/labs/aura/resource/feedManagerConfig.xml" +
                " feedManagerStarter";
        
        // create a configuration and set relevant properties
        ProcessConfiguration pc = new ProcessConfiguration();
        pc.setCommandLine(cmdLine.trim().split(" "));
        pc.setSystemSinks(
                logsFSMntPnt + "/feedMgr-" + n + ".out", false);
        
        Collection<FileSystemMountParameters> mountParams = 
            new ArrayList<FileSystemMountParameters>();

        mountParams.add(
                new FileSystemMountParameters(auraDist.getUUID(), 
                                              new File(auraDistMntPnt).getName()));
        mountParams.add(
                new FileSystemMountParameters(logsFS.getUUID(),
                                           new File(logsFSMntPnt).getName()));
        pc.setFileSystems(mountParams);
        pc.setWorkingDirectory(logsFSMntPnt);
        
        // Set the addresses for the process
        List<UUID> addresses = new ArrayList<UUID>();
        addresses.add(getAddressFor(instance + "-feedMgr-" + n).getUUID());
        addresses.add(getExternalAddressFor(instance + "-feedMgr-" + n).getUUID());
        
        pc.setNetworkAddresses(addresses);
        pc.setProcessExitAction(ProcessExitAction.DESTROY);
  
        // don't overlap with other replicants
        pc.setLocationConstraint(
                new ProcessRegistrationFilter.NameMatch(
                        Pattern.compile(instance + "-feedMgr-*")));

        return pc;
    }
    
    protected ProcessConfiguration getStatServiceConfig() throws Exception {
        String cmdLine =
                "-DauraHome=" + auraDistMntPnt +
                " -jar " + auraDistMntPnt + "/dist/aardvark.jar" + 
                " /com/sun/labs/aura/resource/statServiceConfig.xml" +
                " statServiceStarter";
        
        // create a configuration and set relevant properties
        ProcessConfiguration pc = new ProcessConfiguration();
        pc.setCommandLine(cmdLine.trim().split(" "));
        pc.setSystemSinks(logsFSMntPnt + "/statService.out", false);
        
        Collection<FileSystemMountParameters> mountParams = 
            new ArrayList<FileSystemMountParameters>();

        mountParams.add(
                new FileSystemMountParameters(auraDist.getUUID(), 
                                              new File(auraDistMntPnt).getName()));
        mountParams.add(
                new FileSystemMountParameters(logsFS.getUUID(),
                                           new File(logsFSMntPnt).getName()));
        pc.setFileSystems(mountParams);
        pc.setWorkingDirectory(logsFSMntPnt);
        
        // Set the addresses for the process
        List<UUID> addresses = new ArrayList<UUID>();
        addresses.add(getAddressFor(instance + "-statSrv").getUUID());
        
        pc.setNetworkAddresses(addresses);
        pc.setProcessExitAction(ProcessExitAction.DESTROY);
        
        return pc;
    }

    protected ProcessConfiguration getAardvarkConfig() throws Exception {
        String cmdLine = "-DauraHome=" + auraDistMntPnt +
                " -jar " + auraDistMntPnt + "/dist/aardvark.jar" + 
                " /com/sun/labs/aura/aardvark/resource/aardvarkConfig.xml" +
                " aardvarkStarter";
        
        // create a configuration and set relevant properties
        ProcessConfiguration pc = new ProcessConfiguration();
        pc.setCommandLine(cmdLine.trim().split(" "));
        pc.setSystemSinks(logsFSMntPnt + "/aardvark.out", false);
        
        Collection<FileSystemMountParameters> mountParams = 
            new ArrayList<FileSystemMountParameters>();

        mountParams.add(
                new FileSystemMountParameters(auraDist.getUUID(), 
                                              new File(auraDistMntPnt).getName()));
        mountParams.add(
                new FileSystemMountParameters(logsFS.getUUID(),
                                           new File(logsFSMntPnt).getName()));
        pc.setFileSystems(mountParams);
        pc.setWorkingDirectory(logsFSMntPnt);
        
        // Set the addresses for the process
        List<UUID> addresses = new ArrayList<UUID>();
        addresses.add(getAddressFor(instance + "-aardvark").getUUID());
        
        pc.setNetworkAddresses(addresses);
        pc.setProcessExitAction(ProcessExitAction.DESTROY);
        
        return pc;
    }
    
    protected ProcessConfiguration getRecommenderConfig() throws Exception {
        String cmdLine =
                "-DauraHome=" + auraDistMntPnt +
                " -jar " + auraDistMntPnt + "/dist/aardvark.jar" + 
                " /com/sun/labs/aura/resource/recommenderManagerConfig.xml" +
                " recommenderManagerStarter";

        // create a configuration and set relevant properties
        ProcessConfiguration pc = new ProcessConfiguration();
        pc.setCommandLine(cmdLine.trim().split(" "));
        pc.setSystemSinks(logsFSMntPnt + "/recommender.out", false);
        
        Collection<FileSystemMountParameters> mountParams = 
            new ArrayList<FileSystemMountParameters>();

        mountParams.add(
                new FileSystemMountParameters(auraDist.getUUID(), 
                                              new File(auraDistMntPnt).getName()));
        mountParams.add(
                new FileSystemMountParameters(logsFS.getUUID(),
                                           new File(logsFSMntPnt).getName()));
        pc.setFileSystems(mountParams);
        pc.setWorkingDirectory(logsFSMntPnt);
        
        // Set the addresses for the process
        List<UUID> addresses = new ArrayList<UUID>();
        addresses.add(getAddressFor(instance + "-recommender").getUUID());
        
        pc.setNetworkAddresses(addresses);
        pc.setProcessExitAction(ProcessExitAction.DESTROY);
        
        return pc;
    }
    
    protected void createAuraNetwork() throws Exception {
        try {
            // Try to create a customer network for the test
            network = grid.createNetwork(instance + "-auraNet", 64,
                                         new CustomerNetworkConfiguration());
            System.out.println("Created network " + network.getName());
        } catch (DuplicateNameException e) {
            // Reuse an existing network
            network = grid.getNetwork(instance + "-auraNet");
            System.out.println("Network already exists, reusing "
                    + network.getName());
        } catch (NetworkAllocationException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Create a file system, or accept an existing one with the same name
     * @param fsName
     */
    protected FileSystem getFS(String fsName) throws
            RemoteException,
            DuplicateNameException,
            StorageManagementException {
        
        BaseFileSystemConfiguration fsConfiguration = 
            new BaseFileSystemConfiguration();
        
        System.out.println("Looking for filesystem " + fsName);
        FileSystem fileSystem = grid.getFileSystem(fsName);
        
        if (fileSystem == null) {
            System.out.println("Creating filesystem " + fsName);
            fileSystem = grid.createBaseFileSystem(fsName, fsConfiguration);
        } else {
            System.out.println("Found existing filesystem " +
                    fileSystem.getName() + " with uuid: "
                    + fileSystem.getUUID());
        }
        return fileSystem;
    }
    
    /**
     * Get an address for a given hostname.  The hostname should be based
     * on the process name.  The address is allocated and a host name binding
     * is created for it.
     * 
     * @param hostName
     * @return
     * @throws java.lang.Exception
     */
    protected NetworkAddress getAddressFor(String hostName) throws Exception {
        // Allocate the internal addresses and the real services behind the
        // virtual service
        HostNameZone hnZone = grid.getInternalHostNameZone();
        NetworkAddress internalAddress = null;

        try {
            internalAddress = 
                network.allocateAddress("addr-" + hostName);
            System.out.println("Allocated internal address "
                               + internalAddress.getAddress());
        } catch (DuplicateNameException e) {
            internalAddress = network.getAddress("addr-" + hostName);
            System.out.println("Reusing address "
                               + internalAddress.getAddress());
        } catch (NetworkAddressAllocationException e) {
            System.err.println(e.getMessage());
            throw e;
        }

        addrCnt++;
        bindHostName(hnZone, internalAddress, hostName);
        return internalAddress;
    }
    
    protected NetworkAddress getExternalAddressFor(String name) throws Exception {
        // Allocate an external address for the virtual service if necessary
        NetworkAddress externalAddress = null;
        try {
            externalAddress =
                    grid.allocateExternalAddress(name + "-ext");
            System.out.println("Allocated external address " +
                    externalAddress.getAddress());
        } catch (DuplicateNameException e) {
            System.out.println("External address exists, reusing");
            externalAddress = grid.getExternalAddress(name + "-ext");
        } catch (NetworkAddressAllocationException e) {
            System.err.println(e.getMessage());
            System.exit(2);
        }
        bindHostName(grid.getExternalHostNameZone(), externalAddress, name);
        return externalAddress;
    }
    
    protected void startRegistration(ProcessRegistration reg)  throws Exception {
        startRegistration(reg, true);
    }
    
    protected void startRegistration(final ProcessRegistration reg, boolean wait) throws Exception {
        Thread starter = new Thread() {
            public void run() {
                try {
                    reg.start(true);
                } catch (Exception e) {
                    System.out.println("Registration start failed " + e.getMessage());
                }

                System.out.println("Registration " + reg.getName()
                        + " started");
            }
        };
        starter.start();
        if (wait) {
            while (reg.getRunState() != RunState.RUNNING) {
                reg.waitForStateChange(1000000L);
            }
        }
    }
    
    protected void bindHostName(HostNameZone hnZone,
                                NetworkAddress addr,
                                String hostName) throws Exception {
        HostNameBinding binding = null;
        try {
            HostNameBindingConfiguration hnbConf =
                    new HostNameBindingConfiguration();
            Collection<UUID> addrs = new ArrayList<UUID>();
            addrs.add(addr.getUUID());
            hnbConf.setAddresses(addrs);
            hnbConf.setHostName(hostName);
            binding =
                  hnZone.createBinding(hnbConf.getHostName(), hnbConf);
        } catch (DuplicateNameException dne) {
            binding = hnZone.getBinding(hostName);
            System.out.println("Host name \"" + hostName +
                "\" has already been defined as " +
                binding.getConfiguration().getAddresses().toArray()[0]);
        } catch (ConflictingHostNameException chne) {
            binding = hnZone.getBinding(hostName);
            System.out.println("Host name \"" + hostName +
                "\" has already been defined as " +
                binding.getConfiguration().getAddresses().toArray()[0]);
        }

    }
    
    protected void completelyDestroyReg(String regName) {
        
    }

}
