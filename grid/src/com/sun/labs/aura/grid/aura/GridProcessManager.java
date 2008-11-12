package com.sun.labs.aura.grid.aura;

import com.sun.caroline.platform.BaseFileSystem;
import com.sun.caroline.platform.BaseFileSystemConfiguration;
import com.sun.caroline.platform.DestroyInProgressException;
import com.sun.caroline.platform.Event;
import com.sun.caroline.platform.EventStream;
import com.sun.caroline.platform.FileSystem;
import com.sun.caroline.platform.ProcessConfiguration;
import com.sun.caroline.platform.ProcessRegistration;
import com.sun.caroline.platform.ProcessRegistrationFilter;
import com.sun.caroline.platform.Resource;
import com.sun.caroline.platform.ResourceName;
import com.sun.caroline.platform.RunState;
import com.sun.caroline.platform.Selector;
import com.sun.caroline.platform.SnapshotFileSystem;
import com.sun.caroline.platform.SnapshotFileSystemConfiguration;
import com.sun.labs.aura.datastore.impl.DSBitSet;
import com.sun.labs.aura.datastore.impl.PartitionCluster;
import com.sun.labs.aura.datastore.impl.ProcessManager;
import com.sun.labs.aura.datastore.impl.Replicant;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.util.TimeSpec;
import com.sun.labs.util.props.Component;
import com.sun.labs.util.props.ConfigStringList;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 *
 */
public class GridProcessManager extends Aura implements ProcessManager {

    @ConfigStringList(defaultList={"1h", "1d"})
    public static final String PROP_SNAP_INTERVALS = "snapIntervals";

    @ConfigStringList(defaultList={"hourly", "daily"})
    public static final String PROP_SNAP_TAGS = "snapTags";

    @ConfigStringList(defaultList={"24", "7"})
    public static final String PROP_SNAP_COUNTS = "snapCounts";
    
    @Override
    public String serviceName() {
        return "GridProcessManager";
    }
    
    List<EventStream> eventStreams;
    
    EventHandler eh;

    Timer timer;

    public GridProcessManager() {
        timer = new Timer("snapshotter", true);
    }

    public void start() {
        eh = new EventHandler();
        Thread t = new Thread(eh);
        t.setDaemon(true);
        t.start();
    }

    public void stop() {
        eh.finished = true;
    }

    public PartitionCluster createPartitionCluster(DSBitSet prefix, DSBitSet owner) throws AuraException, RemoteException {
        try {
            ProcessConfiguration pcConfig = getPartitionClusterConfig(prefix.
                    toString(), false, owner.toString());
            ProcessRegistration pcReg = gu.createProcess(getPartitionName(
                    prefix.toString()), pcConfig);
            gu.startRegistration(pcReg);
            while(pcReg.getRunState() != RunState.RUNNING) {
                pcReg.waitForStateChange(100000L);
            }
            
            Thread.sleep(5000);
            
            //
            // Now it should be registered with the Jini server, let's look up the partition clusters
            // and get the one with the right prefix.
            PartitionCluster ret = null;
            for(Component c : cm.lookupAll(com.sun.labs.aura.datastore.impl.PartitionCluster.class, null)) {
                if(((PartitionCluster) c).getPrefix().equals(prefix)) {
                    ret = (PartitionCluster) c;
                    break;
                }
            }
            
            if(ret != null) {
                //
                // Now that we have the partition cluster running, we need a replicant
                // running underneath it.
                createReplicant(prefix, owner);
            } else {
                logger.warning("Partition for prefix " + prefix.toString() + " not found");
            }
            
            return ret;
        } catch(Exception ex) {
            throw new AuraException("Error getting partition cluster for prefix " +
                    prefix, ex);
        }
    }

