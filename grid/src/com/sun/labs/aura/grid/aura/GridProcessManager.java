package com.sun.labs.aura.grid.aura;

import com.sun.caroline.platform.BaseFileSystem;
import com.sun.caroline.platform.Event;
import com.sun.caroline.platform.EventStream;
import com.sun.caroline.platform.ProcessConfiguration;
import com.sun.caroline.platform.ProcessRegistration;
import com.sun.caroline.platform.ProcessRegistrationFilter;
import com.sun.caroline.platform.Resource;
import com.sun.caroline.platform.ResourceName;
import com.sun.caroline.platform.RunState;
import com.sun.caroline.platform.Selector;
import com.sun.labs.aura.datastore.impl.DSBitSet;
import com.sun.labs.aura.datastore.impl.PartitionCluster;
import com.sun.labs.aura.datastore.impl.ProcessManager;
import com.sun.labs.aura.datastore.impl.Replicant;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.util.props.Component;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 *
 */
public class GridProcessManager extends Aura implements ProcessManager {

    @Override
    public String serviceName() {
        return "GridProcessManager";
    }
    
    List<EventStream> eventStreams;
    
    EventHandler eh;
    
    public void start() {
        eh = new EventHandler();
        Thread t = new Thread(eh);
        t.setDaemon(true);
        t.start();
    }

    public void stop() {
        eh.finished = true;
    }

    public PartitionCluster createPartitionCluster(DSBitSet prefix) throws AuraException, RemoteException {
        try {
            ProcessConfiguration pcConfig = getPartitionClusterConfig(prefix.
                    toString());
            ProcessRegistration pcReg = gu.createProcess(getPartitionName(
                    prefix.toString()), pcConfig);
            while(pcReg.getRunState() != RunState.RUNNING) {
                pcReg.waitForStateChange(100000L);
            }
            
            //
            // Now it should be registered with the Jini server, let's look up the partition clusters
            // and get the one with the right prefix.
            PartitionCluster ret = null;
            Component[] components = cm.getComponentRegistry().lookup(com.sun.labs.aura.datastore.impl.PartitionCluster.class, Integer.MAX_VALUE);
            for(Component c : components) {
                if(((PartitionCluster) c).getPrefix().equals(prefix)) {
                    ret = (PartitionCluster) c;
                    break;
                }
            }
            
            if(ret != null) {
                //
                // Now that we have the partition cluster running, we need a replicant
                // running underneath it.
                createReplicant(prefix);
            } else {
                logger.warning("Partition for prefix " + prefix.toString() + " not found");
            }
            
            return ret;
        } catch(Exception ex) {
            throw new AuraException("Error getting partition cluster for prefix " +
                    prefix, ex);
        }
    }

    public Replicant createReplicant(DSBitSet prefix) throws AuraException, RemoteException {
        try {
            String prefixString = prefix.toString();
            
            //
            // Make sure there's a filesystem for this replicant!
            createReplicantFileSystem(prefixString);
            ProcessConfiguration repConfig = getReplicantConfig(replicantConfig,
                    prefixString);
            ProcessRegistration pcReg = gu.createProcess(getReplicantName(
                    prefixString), repConfig);
            while(pcReg.getRunState() != RunState.RUNNING) {
                pcReg.waitForStateChange(1000000L);
            }
            
            //
            // Now it should be registered with the Jini server, let's look up the replicants
            // and get the one with the right prefix.
            Component[] components = cm.getComponentRegistry().lookup(
                    com.sun.labs.aura.datastore.impl.Replicant.class,
                    Integer.MAX_VALUE);
            for(Component c : components) {
                if(((Replicant) c).getPrefix().equals(prefix)) {
                    return (Replicant) c;
                }
            }
            return null;
        } catch(Exception ex) {
            throw new AuraException("Error getting partition cluster for prefix " +
                    prefix, ex);
        }
    }
    
    public void finishSplit(DSBitSet oldPrefix, DSBitSet childPrefix1,
            DSBitSet childPrefix2) {
        
        //
        // Change the metadata on the old filesystem.
        BaseFileSystem fs = (BaseFileSystem) repFSMap.get(oldPrefix.toString());
        if(fs == null) {
            logger.warning("Unable to find file system for prefix " + oldPrefix + " after split");
            return;
        }
        Map<String,String> md = fs.getConfiguration().getMetadata();
        md.put("prefix", childPrefix1.toString());
        fs.getConfiguration().setMetadata(md);
        
        //
        // Belt-and-suspenders: make sure that the child has the right prefix too.
        fs = (BaseFileSystem) repFSMap.get(childPrefix2.toString());
        if(fs == null) {
            logger.warning("Unable to find file system for new child prefix " + childPrefix2 + " after split");
        }
        md = fs.getConfiguration().getMetadata();
        md.put("prefix", childPrefix2.toString());
        fs.getConfiguration().setMetadata(md);
    }

