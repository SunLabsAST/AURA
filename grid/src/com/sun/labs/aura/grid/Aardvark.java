package com.sun.labs.aura.grid;

import com.sun.caroline.platform.FileSystem;
import com.sun.caroline.platform.FileSystemMountParameters;
import com.sun.caroline.platform.Network;
import com.sun.caroline.platform.ProcessConfiguration;
import com.sun.caroline.platform.ProcessExitAction;
import com.sun.caroline.platform.ProcessRegistrationFilter;
import com.sun.caroline.platform.StorageManagementException;
import com.sun.labs.util.props.ConfigInteger;
import com.sun.labs.util.props.ConfigurationManager;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * A base class for aardvark operations on the grid.
 */
public abstract class Aardvark extends ServiceAdapter {

    protected ConfigurationManager cm;

    /**
     * The number of crawler processes to start.
     */
    @ConfigInteger(defaultValue = 6)
    public static final String PROP_NUM_CRAWLERS = "numCrawlers";

    protected int numCrawlers;

    private FileSystem auraDist;

    private FileSystem logFS;

    private FileSystem cacheFS;

    public static final String cacheFSMntPnt = "/files/cache";

    protected Network network;

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

    protected ProcessConfiguration getFeedSchedulerConfig() throws Exception {
        String[] cmdLine = new String[]{
            "-Xmx2g",
            "-DauraHome=" + GridUtil.auraDistMntPnt,
            "-DauraGroup=" + instance + "-aura",
            "-DcacheDir=" + cacheFSMntPnt,
            "-jar " + GridUtil.auraDistMntPnt + "/dist/aardvark.jar",
            "/com/sun/labs/aura/aardvark/resource/feedSchedulerConfig.xml",
            "feedSchedulerStarter"
        };

        // create a configuration and set relevant properties
        ProcessConfiguration pc = new ProcessConfiguration();
        pc.setCommandLine(cmdLine);
        pc.setSystemSinks(GridUtil.logFSMntPnt + "/feedSched.out", false);

        Collection<FileSystemMountParameters> mountParams =
                new ArrayList<FileSystemMountParameters>();

        mountParams.add(
                new FileSystemMountParameters(auraDist.getUUID(),
                new File(GridUtil.auraDistMntPnt).getName()));
        mountParams.add(
                new FileSystemMountParameters(logFS.getUUID(),
                new File(GridUtil.logFSMntPnt).getName()));
        mountParams.add(
                new FileSystemMountParameters(cacheFS.getUUID(), "cache"));

        pc.setFileSystems(mountParams);
        pc.setWorkingDirectory(GridUtil.logFSMntPnt);

        // Set the addresses for the process
        List<UUID> addresses = new ArrayList<UUID>();
        addresses.add(GridUtil.getAddressFor(grid, network, instance +
                "-feedSched").getUUID());

        pc.setNetworkAddresses(addresses);
        pc.setProcessExitAction(ProcessExitAction.PARK);

        return pc;
    }

    protected ProcessConfiguration getFeedManagerConfig(int n)
            throws Exception {
        String[] cmdLine = new String[]{
            "-DauraHome=" + GridUtil.auraDistMntPnt,
            "-DauraGroup=" + instance + "-aura",
            "-jar " + GridUtil.auraDistMntPnt + "/dist/aardvark.jar",
            "/com/sun/labs/aura/aardvark/resource/feedManagerConfig.xml",
            "feedManagerStarter"
        };

        // create a configuration and set relevant properties
        ProcessConfiguration pc = new ProcessConfiguration();
        pc.setCommandLine(cmdLine);
        pc.setSystemSinks(
                GridUtil.logFSMntPnt + "/feedMgr-" + n + ".out", false);

        Collection<FileSystemMountParameters> mountParams =
                new ArrayList<FileSystemMountParameters>();

        mountParams.add(
                new FileSystemMountParameters(auraDist.getUUID(),
                new File(GridUtil.auraDistMntPnt).getName()));
        mountParams.add(
                new FileSystemMountParameters(logFS.getUUID(),
                new File(GridUtil.logFSMntPnt).getName()));
        pc.setFileSystems(mountParams);
        pc.setWorkingDirectory(GridUtil.logFSMntPnt);

        // Set the addresses for the process
        List<UUID> addresses = new ArrayList<UUID>();
        addresses.add(GridUtil.getAddressFor(grid, network, instance +
                "-feedMgr-" + n).getUUID());

        pc.setNetworkAddresses(addresses);
        pc.setProcessExitAction(ProcessExitAction.PARK);

        // don't overlap with other replicants
        pc.setLocationConstraint(
                new ProcessRegistrationFilter.NameMatch(
                Pattern.compile(instance + ".*-feedMgr-.*")));

        return pc;
    }