    public Replicant createReplicant(DSBitSet prefix, DSBitSet owner) throws AuraException, RemoteException {
        try {
            String prefixString = prefix.toString();
            
            //
            // Make sure there's a filesystem for this replicant!
            createReplicantFileSystem(prefixString, owner.toString());
            ProcessConfiguration repConfig = getReplicantConfig(replicantConfig,
                    prefixString);
            ProcessRegistration repReg = gu.createProcess(getReplicantName(
                    prefixString), repConfig);
            gu.startRegistration(repReg);
            while(repReg.getRunState() != RunState.RUNNING) {
                repReg.waitForStateChange(1000000L);
            }
            
            Thread.sleep(5000);
            //
            // Now it should be registered with the Jini server, let's look up the replicants
            // and get the one with the right prefix.
            for(Component c : cm.lookupAll(com.sun.labs.aura.datastore.impl.Replicant.class, null)) {
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
        BaseFileSystemConfiguration fsc = fs.getConfiguration();
        Map<String,String> md = fsc.getMetadata();
        logger.info("Changing old prefix to " + childPrefix1);
        md.put("prefix", childPrefix1.toString());
        fsc.setMetadata(md);
        try {
            fs.changeConfiguration(fsc);
        } catch (Exception rx) {
            logger.log(Level.SEVERE, "Error setting file system metadata for " + fs.getName(), rx);
        }

        //
        // Update our map with the new prefix
        repFSMap.put(childPrefix1.toString(), fs);
        repFSMap.remove(oldPrefix.toString());
        
        //
        // Update the process registration for the old replicant to reflect
        // the new prefix
        ProcessRegistration oldRep = gu.lookupProcessRegistration(
                getReplicantName(oldPrefix.toString()));
        try {
            ProcessConfiguration repConf =
                    getReplicantConfig(replicantConfig, childPrefix1.toString());
            oldRep.changeConfiguration(repConf);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to update replicant "
                    + oldPrefix.toString() + " registration", e);
        }
        
        //
        // Replace the config for the old partition too, thereby updating
        // the command line and the metadata
        ProcessRegistration oldPart = gu.lookupProcessRegistration(
                getPartitionName(oldPrefix.toString()));
        try {
            ProcessConfiguration pc =
                    getPartitionClusterConfig(childPrefix1.toString());
            oldPart.changeConfiguration(pc);
        } catch (DestroyInProgressException e) {
            //
            // This is fine - if the reg is getting destroyed, we don't care
            logger.log(Level.FINE, "Failed to update partition prefix since partition is being destroyed: " + oldPrefix.toString());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error setting process metadata for part " + oldPrefix.toString(), e);
        }
        
        //
        // Belt-and-suspenders: make sure that the child has the right prefix too and remove the owner
        fs = (BaseFileSystem) ownedFSMap.get(childPrefix2.toString());
        if(fs == null) {
            logger.warning("Unable to find file system for new child prefix " + childPrefix2 + " after split");
        }
        fsc = fs.getConfiguration();
        md = fsc.getMetadata();
        md.put("prefix", childPrefix2.toString());
        md.remove("owner");
        fsc.setMetadata(md);
        try {
            fs.changeConfiguration(fsc);
        } catch (Exception rx) {
            logger.log(Level.SEVERE, "Error setting file system metadata for " + fs.getName(), rx);
        }
        
        //
        // Remove the owner for the new partition cluster
        ProcessRegistration newPart = gu.lookupProcessRegistration(
                getPartitionName(childPrefix2.toString()));
        ProcessConfiguration pc = newPart.getRegistrationConfiguration();
        md = pc.getMetadata();
        md.remove("owner");
        pc.setMetadata(md);
        try {
            newPart.changeConfiguration(pc);
        } catch (DestroyInProgressException e) {
            //
            // This is fine - if the reg is getting destroyed, we don't care
            logger.log(Level.FINE, "Failed to update partition prefix since partition is being destroyed: " + childPrefix2.toString());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error setting process metadata for part " + childPrefix2.toString(), e);
        }
        
        //
        // Move the FS from owned map to regular map
        repFSMap.put(childPrefix2.toString(), fs);
        ownedFSMap.remove(childPrefix2.toString());
        
    }

    public void snapshot(DSBitSet prefix)
            throws AuraException, RemoteException {
        try {
            gu.snapshot(getReplicantName(prefix.toString()));
        } catch (Exception e) {
            throw new AuraException("Unable to create snapshot for " + prefix.toString(), e);
        }
    }

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        List<String> intervals = ps.getStringList(PROP_SNAP_INTERVALS);
        List<String> tags = ps.getStringList(PROP_SNAP_TAGS);
        List<String> counts = ps.getStringList(PROP_SNAP_COUNTS);
        if(!(intervals.size() == tags.size() && intervals.size() == counts.size())) {
            throw new PropertyException(ps.getInstanceName(), PROP_SNAP_INTERVALS,
                    PROP_SNAP_INTERVALS + ", " + PROP_SNAP_TAGS +
                    ", and " + PROP_SNAP_COUNTS + 
                    " must be the same length");
        }
        for(int i = 0; i < intervals.size(); i++) {
            try {
                Snapshotter shot =
                        new Snapshotter(intervals.get(i), tags.get(i), counts.
                        get(i));
            } catch (NumberFormatException ex) {
                throw new PropertyException(ps.getInstanceName(), PROP_SNAP_COUNTS,
                        "Bad snapshot count: " + counts.get(i));
            } catch (IllegalArgumentException ex) {
                throw new PropertyException(ps.getInstanceName(), PROP_SNAP_INTERVALS,
                        "Bad snapshot interval: " + intervals.get(i));
            }

        }
    }


    
    protected class EventHandler implements Runnable {
        
