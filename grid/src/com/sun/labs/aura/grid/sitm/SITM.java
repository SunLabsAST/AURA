/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.grid.sitm;

import com.sun.caroline.platform.FileSystem;
import com.sun.caroline.platform.FileSystemMountParameters;
import com.sun.caroline.platform.Network;
import com.sun.caroline.platform.ProcessConfiguration;
import com.sun.caroline.platform.ProcessExitAction;
import com.sun.caroline.platform.StorageManagementException;
import com.sun.labs.aura.grid.util.GridUtil;
import com.sun.labs.aura.grid.ServiceAdapter;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A base class for starting and stopping the SITM services.
 */
public abstract class SITM extends ServiceAdapter {

    private FileSystem auraDist;

    private FileSystem logFS;

    private FileSystem cacheFS;

    public static final String cacheFSMntPnt = "/files/cache";

    protected Network network;
    
    protected GridUtil gu;
    
    public String getArtistCrawlerName() {
        return instance + "-artistCrawler";
    }
    
    public String getListenerCrawlerName() {
        return instance + "-listenerCrawler";
    }
    
    public String getTagCrawlerName() {
        return instance + "-tagCrawler";
    }
    
    /**
     * Gets a basic process configuration 
     * @param cwd the working directory for the configuration
     * @param logFile the name of the log file to use
     * @return a process configuration.
     */
    protected ProcessConfiguration getProcessConfig(
            String cwd,
            String logFile) {
        ProcessConfiguration pc = new ProcessConfiguration();
        pc.setSystemSinks(GridUtil.logFSMntPnt + "/" + logFile, false);

        //
        // Every body will get dist, log and cache filesystems.
        Collection<FileSystemMountParameters> mountParams =
                new ArrayList<FileSystemMountParameters>();
        mountParams.add(
                new FileSystemMountParameters(auraDist.getUUID(),
                new File(GridUtil.auraDistMntPnt).getName()));
        mountParams.add(
                new FileSystemMountParameters(logFS.getUUID(),
                new File(GridUtil.logFSMntPnt).getName()));
        mountParams.add(
                new FileSystemMountParameters(cacheFS.getUUID(),
                new File(GridUtil.cacheFSMntPnt).getName()));
        pc.setFileSystems(mountParams);
        pc.setWorkingDirectory(cwd);
        return pc;
    }
    
    public ProcessConfiguration getArtistCrawlerConfig() throws Exception {
        String[] cmdLine = new String[] {
            "-Xmx2g",
            "-DauraHome=" + GridUtil.auraDistMntPnt,
            "-DauraGroup=" + instance + "-aura",
            "-DcacheDir=" + cacheFSMntPnt,
            "-jar",
            GridUtil.auraDistMntPnt + "/dist/grid.jar",
            "/com/sun/labs/aura/music/resource/artistCrawlerConfig.xml",
            "starter"
        };
        
        logger.info("Instance: " + instance);
        
        ProcessConfiguration pc = getProcessConfig(GridUtil.logFSMntPnt,
                "artistCrawler.out");
        pc.setCommandLine(cmdLine);

        // Set the addresses for the process
        List<UUID> addresses = new ArrayList<UUID>();
        addresses.add(gu.getAddressFor(getArtistCrawlerName()).getUUID());
        pc.setNetworkAddresses(addresses);
        pc.setProcessExitAction(ProcessExitAction.PARK);        
        return pc;
    }
    
    public ProcessConfiguration getListenerCrawlerConfig() throws Exception {
        String[] cmdLine = new String[] {
            "-Xmx2g",
            "-DauraHome=" + GridUtil.auraDistMntPnt,
            "-DauraGroup=" + instance + "-aura",
            "-DcacheDir=" + cacheFSMntPnt,
            "-jar",
            GridUtil.auraDistMntPnt + "/dist/grid.jar",
            "/com/sun/labs/aura/music/resource/listenerCrawlerConfig.xml",
            "starter"
        };
        
        ProcessConfiguration pc = getProcessConfig(GridUtil.logFSMntPnt,
                "listenerCrawler.out");
        pc.setCommandLine(cmdLine);

        // Set the addresses for the process
        List<UUID> addresses = new ArrayList<UUID>();
        addresses.add(gu.getAddressFor(getListenerCrawlerName()).getUUID());
        pc.setNetworkAddresses(addresses);
        pc.setProcessExitAction(ProcessExitAction.PARK);        
        return pc;
    }
    
    public ProcessConfiguration getTagCrawlerConfig() throws Exception {
        String[] cmdLine = new String[] {
            "-Xmx2g",
            "-DauraHome=" + GridUtil.auraDistMntPnt,
            "-DauraGroup=" + instance + "-aura",
            "-DcacheDir=" + cacheFSMntPnt,
            "-jar",
            GridUtil.auraDistMntPnt + "/dist/grid.jar",
            "/com/sun/labs/aura/music/resource/tagCrawlerConfig.xml",
            "starter"
        };
        
        ProcessConfiguration pc = getProcessConfig(GridUtil.logFSMntPnt,
                "tagCrawler.out");
        pc.setCommandLine(cmdLine);

        // Set the addresses for the process
        List<UUID> addresses = new ArrayList<UUID>();
        addresses.add(gu.getAddressFor(getArtistCrawlerName()).getUUID());
        pc.setNetworkAddresses(addresses);
        pc.setProcessExitAction(ProcessExitAction.PARK);        
        return pc;
    }
    
    @Override
    public String serviceName() {
        return "SITM";
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        try {
            gu = new GridUtil(grid, instance);
        } catch(Exception ex) {
            throw new PropertyException(ex, ps.getInstanceName(), "grid", "Error getting grid network");
        }
        try {
            network = gu.getNetwork();
            gu.getFS("sys.packages");
            auraDist = gu.getAuraDistFS();
            logFS = gu.getAuraLogFS();
            cacheFS = gu.getCacheFS();
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
