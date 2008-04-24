
package com.sun.labs.aura.util;

import com.sun.caroline.platform.BaseFileSystemConfiguration;
import com.sun.caroline.platform.ConflictingHostNameException;
import com.sun.caroline.platform.CustomerNetworkConfiguration;
import com.sun.caroline.platform.DuplicateNameException;
import com.sun.caroline.platform.DynamicNatConfiguration;
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
import com.sun.caroline.platform.NetworkConfiguration;
import com.sun.caroline.platform.NetworkSetting;
import com.sun.caroline.platform.ProcessConfiguration;
import com.sun.caroline.platform.ProcessExitAction;
import com.sun.caroline.platform.ProcessRegistration;
import com.sun.caroline.platform.ProcessRegistrationFilter;
import com.sun.caroline.platform.RunState;
import com.sun.caroline.platform.StorageManagementException;
import com.sun.labs.aura.datastore.impl.DSBitSet;
import com.sun.org.apache.xalan.internal.xsltc.cmdline.getopt.GetOpt;
import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
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
    private FileSystem schedFS;
    private String auraDistMntPnt = "/files/auraDist";
    private String logsFSMntPnt = "/files/auraLogs";
    private Network network;
    private int addrCnt = 1;
    
    private String instance = "live";
    
    //private String[] prefixCodeList = new String[] { "00", "01", "10", "11" };
