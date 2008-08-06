package com.sun.labs.aura.grid;

import com.sun.labs.aura.grid.util.GridUtil;
import com.sun.caroline.platform.FileSystem;
import com.sun.caroline.platform.FileSystemMountParameters;
import com.sun.caroline.platform.Grid;
import com.sun.caroline.platform.GridFactory;
import com.sun.caroline.platform.Network;
import com.sun.caroline.platform.ProcessConfiguration;
import com.sun.caroline.platform.ProcessExitAction;
import com.sun.caroline.platform.ProcessRegistration;
import com.sun.caroline.platform.StorageManagementException;
import com.sun.labs.minion.util.Getopt;
import com.sun.labs.util.SimpleLabsLogFormatter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A main class to deploy an Aura service onto the grid.
 */
public class ServiceDeployer {

    public final static String baseClasspath =
        GridUtil.auraDistMntPnt + "/dist/aura.jar:" +
        GridUtil.auraDistMntPnt + "/dist/aardvark.jar:" +
        GridUtil.auraDistMntPnt + "/dist/grid.jar";
    
    Grid grid;
    GridUtil gu;
    Collection<FileSystemMountParameters> mountParams =
            new ArrayList<FileSystemMountParameters>();
    Logger logger = Logger.getLogger("");

    /** ToDo: Write getters and setters for all these */
    String instanceName = "default-instance-name";
    String username;
    String password;
    URL gridURL;
    
    public ServiceDeployer() {
    }
    
    public ServiceDeployer(String instanceName, URL gridURL, String username, String password) {
        this.instanceName = instanceName;
        this.gridURL = gridURL;
        this.username = username;
        this.password = password;
    }

    public GridUtil getGridUtil() throws Exception {
        if(instanceName == null) {
            throw new IllegalStateException("Instance name not set before attempting to access grid");
        }

        if(gu == null) {
            gu = new GridUtil(getGrid(), instanceName);
        }
        return gu;
    }
    
    public Grid getGrid() throws Exception {
        if(username == null) {
            throw new IllegalStateException("Username not set before attempting to access grid");
        }
        if(password == null) {
            throw new IllegalStateException("Password not set before attempting to access grid");
        }
        if(gridURL == null) {
            throw new IllegalStateException("Grid URL not set before attempting to access grid");
        }
        if(instanceName == null) {
            throw new IllegalStateException("Instance name not set before attempting to access grid");
        }

        if(grid == null) {
            grid = GridFactory.getGrid(gridURL, username, password);
            logger.info("Got grid: " + grid.getGridURL() + " instance: " + instanceName);
        }
        return grid;
    }
    
    public void addMount(String mountDir) throws RemoteException, StorageManagementException, Exception {
        UUID uuid = getGridUtil().getFS(mountDir).getUUID();
        String mountPoint = new File(mountDir).getName();
        logger.info("Mounting " + uuid + " at " + mountPoint);
        mountParams.add(new FileSystemMountParameters(uuid, mountPoint));
    }
    
    /**
     */
    public void deploy(String jarFile, String[] jvmArgs, String config, String starter)
            throws Exception {
        String classpath = baseClasspath;
        
        if(jarFile != null) {
            classpath = classpath + ":" + jarFile;
        }
        
        String[] cmd = new String[]{
            "-DauraHome=" + GridUtil.auraDistMntPnt,
            "-Dinstance=" + instanceName,
            "-DauraInstance=" + instanceName,
            "-DauraPolicy=" + GridUtil.auraDistMntPnt + "/dist/jsk-all.policy",
            "-DauraGroup=" + instanceName + "-aura",
            "-cp",
            classpath,
            "com.sun.labs.aura.AuraServiceStarter",
            config,
            starter
        };

        if(jvmArgs != null && jvmArgs.length > 0) {
            String[] tmp = new String[cmd.length + jvmArgs.length];
            System.arraycopy(jvmArgs, 0, tmp, 0, jvmArgs.length);
            System.arraycopy(cmd, 0, tmp, jvmArgs.length, cmd.length);
            cmd = tmp;
        }

        ProcessConfiguration processConfig =
                getGridUtil().getProcessConfig(cmd, starter, mountParams);
        processConfig.setProcessExitAction(ProcessExitAction.DESTROY);

        Network network = getGridUtil().getNetwork();
        if(network == null) {
            throw new IllegalStateException("No network for deployment");
        }
        
        ProcessRegistration reg = getGridUtil().createProcess(starter, processConfig);
        getGridUtil().startRegistration(reg);
    }