    protected ProcessConfiguration getRecommenderConfig() throws Exception {
        String[] cmdLine = new String[]{
            "-DauraHome=" + GridUtil.auraDistMntPnt,
            "-DauraGroup=" + instance + "-aura",
            "-jar " + GridUtil.auraDistMntPnt + "/dist/aardvark.jar",
            "/com/sun/labs/aura/aardvark/resource/recommenderManagerConfig.xml",
            "recommenderManagerStarter"
        };

        // create a configuration and set relevant properties
        ProcessConfiguration pc = new ProcessConfiguration();
        pc.setCommandLine(cmdLine);
        pc.setSystemSinks(GridUtil.logFSMntPnt + "/recommender.out", false);

        Collection<FileSystemMountParameters> mountParams =
                new ArrayList<FileSystemMountParameters>();

        mountParams.add(
                new FileSystemMountParameters(auraDist.getUUID(),
                new File(GridUtil.auraDistMntPnt).getName()));
        mountParams.add(
                new FileSystemMountParameters(logFS.getUUID(),
                new File(GridUtil.logFSMntPnt).getName()));
        pc.setFileSystems(mountParams);
        pc.setWorkingDirectory(GridUtil.logFSMntPnt);

        // Set the addresses for the process
        List<UUID> addresses = new ArrayList<UUID>();
        addresses.add(GridUtil.getAddressFor(grid, network, instance +
                "-recommender").getUUID());

        pc.setNetworkAddresses(addresses);
        pc.setProcessExitAction(ProcessExitAction.PARK);

        return pc;
    }

    protected ProcessConfiguration getAardvarkConfig() throws Exception {
        String[] cmdLine = new String[]{
            "-DauraHome=" + GridUtil.auraDistMntPnt,
            "-DauraGroup=" + instance + "-aura",
            "-jar " + GridUtil.auraDistMntPnt + "/dist/aardvark.jar",
            "/com/sun/labs/aura/aardvark/resource/aardvarkConfig.xml",
            "aardvarkStarter"
        };

        // create a configuration and set relevant properties
        ProcessConfiguration pc = new ProcessConfiguration();
        pc.setCommandLine(cmdLine);
        pc.setSystemSinks(GridUtil.logFSMntPnt + "/aardvark.out", false);

        Collection<FileSystemMountParameters> mountParams =
                new ArrayList<FileSystemMountParameters>();

        mountParams.add(
                new FileSystemMountParameters(auraDist.getUUID(),
                new File(GridUtil.auraDistMntPnt).getName()));
        mountParams.add(
                new FileSystemMountParameters(logFS.getUUID(),
                new File(GridUtil.logFSMntPnt).getName()));
        pc.setFileSystems(mountParams);
        pc.setWorkingDirectory(GridUtil.logFSMntPnt);

        // Set the addresses for the process
        List<UUID> addresses = new ArrayList<UUID>();
        addresses.add(GridUtil.getAddressFor(grid, network, instance +
                "-aardvark").getUUID());

        pc.setNetworkAddresses(addresses);
        pc.setProcessExitAction(ProcessExitAction.PARK);

        return pc;
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        cm = ps.getConfigurationManager();
        try {
            network = grid.getNetwork(instance + "-auraNet");
            auraDist = GridUtil.getFS(grid, instance + "-aura.dist", false);
            logFS = GridUtil.getFS(grid, instance + "-aura.logs", false);
            cacheFS = GridUtil.getFS(grid, instance + "-cache", false);
            numCrawlers = ps.getInt(PROP_NUM_CRAWLERS);
        } catch(RemoteException ex) {
            throw new PropertyException(ex, ps.getInstanceName(), "grid",
                    "Error getting network");
        } catch(StorageManagementException smx) {
            throw new PropertyException(smx, ps.getInstanceName(), "grid",
                    "Error getting filesystems");
        }
        if(network == null) {
            throw new PropertyException(ps.getInstanceName(), "grid",
                    "Aura network not defined");
        }

        if(auraDist == null || logFS == null || cacheFS == null) {
            throw new PropertyException(ps.getInstanceName(), "grid",
                    "Required filesystems not defined");
        }

    }
}
