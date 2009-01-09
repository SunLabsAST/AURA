/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.grid.util;

import com.sun.caroline.platform.BaseFileSystem;
import com.sun.caroline.platform.BaseFileSystemConfiguration;
import com.sun.caroline.platform.ConflictingHostNameException;
import com.sun.caroline.platform.CustomerNetworkConfiguration;
import com.sun.caroline.platform.DuplicateNameException;
import com.sun.caroline.platform.DynamicNatConfiguration;
import com.sun.caroline.platform.FileSystem;
import com.sun.caroline.platform.FileSystemMountParameters;
import com.sun.caroline.platform.Grid;
import com.sun.caroline.platform.HostNameBinding;
import com.sun.caroline.platform.HostNameBindingConfiguration;
import com.sun.caroline.platform.HostNameZone;
import com.sun.caroline.platform.Network;
import com.sun.caroline.platform.NetworkAddress;
import com.sun.caroline.platform.NetworkAddressAllocationException;
import com.sun.caroline.platform.NetworkConfiguration;
import com.sun.caroline.platform.NetworkSetting;
import com.sun.caroline.platform.ProcessConfiguration;
import com.sun.caroline.platform.ProcessExitAction;
import com.sun.caroline.platform.ProcessRegistration;
import com.sun.caroline.platform.RunState;
import com.sun.caroline.platform.StartFailureException;
import com.sun.caroline.platform.StorageManagementException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A set of basic utilities for grid deployment.
 */
public class GridUtil {

    Logger log = Logger.getLogger("com.sun.labs.aura.grid.GridUtil");

    public final int RETRIES = 1;

    private Grid grid;

    private String instance;

    private Network network;

    private Queue<ProcessRegistration> stopped;

    protected FileSystem auraDist;

    protected FileSystem auraCache;

    protected FileSystem auraLogs;

    public GridUtil(Grid grid, String instance) throws Exception {
        this.grid = grid;
        this.instance = instance;
        stopped = new LinkedList<ProcessRegistration>();
        createAuraNetwork();
        auraDist = getAuraDistFS();
        auraCache = getAuraCacheFS();
        auraLogs = getAuraLogFS();
    }

    public Grid getGrid() {
        return grid;
    }

    public Network getNetwork() {
        return network;
    }

    /**
     * Look up an existing registration by name
     * 
     * @param regName the name to find
     * @return the process registration or null if there is no
     *         registration with the given name
     */
    public ProcessRegistration lookupProcessRegistration(String regName) {
        String processName = String.format("%s-%s", instance, regName);
        try {
            return grid.getProcessRegistration(processName);
        } catch (RemoteException e) {
            return null;
        }
    }
    
    /**
     * Gets a basic process configuration 
     * @param cwd the working directory for the configuration
     * @param logName the name to log to.
     * @return a process configuration.
     */
    public ProcessConfiguration getProcessConfig(
            String type,
            String[] cmdLine,
            String logName) throws Exception {
        return getProcessConfig(type, cmdLine, logName, null, false);
    }

    /**
     * Gets a basic process configuration 
     * @param cwd the working directory for the configuration
     * @param logName the name to log to.
     * @return a process configuration.
     */
    public ProcessConfiguration getProcessConfig(
            String[] cmdLine,
            String logName) throws Exception {
        return getProcessConfig(null, cmdLine, logName, null, false);
    }

    /**
     * 
     * Gets a basic process configuration 
     * @param cwd the working directory for the configuration
     * @param logName the name to log to.
     * @param extraMounts extra mount points to give to the process configuration.
     * @return a process configuration.
     */
    public ProcessConfiguration getProcessConfig(
            String type,
            String[] cmdLine,
            String logName,
            Collection<FileSystemMountParameters> extraMounts) throws Exception {
        return getProcessConfig(type, cmdLine, logName, extraMounts, false);
    }