    public static void main(String[] args) {
        //
        // Use the labs format logging.
        Logger logger = Logger.getLogger("");
        for(Handler h : logger.getHandlers()) {
            h.setFormatter(new SimpleLabsLogFormatter());
        }

        String flags = "j:a:i:g:u:m:";
        Getopt gopt = new Getopt(args, flags);

        ServiceDeployer deployer = new ServiceDeployer();
        
        String jarFile = null;
        ArrayList<String> mounts = new ArrayList<String>();
     
        //
        // Try to read ~/.caroline.
        File homeDir = new File(System.getProperty("user.home"));
        File dotCaroline = new File(homeDir, ".caroline");
        try {
            if(dotCaroline.exists()) {
                Properties props = new Properties();
                props.load(new FileInputStream(dotCaroline));
                deployer.gridURL = new URL(props.getProperty("gridURL"));
                deployer.username = props.getProperty("customerID");
                deployer.password = props.getProperty("password");
                deployer.instanceName = props.getProperty("instance");
                if(deployer.instanceName == null) {
                    deployer.instanceName = System.getProperty("user.name");
                }
            }
        } catch(Exception e) {
            logger.severe("Error reading .caroline: " + e);
            return;
        }

        if(deployer.gridURL == null) {
            //
            // No .caroline, so we need to set the defaults.
            try {
                deployer.gridURL = new URL("https://dev.caroline.east.sun.com/");
            } catch(MalformedURLException mue) {
                logger.severe("Malformed URL?  Weird");
                return;
            }
            deployer.username = "aura";
            deployer.password = "corona";
        }

        List<String> jvmArgs = new ArrayList<String>();
        int c;
        while((c = gopt.getopt()) != -1) {
            switch(c) {
                case 'a':
                    jvmArgs.add(gopt.optArg);
                    break;
                case 'g':
                    try {
                        deployer.gridURL = new URL(gopt.optArg);
                    } catch(MalformedURLException mue) {
                        logger.severe("Malformed grid URL: " + gopt.optArg);
                        return;
                    }
                    break;
                case 'i':
                    deployer.instanceName = gopt.optArg;
                    break;
                case 'j':
                    jarFile = gopt.optArg;
                    break;
                case 'u':
                    deployer.username = gopt.optArg;
                    break;
                case 'm':
                    mounts.add(gopt.optArg);
                    break;
            }
        }

        if(deployer.password == null) {
            BufferedReader r = new BufferedReader(new InputStreamReader(
                    System.in));
            try {
                deployer.password = r.readLine();
            } catch(IOException ex) {
                logger.severe("Error reading password: " + ex);
                return;
            }
        }

        if(gopt.optInd + 1 >= args.length) {
            logger.severe(
                    "Usage: ServiceDeployer -a <jvmarg> -g <gridurl>\n\t" +
                    "-i <instance> -j <jarfile> -u <user> <configResource> <starter>");
            return;
        }
        String config = args[gopt.optInd];
        String starter = args[gopt.optInd + 1];

        try {
            for(String mountPoint : mounts) {
                deployer.addMount(mountPoint);
            }
            deployer.deploy(jarFile, jvmArgs.toArray(new String[0]), config, starter);
        } catch(Exception ex) {
            logger.log(Level.SEVERE, "Error deploying service", ex);
        }
    }
}