//    private String[] prefixCodeList = new String[] { "0", "1"};
    private static String[] prefixCodeList = new String[] {
        "0000", "0001", "0010", "0011", "0100", "0101", "0110", "0111",
        "1000", "1001", "1010", "1011", "1100", "1101", "1110", "1111",
    };

    private static String usage = "GridDeploy [-n numParts] createCode | startAura | startSlowAura |" +
            " stopAura | startAardvark | \n" +
            " stopAardvark | createWeb | reindexData | reindexChunk prefix |\n" +
            " diskUsage | startAardvarkNC";

    public static void main(String argv[]) throws Exception {
        if (argv.length == 0) {
            System.out.println("Usage: " + usage);
            return;
        }
        
        String globalParams = "n:";
        GetOpt gopt = new GetOpt(argv, globalParams);
        int numParts = 0;
        int c;
        while((c = gopt.getNextOption()) != -1) {
            switch(c) {
                case 'n':
                    numParts = Integer.parseInt(gopt.getOptionArg());
                    break;
            }
        }
        
        if (numParts != 0) {
            if (Integer.highestOneBit(numParts) != Integer.lowestOneBit(numParts)) {
                System.out.println("n must be a power of two");
            }
            int numBits = Integer.toString(numParts, 2).length() - 1;
            prefixCodeList = new String[numParts];
            for (int i = 0; i < numParts; i++) {
                DSBitSet prefixBits = DSBitSet.parse(i);
                prefixBits.setPrefixLength(numBits);
                prefixCodeList[i] = prefixBits.toString();
            }
        }
        
        //
        // Set up cmdArgs to be an argv array of args after the command name
        String cmdArgs[] = gopt.getCmdArgs();
        if (cmdArgs.length == 0) {
            System.out.println("Usage: " + usage);
            return;
        }
        String cmd = cmdArgs[0];
        String[] tmp = new String[cmdArgs.length - 1];
        for (int i = 1; i < cmdArgs.length; i++) {
            tmp[i - 1] = cmdArgs[i];
        }
        cmdArgs = tmp;

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
        
        if (cmd.equals("createCode")) {
            gd.createCodeInfrastructure();
        } else if (cmd.equals("startAura")) {
            gd.createAuraInfrastructure();
            gd.createAuraProcesses();
        } else if (cmd.equals("startSlowAura")) {
            gd.createAuraInfrastructure();
            gd.createSlowAuraProcesses();
        } else if (cmd.equals("startAardvark")) {
            gd.createAardvarkProcesses();
        } else if (cmd.equals("createWeb")) {
            //
            // create the resources for a glassfish install
            gd.createWebInfrastructure();
        } else if (cmd.equals("reindexData")) {
            gd.createAuraInfrastructure();
            gd.createReindexerProcesses();
        } else if(cmd.equals("reindexChunk")) {
            gd.createAuraInfrastructure();
            gd.createReindexerProcess(cmdArgs[0]);
        } else if (cmd.equals("splitDB")) {
            GetOpt opt = new GetOpt(cmdArgs, "h:s:");
            int numSplits = 0;
            String target = null;
            while((c = opt.getNextOption()) != -1) {
                switch(c) {
                    case 's':
                        numSplits = Integer.parseInt(opt.getOptionArg());
                        break;
                    case 'h':
                        target = opt.getOptionArg();
                        break;
                }
            }
            if (numSplits == 0) {
                System.out.println("Usage: splitDB -s numSplits [-h justOneHash]");
                return;
            }
            gd.createSplitDBProcesses(numSplits, target);

        } else if(cmd.equals("stopAura")) {
            gd.stopAuraProcesses();
        } else if(cmd.equals("stopAardvark")) {
            gd.stopAardvarkProcesses();
        } else if(cmd.equals("diskUsage")) {
            gd.createCodeInfrastructure();
            gd.createAuraFilesystems();
            gd.getDiskUsage();
        } else if(cmd.equals("startAardvarkNC")) {
            gd.createAardvarkNCProcesses();
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
        
        createAuraFilesystems();
        
        //
        // Set up the network that we'll use
        createAuraNetwork();
        
    }
    
    public void createAuraFilesystems() throws Exception {
 
        //
        // Make a place to write all text output log files
        logsFS = getFS(instance + "-aura.logs");
        
        //
        // Make a place for the scheduler to cache stuff
        schedFS = getFS(instance + "-sched");
        
        //
        // Set up the file systems for each replicant
        for(int i = 0; i < prefixCodeList.length; i++) {
            String currPrefix = prefixCodeList[i];
            repFSMap.put(instance + "-" + currPrefix,
                    getFS(getReplicantName(currPrefix)));
        }
    }
    
    public void getDiskUsage() throws Exception {
        long total = logsFS.getMetrics().getSpaceUsed() +
                auraDist.getMetrics().getSpaceUsed();
        for(Map.Entry<String,FileSystem> e : repFSMap.entrySet()) {
            total += e.getValue().getMetrics().getSpaceUsed();
        }
        
        System.out.printf("Total usage: %.2fGB\n", (total / (1000 * 1000 * 1000.0)));
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

    public String getReggieName() {
        return instance + "-reggie";
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
    
    private ProcessRegistration createProcess(String name, ProcessConfiguration config) throws Exception {
        ProcessRegistration reg = null;
        try {
            reg = grid.createProcessRegistration(name, config);
        } catch(DuplicateNameException dne) {
            System.out.println(name + " already exists, reusing");
            reg = grid.getProcessRegistration(name);
        }
        return reg;
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
        ProcessRegistration regReg = createProcess(getReggieName(), getReggieConfig());
        startRegistration(regReg);
        
        //
        // Next, get a data store head and start it
        ProcessRegistration dsHeadReg = createProcess(getDataStoreHeadName(), getDataStoreHeadConfig());
        startRegistration(dsHeadReg);
        
        //
        // Now, start partition clusters for each prefix
        ProcessRegistration lastReg = null;
        for (int i = 0; i < prefixCodeList.length; i++) {
            ProcessRegistration pcReg = createProcess(getPartitionName(prefixCodeList[i]),
                    getPartitionClusterConfig(prefixCodeList[i]));
            startRegistration(pcReg, false);
            lastReg = pcReg;
        }
        
        while (lastReg.getRunState() != RunState.RUNNING) {
            lastReg.waitForStateChange(1000000L);
        }
        
        //
        // Start the replicants for each prefix
        for (int i = 0; i < prefixCodeList.length; i++) {
            ProcessRegistration repReg = createProcess(getReplicantName(prefixCodeList[i]), getReplicantConfig(prefixCodeList[i]));
            startRegistration(repReg, false);
            lastReg = repReg;
        }
        
        while (lastReg.getRunState() != RunState.RUNNING) {
            lastReg.waitForStateChange(1000000L);
        }
        
        //
        // And finally start a stat service
        ProcessRegistration statSrvReg = createProcess(getStatServiceName(), getStatServiceConfig());
        startRegistration(statSrvReg);

    }
    
    /**
     * Assuming a fully built infrastructure, this uses the network and
     * file systems already there to start up the aura processes
     * 
     * @throws java.lang.Exception
     */
    public void createSlowAuraProcesses() throws Exception {
        //
        // Get a reggie started up first thing
        ProcessRegistration regReg = createProcess(getReggieName(), getReggieConfig());
        startRegistration(regReg);
        
        //
        // Next, get a data store head and start it
        ProcessRegistration dsHeadReg = createProcess(getDataStoreHeadName(), getDataStoreHeadConfig());
        startRegistration(dsHeadReg);
        
        //
        // Now, start partition clusters for each prefix
        ProcessRegistration lastReg = null;
        for (int i = 0; i < prefixCodeList.length; i++) {
            ProcessRegistration pcReg = createProcess(getPartitionName(prefixCodeList[i]),
                    getPartitionClusterConfig(prefixCodeList[i]));
            startRegistration(pcReg, false);
            lastReg = pcReg;
        }
        
        while (lastReg.getRunState() != RunState.RUNNING) {
            lastReg.waitForStateChange(1000000L);
        }
        
        //
        // Start the replicants for each prefix
        for (int i = 0; i < prefixCodeList.length; i++) {
            ProcessRegistration repReg = createProcess(getReplicantName(prefixCodeList[i]), getSlowDumpReplicantConfig(prefixCodeList[i]));
            startRegistration(repReg, false);
            lastReg = repReg;
        }
        
        while (lastReg.getRunState() != RunState.RUNNING) {
            lastReg.waitForStateChange(1000000L);
        }
        
        //
        // And finally start a stat service
        ProcessRegistration statSrvReg = createProcess(getStatServiceName(), getStatServiceConfig());
        startRegistration(statSrvReg);

    }
    
    /**
     * Issues a gentle shutdown for a process.  The resulting process registration
     * should be added to a queue and waitForFinish should be called with that queue.
     * This allows us to quickly start the termination of a number of registrations.
     * @param name
     * @return
     * @throws java.lang.Exception
     */
    private ProcessRegistration stopProcess(String name) throws Exception {
         ProcessRegistration reg = grid.getProcessRegistration(name);
        if(reg != null) {
            System.out.println("Stopping: " + reg);
            reg.shutdownGently(true, 1000);
        } else {
            System.out.println("No registration for " + name + " to stop");
        }
        return reg;
    }
    
    private void waitForFinish(Queue<ProcessRegistration> q)
            throws Exception {
        waitForFinish(q, 600000);
    }
    
    /**
     * Waits for the queue of processes to finish.
     * @param q a queue of registrations that we're interested in.
     * @param timeout how long we should wait before forcefully killing processes.
     * @throws java.lang.Exception
     */
    private void waitForFinish(Queue<ProcessRegistration> q, long timeout) throws Exception {
        long finish = timeout + System.currentTimeMillis();
        int n = 0;
        while(q.size() > 0) {
            ProcessRegistration reg = q.poll();
            
            if(reg == null) {
                continue;
            }
            
            //
            // If it's not done, then put it back on the queue.
            if(reg.getProcessOutcome() == null || reg.getRunState() != RunState.NONE) {
                System.out.println(reg + " rs: " + reg.getRunState() + " ro: " +
                        reg.getProcessOutcome());
                q.offer(reg);
                Thread.sleep(500);
            }
            
            if(System.currentTimeMillis() > finish) {
                break;
            }
            
        }
        
        while(q.size() > 0) {
            ProcessRegistration reg = q.poll();
            if(reg != null) {
                reg.shutdownForcefully(true);
            }
        }
    }
    
    public void stopAuraProcesses() throws Exception {
        Queue<ProcessRegistration> q = new LinkedList<ProcessRegistration>();
        q.add(stopProcess(getStatServiceName()));
        
        for(int i = 0; i < prefixCodeList.length; i++) {
            q.add(stopProcess(getPartitionName(prefixCodeList[i])));
        }

        //
        // Start the replicants for each prefix
        for(int i = 0; i < prefixCodeList.length; i++) {
            q.add(stopProcess(getReplicantName(prefixCodeList[i])));
        }
        
        q.add(stopProcess(getDataStoreHeadName()));
        q.add(stopProcess(getReggieName()));
        
        waitForFinish(q);
    }
    
    public String getRIName(String prefix) {
        return instance + "-reindex-" + prefix;
    }
    
    public void createReindexerProcesses() throws Exception {
        //
        // Start the replicants for each prefix
        for(int i = 0; i < prefixCodeList.length; i++) {
            createReindexerProcess(prefixCodeList[i]);
        }
    }
    
    public void createReindexerProcess(String prefix) throws Exception {
        //
        // Start the replicants for each prefix
        ProcessRegistration reindexReg = createProcess(getRIName(prefix),
                getReindexerConfig(prefix));
        startRegistration(reindexReg, false);
    }

    public void createSplitDBProcesses(int numSplits, String targetPart)
            throws Exception {
        if (targetPart != null) {
            prefixCodeList = new String[] {targetPart};
        }
        for (String prefix : prefixCodeList) {
            ProcessRegistration splitReg = createProcess("live-split-" + prefix,
                    getSplitDBConfig(numSplits, prefix));
            startRegistration(splitReg, false);
        }
    }
    
    public String getFMName(int n) {
        return instance + "-feedMgr-" + n;
    }
    
    public String getRecName() {
        return instance + "-recommender";
    }
    
    public String getSchedName() {
        return instance + "-feedSched";
    }
    
    public String getAAName() {
        return instance + "-aardvark";
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
        schedFS = getFS(instance + "-sched");

        //
        // Start the Feed Scheduler
        NetworkAddress crawlerNat = getExternalAddressFor("feedMgrNat");
        ProcessRegistration feedSchedReg = 
                createProcess(getSchedName(), getFeedSchedulerConfig());
        
        UUID internal = feedSchedReg.getRegistrationConfiguration()
                .getNetworkAddresses().iterator().next();
        createNAT(crawlerNat.getUUID(), internal, "feedSched");
        startRegistration(feedSchedReg);

        //
        // Start a few feed crawlers
        ProcessRegistration lastReg = null;

        for (int i = 0; i < 6; i++) {
            ProcessConfiguration feedMgrConfig = getFeedManagerConfig(i);
            ProcessRegistration feedMgrReg = createProcess(getFMName(i), feedMgrConfig);
            
            //
            // Make a dynamic NAT for this process config
            ProcessConfiguration pc = feedMgrConfig;
            internal = pc.getNetworkAddresses().iterator().next();
            createNAT(crawlerNat.getUUID(), internal, "feedMgr-" + i);
            startRegistration(feedMgrReg, false);
            lastReg = feedMgrReg;
        }
        
        while (lastReg.getRunState() != RunState.RUNNING) {
            lastReg.waitForStateChange(1000000L);
        }
        
        //
        // Create a recommendation manager
        ProcessRegistration recReg = createProcess(getRecName(), getRecommenderConfig());
        startRegistration(recReg);

        //
        // And now make an Aardvark
        ProcessRegistration aardvarkReg = createProcess(getAAName(), getAardvarkConfig());
        startRegistration(aardvarkReg);
        
    }

    public void createAardvarkNCProcesses() throws Exception {
        //
        // Use createCodeInfra to get references to what should be existing
        // file systems and networks
        createCodeInfrastructure();
        createAuraNetwork();
        
        //
        // Make a place to write all text output log files
        logsFS = getFS(instance + "-aura.logs");


        //
        // Create a recommendation manager
        ProcessRegistration recReg = createProcess(getRecName(), getRecommenderConfig());
        startRegistration(recReg);

        //
        // And now make an Aardvark
        ProcessRegistration aardvarkReg = createProcess(getAAName(), getAardvarkConfig());
        startRegistration(aardvarkReg);
        
    }

    
    public void stopAardvarkProcesses() throws Exception {
        Queue<ProcessRegistration> q = new LinkedList<ProcessRegistration>();
        q.add(stopProcess(getAAName()));
        for(int i = 0; i < 6; i++) {
            q.add(stopProcess(getFMName(i)));
        }

        q.add(stopProcess(getSchedName()));
        q.add(stopProcess(getRecName()));
        waitForFinish(q);
    }
    
    protected ProcessConfiguration getReggieConfig() throws Exception {
        String cmdLine =
                "-Djava.security.policy=" + auraDistMntPnt + "/jini/jsk-all.policy" +
                " -Djava.util.logging.config.file=" + auraDistMntPnt + "/jini/logging.properties" +
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
        //addresses.add(getExternalAddressFor(instance + "-reggie").getUUID());
        
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
        //addresses.add(getExternalAddressFor(instance + "-dsHead").getUUID());
        
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
                "-Xmx3g"+
                " -DauraHome=" + auraDistMntPnt +
                " -DstartingDataDir=" + auraDistMntPnt + "/classifier/starting.idx" +
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
                        Pattern.compile(instance + ".*-rep-.*")));
        
        return pc;
    }
    
    protected ProcessConfiguration getSlowDumpReplicantConfig(String prefix) 
            throws Exception {
        String cmdLine =
                "-Xmx3g"+
                " -DauraHome=" + auraDistMntPnt +
                " -DstartingDataDir=" + auraDistMntPnt + "/classifier/starting.idx" +
                " -Dprefix=" + prefix +
                " -DdataFS=/files/data/" + prefix +
                " -jar " + auraDistMntPnt + "/dist/aardvark.jar" + 
                " /com/sun/labs/aura/resource/replicantSlowDumpConfig.xml" +
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
                        Pattern.compile(instance + ".*-rep-.*")));
        
        return pc;
    }

    protected ProcessConfiguration getReindexerConfig(String prefix)
            throws Exception {
        String cmdLine =
                "-Xmx2g" + 
                " -DauraHome=" + auraDistMntPnt +
                " -DstartingDataDir=" + auraDistMntPnt +
                "/classifier/starting.idx" +
                " -Dprefix=" + prefix +
                " -DdataFS=/files/data/" + prefix +
                " -cp " + auraDistMntPnt + "/dist/aardvark.jar" +
                ":" + auraDistMntPnt + "/dist/lib/ktsearch.jar" +
                ":" + auraDistMntPnt + "/dist/lib/LabsUtil.jar" +
                " com.sun.labs.aura.util.Reindexer" +
                " -d /files/data/" + prefix + "/reindex.idx" +
                " -b /files/data/" + prefix + "/db" +
                " -o /files/data/" + prefix + "/itemIndex.idx";
 
        // create a configuration and set relevant properties
        ProcessConfiguration pc = new ProcessConfiguration();
        pc.setCommandLine(cmdLine.trim().split(" "));
        pc.setSystemSinks(
                logsFSMntPnt + "/reindex-" + prefix + ".out", false);

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

        pc.setProcessExitAction(ProcessExitAction.DESTROY);

        // don't overlap with other replicants
        pc.setLocationConstraint(
                new ProcessRegistrationFilter.NameMatch(
                Pattern.compile(instance + ".*-reindex-.*")));

        return pc;
    }

    protected ProcessConfiguration getSplitDBConfig(int numSplits,
            String srcPrefix) throws Exception {
        String cmdLine = "-Xmx2G" +
                " -cp " + auraDistMntPnt + "/dist/aardvark.jar" +
                ":" + auraDistMntPnt + "/dist/lib/ktsearch.jar" +
                " com.sun.labs.aura.util.DBSplitter" +
                " -n " + numSplits +
                " -h " + srcPrefix +
                " -p " + instance + "-replicant-";
        // create a configuration and set relevant properties
        ProcessConfiguration pc = new ProcessConfiguration();
        pc.setCommandLine(cmdLine.trim().split(" "));
        pc.setSystemSinks(
                logsFSMntPnt + "/split-" + srcPrefix + ".out", false);

        Collection<FileSystemMountParameters> mountParams =
                new ArrayList<FileSystemMountParameters>();

        auraDist = getFS(instance + "-aura.dist");
        mountParams.add(
                new FileSystemMountParameters(auraDist.getUUID(),
                new File(auraDistMntPnt).getName()));
        logsFS = getFS(instance + "-aura.logs");
        mountParams.add(
                new FileSystemMountParameters(logsFS.getUUID(),
                new File(logsFSMntPnt).getName()));
        FileSystem srcFS = getFS(getReplicantName(srcPrefix));
        mountParams.add(
                new FileSystemMountParameters(
                srcFS.getUUID(),
                getReplicantName(srcPrefix)));
        // mount all the split file systems
        String[] newPrefixes = DBSplitter.getNewPrefixes(numSplits, srcPrefix);
        for (String prefix : newPrefixes) {
            FileSystem fs = getFS(getReplicantName(prefix));
            mountParams.add(
                    new FileSystemMountParameters(
                    fs.getUUID(),
                    getReplicantName(prefix)));
        }

        pc.setFileSystems(mountParams);
        pc.setWorkingDirectory("/files");

        pc.setProcessExitAction(ProcessExitAction.DESTROY);

        // don't overlap with other replicants
        pc.setLocationConstraint(
                new ProcessRegistrationFilter.NameMatch(
                Pattern.compile(instance + ".*-splitter-.*")));

        return pc;
    }
    
    protected ProcessConfiguration getFeedSchedulerConfig() throws Exception {
        String cmdLine =
                "-Xmx2g" +
                " -DauraHome=" + auraDistMntPnt +
                " -DcacheDir=/files/cache" +
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
        mountParams.add(
                new FileSystemMountParameters(schedFS.getUUID(), "cache"));
        
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
        //addresses.add(getExternalAddressFor(instance + "-feedMgr-" + n).getUUID());
        
        pc.setNetworkAddresses(addresses);
        pc.setProcessExitAction(ProcessExitAction.DESTROY);
  
        // don't overlap with other replicants
        pc.setLocationConstraint(
                new ProcessRegistrationFilter.NameMatch(
                        Pattern.compile(instance + ".*-feedMgr-.*")));

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
            network = grid.createNetwork(instance + "-auraNet", 512,
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
        
        //System.out.println("Looking for filesystem " + fsName);
        FileSystem fileSystem = grid.getFileSystem(fsName);
        
        if (fileSystem == null) {
            System.out.println("Creating filesystem " + fsName);
            fileSystem = grid.createBaseFileSystem(fsName, fsConfiguration);
        } else {
            System.out.println("Found existing filesystem " +
                    fileSystem.getName() /* + " with uuid: "
                    + fileSystem.getUUID()*/);
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
    
    protected void createNAT(UUID external, UUID internal, String name)
            throws Exception {
        NetworkConfiguration netConf =
                new DynamicNatConfiguration(external,
                                            internal);
        try {
            grid.createNetworkSetting(instance + "-" + name + "-nat",
                                      netConf);
        } catch (DuplicateNameException dne) {
            NetworkSetting ns = grid.getNetworkSetting(instance +
                    "-" + name + "-nat");
            ns.changeConfiguration(netConf);
        }
    }
    
    protected void completelyDestroyReg(String regName) {
        
    }

}