    /**
     * 
     * Gets a basic process configuration 
     * @param cwd the working directory for the configuration
     * @param logName the name to log to.
     * @param extraMounts extra mount points to give to the process configuration.
     * @param appendOutput if <code>true</code>, then append output to the existing
     * output files.
     * @return a process configuration.
     */
    public ProcessConfiguration getProcessConfig(
            String type, String[] cmdLine,
            String logName,
            Collection<FileSystemMountParameters> extraMounts,
            boolean appendOutput) throws Exception {
        ProcessConfiguration pc = new ProcessConfiguration();
        pc.setSystemSinks(String.format("%s/other/%s.out", logFSMntPnt,
                logName), appendOutput);

        //
        // Every body will get dist, log and cache filesystems.
        Collection<FileSystemMountParameters> mountParams =
                new ArrayList<FileSystemMountParameters>();
        mountParams.add(
                new FileSystemMountParameters(auraDist.getUUID(),
                new File(auraDistMntPnt).getName()));
        mountParams.add(
                new FileSystemMountParameters(auraLogs.getUUID(),
                new File(logFSMntPnt).getName()));
        mountParams.add(
                new FileSystemMountParameters(auraCache.getUUID(),
                new File(cacheFSMntPnt).getName()));
        if(extraMounts != null) {
            mountParams.addAll(extraMounts);
        }
        pc.setFileSystems(mountParams);
        pc.setWorkingDirectory(auraDistMntPnt);
        pc.setCommandLine(cmdLine);

        // Set the addresses for the process
        List<UUID> addresses = new ArrayList<UUID>();
        addresses.add(getAddressFor(logName).getUUID());
        pc.setNetworkAddresses(addresses);

        Map<String, String> md = getMetaData();
        md.put("name", logName);
        if(type != null) {
            md.put("type", type);
        }
        pc.setMetadata(md);

        //
        // When things die, we want them to restart!
        pc.setProcessExitAction(ProcessExitAction.RESTART);
        return pc;
    }

    private Map<String, String> getMetaData() {
        Map<String, String> ret = new HashMap<String, String>();
        ret.put("instance", instance);
        return ret;
    }

    /**
     * Creates a process registration on a grid, reusing an on-grid registration
     * if one exists.
     * @param name the name of the registration
     * @param config the configuration for the process
     * @return the on-grid process registration.
     * @throws java.lang.Exception if anything goes wrong creating the registration.
     */
    public ProcessRegistration createProcess(String name,
            ProcessConfiguration config) throws Exception {
        ProcessRegistration reg = null;
        String processName = String.format("%s-%s", instance, name);
        try {
            reg = grid.createProcessRegistration(processName, config);
            log.info("Created process registration: " + processName);
        } catch(DuplicateNameException dne) {
            log.fine("ProcessRegistration: " + name + " already exists, reusing");
            reg = grid.getProcessRegistration(processName);
            if(reg == null) {
                throw new NullPointerException("Failed to retreive existing registration: " +
                        name);
            }
            
            //
            // Make sure this registration is using the config passed in.
            reg.changeConfiguration(config);
        }
        return reg;
    }

    public void startRegistration(ProcessRegistration reg) throws Exception {
        startRegistration(reg, true);
    }

    public void startRegistration(final ProcessRegistration reg, boolean wait)
            throws Exception {

        if(reg.getRunState() == RunState.RUNNING) {
            log.fine(String.format("Registration %s is already running", reg.getName()));
            return;
        }
        
        Thread starter = new Thread() {

            public void run() {
                int tries = 0;
                Exception startException = null;
                //
                // Workaround for a mount problem.
                while(tries < RETRIES) {
                    tries++;
                    try {
                        reg.start(true);
                        startException = null;
                        break;
                    } catch(StartFailureException sfe) {
                        startException = sfe;
                        continue;
                    } catch(Exception e) {
                        startException = e;
                        break;
                    }
                }

                if(startException != null) {
                    log.log(Level.SEVERE, "Error starting service: " + reg,
                            startException);
                } else {
                    log.fine(String.format(
                            "Registration %s started after %d tries", reg.
                            getName(), tries));
                }
            }
        };
        starter.start();
        if(wait) {
            while(reg.getRunState() != RunState.RUNNING) {
                reg.waitForStateChange(1000000L);
            }
        }
    }

    /**
     * Issues a gentle shutdown for a process.  The resulting process registration
     * should be added to a queue and waitForFinish should be called with that queue.
     * This allows us to quickly start the termination of a number of registrations.
     * @param name
     * @return
     * @throws java.lang.Exception
     */
    public ProcessRegistration stopProcess(String name) throws Exception {
        ProcessRegistration reg = grid.getProcessRegistration(String.format(
                "%s-%s", instance, name));
        if(reg != null) {
            stopProcess(reg);
        } else {
            log.info("No registration for " + name + " to stop");
        }
        return reg;
    }
    
    public void stopProcess(ProcessRegistration reg) throws Exception {
        log.fine("Stopping: " + reg);

        //
        // We were asked to stop the process, so we can't just shut it down,
        // because they're set to restart.  So, first we need to change the 
        // process config to park the process.
        ProcessConfiguration pc = reg.getIncarnationConfiguration();
        pc.setProcessExitAction(ProcessExitAction.PARK);
        reg.changeConfiguration(pc);
        reg.shutdownGently(true, 1000);
        stopped.add(reg);
    }

