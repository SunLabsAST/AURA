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
 * A grid service that will clean up from a previous release, removing the
 * snapshots associated with the release.
 */
public class CleanupRelease extends Aura {

    @ConfigString(defaultValue="rel")
    public static final String PROP_REL_NAME = "relName";

    private String relName;
    
    @Override
    public String serviceName() {
        return "CleanupRelease";
    }

    @Override
    public void start() {

        try {
            //
            // Snapshot the dist file system.
            FileSystem fs = gu.getFS("aura.dist" + "-" + relName, false);
            if(fs != null) {
                fs.destroy();
            }

            //
            // Snapshot the logs.
            fs = gu.getFS("aura.logs" + "-" + relName, false);
            if(fs != null) {
                fs.destroy();
            }

            //
            // Snapshot the cache filesystem.
            fs = gu.getFS("cache" + "-" + relName, false);
            if(fs != null) {
                fs.destroy();
            }
            
            //
            // Snapshot the replicant file systems.
            for(String prefix : repFSMap.keySet()) {
                fs = gu.getFS("replicant" + "-" + prefix + "-" + relName, false);
                if(fs != null) {
                    fs.destroy();
                }
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error cleaning up release", ex);
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
