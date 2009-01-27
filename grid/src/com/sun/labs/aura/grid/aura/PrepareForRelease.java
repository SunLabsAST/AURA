package com.sun.labs.aura.grid.aura;

import com.sun.caroline.platform.BaseFileSystem;
import com.sun.caroline.platform.FileSystem;
import com.sun.caroline.platform.ResourceName;
import com.sun.caroline.platform.SnapshotFileSystem;
import com.sun.caroline.platform.SnapshotFileSystemConfiguration;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * A grid service that will prepare for a release by snapshotting all of our
 * important data so that we can roll back if necessary.
 */
public class PrepareForRelease extends Aura {

    @ConfigString(defaultValue="rel")
    public static final String PROP_REL_NAME = "relName";

    private String relName;
    
    @Override
    public String serviceName() {
        return "PrepareForRelease";
    }

    @Override
    public void start() {

        try {
            //
            // Snapshot the dist file system.
            BaseFileSystem bfs = (BaseFileSystem) gu.getFS("aura.dist");
            bfs.createSnapshot(ResourceName.getCSName(bfs.getName()) + "-" + relName);

            //
            // Snapshot the logs.
            bfs = (BaseFileSystem) gu.getFS("aura.logs");
            bfs.createSnapshot(ResourceName.getCSName(bfs.getName()) + "-" +
                    relName);

            //
            // Snapshot the cache filesystem.
            bfs = (BaseFileSystem) gu.getFS("cache");
            bfs.createSnapshot(ResourceName.getCSName(bfs.getName()) + "-" +
                    relName);
            
            //
            // Snapshot the replicant file systems.
            for(Map.Entry<String,FileSystem> e : repFSMap.entrySet()) {
                bfs = (BaseFileSystem) e.getValue();
                SnapshotFileSystem sfs = bfs.createSnapshot(ResourceName.getCSName(bfs.getName()) + "-" + relName);
                SnapshotFileSystemConfiguration sfsc =
                        sfs.getConfiguration();
                Map<String, String> md = sfsc.getMetadata();
                if(md == null) {
                    md = new HashMap<String, String>();
                }
                md.put("prefix", e.getKey());
                sfsc.setMetadata(md);
                sfs.changeConfiguration(sfsc);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error preparing for release", ex);
        }
    }

    @Override
    public void stop() {
    }

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        relName = ps.getString(PROP_REL_NAME);
    }


}