    public void destroyRegistration(String name) throws Exception {
        ProcessRegistration reg = grid.getProcessRegistration(String.format(
                "%s-%s", instance, name));
        if(reg != null) {
            log.fine("Destroying: " + reg);
            reg.destroy(100000);
        } else {
            log.fine("No registration for " + name + " to destroy");
        }
    }

    public void waitForFinish()
            throws Exception {
        waitForFinish(600000);
    }

    /**
     * Waits for the queue of processes to finish.
     * @param timeout how long we should wait before forcefully killing processes.
     * @throws java.lang.Exception
     */
    public void waitForFinish(long timeout)
            throws Exception {
        long finish = timeout + System.currentTimeMillis();
        int n = 0;
        log.info(String.format("Watiing for %d registrations", stopped.size()));
        while(stopped.size() > 0) {
            ProcessRegistration reg = stopped.poll();

            if(reg == null) {
                continue;
            }

            reg.refresh();

            //
            // If it's not done, then put it back on the queue.
            if(reg.getProcessOutcome() == null || reg.getRunState() !=
                    RunState.NONE) {
                stopped.offer(reg);
                Thread.sleep(500);
            }

            if(System.currentTimeMillis() > finish) {
                break;
            }

        }

        while(stopped.size() > 0) {
            ProcessRegistration reg = stopped.poll();
            if(reg != null) {
                reg.shutdownForcefully(true);
            }
        }
    }

    /**
     * Gets a file system on the grid, creating it if necessary.
     * @param fsName the name of the file system to create.
     * @return a fileystem on the grid.
     */
    public FileSystem getFS(String fsName) throws
            RemoteException,
            StorageManagementException {
        return getFS(fsName, true);
    }

    /**
     * Create a file system, or accept an existing one with the same name
     * @param fsName
     */
    public FileSystem getFS(String fsName, boolean allowCreate) throws
            RemoteException,
            StorageManagementException {

        BaseFileSystemConfiguration fsConfiguration =
                new BaseFileSystemConfiguration();

        fsName = instance + "-" + fsName;

        FileSystem fileSystem = grid.getFileSystem(fsName);

        if(fileSystem == null) {
            if(allowCreate) {
                try {
                    fileSystem = grid.createBaseFileSystem(fsName,
                            fsConfiguration);
                    log.info("Created filesystem " + fsName);
                } catch(DuplicateNameException dne) {
                    //
                    // A filesystem could sneak in between the check and create
                    // calls, so we better deal with that here.
                    log.fine("Found existing filesystem " + fsName);
                    fileSystem = grid.getFileSystem(fsName);
                }
            } else {
                log.fine("Filesystem " + fsName + " not found");
            }
        } else {
            log.fine("Found existing filesystem " + fileSystem.getName());
        }
        return fileSystem;
    }

    public FileSystem snapshot(String fsName) throws Exception {
        return snapshot(fsName, null);
    }
    
    public FileSystem snapshot(String fsName, String snapPostfix) throws Exception {
        FileSystem fs = getFS(fsName, false);
        if(fs == null) {
            log.warning("Cannot snapshot non-existent file system " + fsName);
            return null;
        }
        String snapName;
        if(snapPostfix == null) {
            long t = System.currentTimeMillis();
            snapName = String.format("%s-%TF-%TT-%TL", fsName, t, t, t);
        } else {
            snapName = String.format("%s-%s", fsName, snapPostfix);
        }
        return snapshot(fs, snapName);
    }

    public FileSystem snapshot(FileSystem fs, String snapName) throws Exception {
        if(!(fs instanceof BaseFileSystem)) {
            log.warning("Cannot snapshot filesystem " + fs.getName() + " " + fs.
                    getClass().getName());
            return null;
        }
        BaseFileSystem bfs = (BaseFileSystem) fs;
        FileSystem ret = bfs.createSnapshot(snapName);
        log.fine("Created snapshot: " + ret);
        return ret;
    }

    /**
     * Gets the file system where log files should be stored.
     * @return the log file system
     * @throws java.rmi.RemoteException
     * @throws com.sun.caroline.platform.StorageManagementException
     */
    public FileSystem getAuraLogFS() throws RemoteException, StorageManagementException {
        return getFS("aura.logs");
    }
    /**
     * The mount point for the logs file system in a deployed service.
     */
    public static final String logFSMntPnt = "/files/auraLogs";