    public void snapshot(DSBitSet prefix)
            throws AuraException, RemoteException {
        try {
            gu.snapshot(getReplicantName(prefix.toString()));
        } catch (Exception e) {
            throw new AuraException("Unable to create snapshot for " + prefix.toString(), e);
        }
    }
    
    protected class EventHandler implements Runnable {
        
        protected Selector s;
        
        protected boolean finished;

        public EventHandler() {
            s = new Selector();

            //
            // Get events for newly created resources.
            ArrayList<Resource.Type> r = new ArrayList();
            r.add(Resource.Type.PROCESS_REGISTRATION);
            r.add(Resource.Type.BASE_FILE_SYSTEM);
            try {
                s.add(grid.openResourceCreationEventStream(r));
            } catch(RemoteException rx) {
                logger.severe("Error getting creation stream: " + rx);
            }

            //
            // Get destruction events for existing registrations.
            try {
                for(ProcessRegistration pr : grid.findProcessRegistrations(new ProcessRegistrationFilter.RegistrationConfigurationMetaMatch(
                        Pattern.compile("monitor"), Pattern.compile("true")))) {
                    ArrayList<Event.Type> p = new ArrayList();
                    logger.info("found registration " + pr.getName());
                    p.add(Resource.DESTRUCTION);
                    s.add(pr.openEventStream(p));
                }
            } catch(RemoteException rx) {
                logger.severe("Error getting process registration streams: " +
                        rx);
            }
        }
        
        public void run() {
            while(!finished) {
                try {
                    for(EventStream es : s.select(500)) {
                        Event e;
                        try {
                            while((e = es.read(0)) != null) {
                                handleEvent(e);
                            }
                        } catch(java.io.EOFException eof) {
                            //
                            // A destroyed service ran out of events.
                        } catch(java.io.IOException ioe) {
                            logger.log(Level.SEVERE,
                                    "Error reading from event stream: " + es.
                                    getEventOrigin(), ioe);
                        }
                    }
                } catch(InterruptedException ex) {
                    finished = true;
                } 
            }
            s.close();
        }
        
        public void handleEvent(Event e) {
            if(e.getResource() instanceof BaseFileSystem && e.getType().equals(Resource.CREATION)) {
                BaseFileSystem fs = (BaseFileSystem) e.getResource();
                Map<String,String> md = fs.getConfiguration().getMetadata();
                if(md != null) {
                    String type = md.get("type");
                    if(type != null && type.equals("replicant")) {
                        repFSMap.put(md.get("prefix"), fs);
                    }
                }
            } else if(e.getResource() instanceof ProcessRegistration) {
                ProcessRegistration pr =
                        (ProcessRegistration) e.getResource();
                ProcessConfiguration rpc = pr.getRegistrationConfiguration();
                String monitor = rpc == null ? null : rpc.getMetadata().get(
                        "monitor");
                if(monitor == null || !monitor.equalsIgnoreCase("true")) {
                    return;
                }
                logger.info(String.format("Handle %s from %s", e.getType(),
                        pr.getName()));
                if(e.getType().equals(Resource.CREATION)) {
                    ArrayList<Event.Type> p = new ArrayList();
                    p.add(Resource.DESTRUCTION);
                    try {
                        EventStream es = pr.openEventStream(p);
                        s.add(es);
                    } catch(RemoteException rx) {
                        logger.severe("Error opening event stream for " + pr.
                                getName());
                        return;
                    }
                } else if(e.getType().equals(Resource.DESTRUCTION)) {
                    //
                    // A destroyed resource is restarted.
                    ProcessConfiguration pc = pr.getRegistrationConfiguration();
                    String regName = parseRegName(pr.getName());
                    try {
                        ProcessRegistration nr = gu.createProcess(regName, pc);
                        gu.startRegistration(nr);
                    } catch(Exception ex) {
                        logger.log(Level.SEVERE,
                                "Error starting registration for " +
                                pr.getName(), ex);
                    }
                }
            }
        }
        
        protected String parseRegName(String name) {
            String csn = ResourceName.getCSName(name);
            //
            // Take instance- off the string, since the grid utils will add 
            // it back on!
            if(csn.startsWith(instance)) {
                return csn.substring(instance.length() + 1);
            }
            return csn;
        }
    }

}
