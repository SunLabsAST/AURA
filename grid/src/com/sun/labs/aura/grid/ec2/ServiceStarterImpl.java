package com.sun.labs.aura.grid.ec2;

import com.sun.labs.aura.grid.ServiceStarter;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of the service starter interface.
 */
public class ServiceStarterImpl implements ServiceStarter, Configurable {

    /**
     * The directory where the Aura grid distribution lives.
     */
    @ConfigString(defaultValue="/aura-dist")
    public static final String DIST_DIR = "distDir";

    private String distDir;

    @ConfigString(defaultValue="/data")
    public static final String DATA_DIR = "dataDir";

    private String dataDir;

    private Logger logger;

    private List<Process> processes;
    
    @Override
    public boolean start(String configFile, String starter) throws RemoteException {
        ProcessBuilder pb = new ProcessBuilder(new String[] {
           "java",
           "-jar",
           "/dist/grid.jar"
        });
        try {
            Process p = pb.start();
            processes.add(p);
            return true;
        } catch(IOException ex) {
            logger.log(Level.SEVERE, "Error starting process", ex);
            return false;
        }

    }

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