        protected Selector s;
        
        protected boolean finished;

        protected Pattern namePattern = Pattern.compile(String.format("%s-(.*)", instance));
        
        public EventHandler() {
            s = new Selector();

            //
            // A filter that will match registrations with the monitor metadata value set to true
            ProcessRegistrationFilter monitorFilter = new ProcessRegistrationFilter.RegistrationConfigurationMetaMatch(
                        Pattern.compile("monitor"), Pattern.compile("true"));
            //
            // And one that will match registrations that match the user name and instance name.
            logger.info("namePattern: " + namePattern.toString());
            ProcessRegistrationFilter instanceFilter = new ProcessRegistrationFilter.NameMatch(namePattern);

            //
            // The composition of these filters.
            ProcessRegistrationFilter filter = new ProcessRegistrationFilter.And(monitorFilter, instanceFilter);

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
            // Sign up for destruction events for existing registrations.
            try {
                for(ProcessRegistration pr : grid.findProcessRegistrations(filter)) {
                    logger.info("found registration " + pr.getName());
                    ArrayList<Event.Type> p = new ArrayList();
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
            String name = ResourceName.getCSName(e.getResource().getName());
            if(!namePattern.matcher(name).matches()) {
                logger.info(String.format("Ignoring %s from %s", e.getType(), e.getResource().getName()));
                return;
            }
            
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

    /**
     * A task for taking snapshots at pre-defined intervals.
     */
    class Snapshotter extends TimerTask {

        private long interval;

        private String tag;

        private int count;

        private Map<String,List<SnapshotFileSystem>> snaps;

        public Snapshotter(String interval, String tag, String count)
                throws IllegalArgumentException, NumberFormatException {
            this.interval = TimeSpec.parse(interval);
            logger.info(String.format("%s interval %d", tag, this.interval));
            this.tag = tag;
            this.count = Integer.parseInt(count);
            snaps = new HashMap<String, List<SnapshotFileSystem>>();

            //
            // Discover any existing snapshots for this interval/tag.
            try {
                for(FileSystem fs : gu.getGrid().findAllFileSystems()) {
                    if(fs instanceof SnapshotFileSystem) {
                        Map<String, String> mdm = ((SnapshotFileSystem) fs).
                                getConfiguration().getMetadata();
                        if(mdm != null) {
                            String prefix = mdm.get("prefix");
                            if(prefix != null && mdm.get(tag) != null) {
                                mp(prefix, (SnapshotFileSystem) fs);
                            }
                        }
                    }
                }
            } catch (RemoteException rx) {
                logger.severe("Error getting snapshots for " + tag);
            }
            timer.scheduleAtFixedRate(this, 0, this.interval);
        }

        private List<SnapshotFileSystem> mp(String prefix, SnapshotFileSystem fs) {
            List<SnapshotFileSystem> l = snaps.get(prefix);
            if(l == null) {
                l = new ArrayList<SnapshotFileSystem>();
                snaps.put(prefix, l);
            }
            l.add(fs);
            return l;
        }

        @Override
        public void run() {
            
            long t = System.currentTimeMillis();
            for(Map.Entry<String, FileSystem> e : repFSMap.entrySet()) {
                try {
                    String prefix = e.getKey();
                    FileSystem fs = e.getValue();
                    String sname = String.format("%s-%s-%TF-%TT",
                            ResourceName.getCSName(fs.getName()), tag, t, t, t);

                    //
                    // Make the snapshot and set up the metadata.
                    SnapshotFileSystem sfs = ((BaseFileSystem) fs).
                            createSnapshot(sname);
                    SnapshotFileSystemConfiguration sfsc =
                            sfs.getConfiguration();
                    Map<String, String> md = sfsc.getMetadata();
                    if(md == null) {
                        md = new HashMap<String, String>();
                    }
                    md.put(tag, "true");
                    md.put("prefix", prefix);
                    sfsc.setMetadata(md);
                    sfs.changeConfiguration(sfsc);

                    //
                    // Add it to the map, and remove old ones.
                    List<SnapshotFileSystem> l = mp(prefix, sfs);
                    while(l.size() > count) {
                        SnapshotFileSystem ofs = l.remove(0);
                        logger.info(String.format("destroying %s", ofs.getName()));
                        ofs.destroy();
                    }

                } catch (Exception ex) {
                    logger.log(Level.SEVERE, String.format("Error snapshotting %s", e.getValue().getName()), ex);
                }
            }
        }
    }
}
