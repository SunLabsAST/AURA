package com.sun.labs.aura.grid.rp;

import com.sun.caroline.platform.ProcessConfiguration;
import com.sun.caroline.platform.ProcessRegistration;
import com.sun.caroline.platform.RunState;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.grid.ServiceAdapter;
import com.sun.labs.aura.grid.util.GridUtil;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.ConfigInteger;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.ConfigStringList;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarFile;
import java.util.logging.Level;

/**
 * A class for processing (some of) the contents of a data store on a 
 * replicant-by-replicant basis.
 */
public class DataStoreProcessor extends ServiceAdapter {

    @ConfigComponent(type = com.sun.labs.aura.datastore.DataStore.class)
    public static final String PROP_DATA_STORE = "dataStore";

    private DataStore dataStore;

    @ConfigInteger(defaultValue = 4)
    public static final String PROP_NUM_PROCESSORS = "numProcessors";

    private int numProcessors;

    @ConfigString
    public static final String PROP_REPLICANT_CONFIG = "replicantConfig";

    private String replicantConfig;

    @ConfigString
    public static final String PROP_REPLICANT_STARTER = "replicantStarter";

    private String replicantStarter;

    @ConfigString
    public static final String PROP_PROCESSOR_NAME = "processorName";
    
    private String processorName;
    
    @ConfigStringList(defaultList={})
    public static final String PROP_PROCESSOR_JARS = "processorJars";

    private List<String> prefixes;

    private List<ProcessRegistration> running;
    
    private String baseCP;

    @Override
    public String serviceName() {
        return "DataStoreProcessor";
    }
    
    public DataStoreProcessor() throws IOException {
        //
        // This is probably overkill, but we'll extract the classpath from the 
        // grid jar file so that when that changes we won't have to remember
        // to fix the base class here.
        JarFile jf = new JarFile(GridUtil.auraDistMntPnt + "/dist/grid.jar");
        String[] cpe = jf.getManifest().getMainAttributes().getValue("Class-Path").split("\\s+");
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < cpe.length; i++) {
            if(i > 0) {
                sb.append(":");
            }
            sb.append(cpe[i]);
        }
    }
    

    public void startProcess(String prefix) throws Exception {
        List<String> cmdLine = new ArrayList<String>();
        cmdLine.add("-DauraHome=" + GridUtil.auraDistMntPnt);
        cmdLine.add("-DauraGroup=" + instance + "-aura");
        cmdLine.add("-Dprefix=" + prefix);
        cmdLine.add("-cp");
        cmdLine.add(baseCP);
        cmdLine.add("com.sun.labs.aura.AuraServiceStarter");
        cmdLine.add(replicantConfig);
        cmdLine.add(replicantStarter);
        ProcessConfiguration config = 
                gu.getProcessConfig(cmdLine.toArray(new String[0]), instance + 
                "-" + processorName + "-" + prefix );
        ProcessRegistration reg = gu.createProcess(instance, config);
        gu.startRegistration(reg);
        running.add(reg);
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        processorName = ps.getString(PROP_PROCESSOR_NAME);
        replicantConfig = ps.getString(PROP_REPLICANT_CONFIG);
        replicantStarter = ps.getString(PROP_REPLICANT_STARTER);
        
        //
        // Add things to the classpath.
        List<String> cpa = ps.getStringList(PROP_PROCESSOR_JARS);
        for(String cpe : cpa) {
            baseCP += (":" + cpe);
        }
        
        //
        // Get the data store.
        dataStore = (DataStore) ps.getComponent(PROP_DATA_STORE);
        numProcessors = ps.getInt(PROP_NUM_PROCESSORS);
        try {
            prefixes = dataStore.getPrefixes();
        } catch(RemoteException ex) {
            throw new PropertyException(ex, ps.getInstanceName(),
                    PROP_DATA_STORE, "Error getting prefixes from data store");
        }
        running = new ArrayList<ProcessRegistration>();
    }

    public void start() {

        //
        // Fill up the running processors.
        while(prefixes.size() > 0 && running.size() < numProcessors) {
            String prefix = prefixes.remove(0);
            try {
                startProcess(prefix);
            } catch(Exception e) {
                logger.log(Level.SEVERE,
                        "Error starting replicant processor for " + prefix, e);
            }
        }

        while(prefixes.size() > 0) {
            try {
                Thread.sleep(5000);
            } catch(InterruptedException ie) {
                return;
            }
            for(Iterator<ProcessRegistration> i = running.iterator(); i.hasNext();) {
                ProcessRegistration reg = i.next();
                if(reg.getRunState() == RunState.NONE) {
                    i.remove();
                    try {
                        reg.destroy(100000);
                    } catch(RemoteException rx) {
                        logger.log(Level.SEVERE,
                                "Error destroying replicant processor " + reg,
                                rx);
                    }
                    String prefix = prefixes.remove(0);
                    try {
                        startProcess(prefix);
                    } catch(Exception e) {
                        logger.log(Level.SEVERE,
                                "Error starting replicant processor for " +
                                prefix, e);
                    }
                }
            }
        }
        
        //
        // Wait for running processors to finish.
        while(running.size() > 0) {
            for(Iterator<ProcessRegistration> i = running.iterator(); i.hasNext();) {
                ProcessRegistration reg = i.next();
                if(reg.getRunState() == RunState.NONE) {
                    i.remove();
                    try {
                        reg.destroy(100000);
                    } catch(RemoteException rx) {
                        logger.log(Level.SEVERE,
                                "Error destroying replicant processor " + reg,
                                rx);
                    }
                }
            }
        }
    }

    public void stop() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