    /**
     * Gets the file system where code should be stored.
     * @param grid the grid where we should get the filesystem
     * @param instance the instance we want the file system for
     * @return the code file system
     * @throws java.rmi.RemoteException
     * @throws com.sun.caroline.platform.StorageManagementException
     */
    public FileSystem getAuraDistFS() throws RemoteException, StorageManagementException {
        return getFS("aura.dist");
    }
    /**
     * The mount point for the code file system in a deployed service.
     */
    public static final String auraDistMntPnt = "/files/auraDist";

    /**
     * Gets the file system where persistent caches can be stored.
     * @return the cache file system
     * @throws java.rmi.RemoteException
     * @throws com.sun.caroline.platform.StorageManagementException
     */
    public FileSystem getAuraCacheFS() throws RemoteException, StorageManagementException {
        return getFS("cache");
    }
    /**
     * The mount point for the code file system in a deployed service.
     */
    public static final String cacheFSMntPnt = "/files/cache";

    /**
     * Gets the usage (in bytes) for all of the filesystems registered for a
     * given grid account.
     * @param grid the grid for which we want disk usage.
     * @return the usage (in bytes) on the grid
     * @throws java.lang.Exception if there is a problem getting the usage from
     * the grid
     */
    public long getDiskUsage() throws Exception {

        long total = 0;
        for(FileSystem fs : grid.findAllFileSystems()) {
            total += fs.getMetrics().getSpaceUsed();
        }
        return total;
    }

    private void createAuraNetwork() throws Exception {
        try {
            // Try to create a customer network for the test
            network = grid.createNetwork(instance + "-auraNet", 512,
                    new CustomerNetworkConfiguration());
            log.info("Created network " + network.getName());
        } catch(DuplicateNameException e) {
            // Reuse an existing network
            network = grid.getNetwork(instance + "-auraNet");
            log.fine("Network already exists, reusing " + network.getName());
        }
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
    public NetworkAddress getAddressFor(String hostName) throws Exception {
        // Allocate the internal addresses and the real services behind the
        // virtual service
        HostNameZone hnZone = grid.getInternalHostNameZone();
        NetworkAddress internalAddress = null;
        hostName = instance + "-" + hostName;
        try {
            internalAddress =
                    network.allocateAddress(hostName);
            log.info("Allocated internal address " +
                    internalAddress.getAddress());
        } catch(DuplicateNameException e) {
            internalAddress = network.getAddress(hostName);
            log.fine("Reusing address " + internalAddress.getAddress());
        } catch(NetworkAddressAllocationException e) {
            log.severe("Error allocating address: " + e.getMessage());
            throw e;
        }

        bindHostName(hnZone, internalAddress, hostName);
        return internalAddress;
    }
    public NetworkAddress getExternalAddressFor(String name) throws Exception {
        return getExternalAddressFor(name, name);
    }

    public NetworkAddress getExternalAddressFor(String name, String hostName) throws Exception {

        // Allocate an external address for the virtual service if necessary
        NetworkAddress externalAddress = null;
        String extName = instance + "-" + name;
        try {
            externalAddress =
                    grid.allocateExternalAddress(extName);
            log.info("Allocated external address " +
                    externalAddress.getAddress());
        } catch(DuplicateNameException e) {
            log.fine("External address exists, reusing");
            externalAddress = grid.getExternalAddress(extName);
        }
        bindHostName(grid.getExternalHostNameZone(), externalAddress, hostName);
        return externalAddress;
    }

    public void bindHostName(HostNameZone hnZone,
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
            log.info("Created hostname binding for " + hostName);
        } catch(DuplicateNameException dne) {
            binding = hnZone.getBinding(hostName);
            log.finer("Host name \"" + hostName +
                    "\" has already been defined as " +
                    binding.getConfiguration().getAddresses().toArray()[0]);
        } catch(ConflictingHostNameException chne) {
            binding = hnZone.getBinding(hostName);
            log.finer("Host name \"" + hostName +
                    "\" has already been defined as " +
                    binding.getConfiguration().getAddresses().toArray()[0]);
        }

    }

    public void createNAT(UUID external, UUID internal, String name)
            throws Exception {
        NetworkConfiguration netConf =
                new DynamicNatConfiguration(external,
                internal);
        String natName = instance + "-" + name;
        try {
            grid.createNetworkSetting(natName, netConf);
        } catch(DuplicateNameException dne) {
            NetworkSetting ns = grid.getNetworkSetting(natName);
            ns.changeConfiguration(netConf);
        }
    }

    public URL getConfigURL(String arg) {
        URL cu = com.sun.labs.aura.grid.util.GridUtil.class.getResource(arg);
        if(cu == null) {
            try {
                cu = (new File(arg)).toURI().toURL();
            } catch(MalformedURLException mue) {
                return null;
            }
        }
        return cu;
    }
}
