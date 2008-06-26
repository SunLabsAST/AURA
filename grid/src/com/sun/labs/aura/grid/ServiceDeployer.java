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

    Grid grid;
    
    GridUtil gu;

    FileSystem logFS;

    FileSystem auraDistFS;
    Logger logger = Logger.getLogger("");

    public ServiceDeployer(String instance, URL gridURL, String user,
            String passwd) throws RemoteException, StorageManagementException, Exception {
        grid = GridFactory.getGrid(gridURL, user, passwd);
        logger.info("Got grid: " + grid);
        gu = new GridUtil(grid, instance);
        logFS = gu.getAuraLogFS();
        auraDistFS = gu.getAuraDistFS();
    }

    public void deploy(String jarFile, String[] jvmArgs,
            String config, String starter, String instance) throws Exception {

        String classpath = GridUtil.auraDistMntPnt + "/dist/aura.jar:" +
                GridUtil.auraDistMntPnt + "/dist/aardvark.jar:" +
                GridUtil.auraDistMntPnt + "/dist/grid.jar";
        
        if(jarFile != null) {
            classpath = classpath + ":" + jarFile;
        }
        
        String[] cmd = new String[]{
            "-DauraHome=" + GridUtil.auraDistMntPnt,
            "-DauraInstance=" + instance,
            "-DauraGroup=" + instance + "-aura",
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

        ProcessConfiguration pc = new ProcessConfiguration();
        pc.setCommandLine(cmd);
        pc.setSystemSinks(GridUtil.logFSMntPnt + "/" + starter + ".out", false);

        Collection<FileSystemMountParameters> mountParams =
                new ArrayList<FileSystemMountParameters>();

        mountParams.add(
                new FileSystemMountParameters(auraDistFS.getUUID(),
                new File(GridUtil.auraDistMntPnt).getName()));
        mountParams.add(
                new FileSystemMountParameters(logFS.getUUID(),
                new File(GridUtil.logFSMntPnt).getName()));
        pc.setFileSystems(mountParams);
        pc.setWorkingDirectory(GridUtil.logFSMntPnt);
        pc.setProcessExitAction(ProcessExitAction.DESTROY);

        Network network = gu.getNetwork();
        if(network == null) {
            throw new IllegalStateException("No network for deployment");
        }
        
        List<UUID> addresses = new ArrayList<UUID>();
        addresses.add(gu.getAddressFor(instance +
                "-serviceDeployer").getUUID());

        pc.setNetworkAddresses(addresses);
        ProcessRegistration reg = gu.createProcess(starter, pc);
        gu.startRegistration(reg);
    }

    public static void main(String[] args) {
        //
        // Use the labs format logging.
        Logger logger = Logger.getLogger("");
        for(Handler h : logger.getHandlers()) {
            h.setFormatter(new SimpleLabsLogFormatter());
        }

        String flags = "j:a:i:g:u:";
        Getopt gopt = new Getopt(args, flags);

        URL gridURL = null;
        String user = null;
        String passwd = null;
        String instance = null;
        String jarFile = null;

        //
        // Try to read ~/.caroline.
        File homeDir = new File(System.getProperty("user.home"));
        File dotCaroline = new File(homeDir, ".caroline");
        try {
            if(dotCaroline.exists()) {
                Properties props = new Properties();
                props.load(new FileInputStream(dotCaroline));
                gridURL = new URL(props.getProperty("gridURL"));
                user = props.getProperty("customerID");
                passwd = props.getProperty("password");
                instance = props.getProperty("instance");
                if(instance == null) {
                    instance = System.getProperty("user.name");
                }
            }
        } catch(Exception e) {
            logger.severe("Error reading .caroline: " + e);
            return;
        }

        if(gridURL == null) {

            //
            // No .caroline, so we need to set the defaults.
            gridURL = null;
            try {
                gridURL = new URL("https://dev.caroline.east.sun.com/");
            } catch(MalformedURLException mue) {
                logger.severe("Malformed URL?  Weird");
                return;
            }
            user = "aura";
            passwd = "corona";
            instance = System.getProperty("user.name");
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
                        gridURL = new URL(gopt.optArg);
                    } catch(MalformedURLException mue) {
                        logger.severe("Malformed grid URL: " + gopt.optArg);
                        return;
                    }
                    break;
                case 'i':
                    instance = gopt.optArg;
                    break;
                case 'j':
                    jarFile = gopt.optArg;
                    break;
                case 'u':
                    user = gopt.optArg;
                    break;
            }
        }

        if(passwd == null) {
            BufferedReader r = new BufferedReader(new InputStreamReader(
                    System.in));
            try {
                passwd = r.readLine();
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
            ServiceDeployer deployer =
                    new ServiceDeployer(instance, gridURL, user, passwd);
            deployer.deploy(jarFile, jvmArgs.toArray(new String[0]), config,
                    starter, instance);
        } catch(Exception ex) {
            logger.log(Level.SEVERE, "Error deploying service", ex);
        }
    }
}
