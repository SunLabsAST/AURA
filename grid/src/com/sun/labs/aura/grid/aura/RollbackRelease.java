package com.sun.labs.aura.grid.aura;

import com.sun.caroline.platform.BaseFileSystem;
import com.sun.caroline.platform.FileSystem;
import com.sun.caroline.platform.SnapshotFileSystem;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.util.UUID;
import java.util.logging.Level;

/**
 * A grid service that will roll back the code and data to a previous release.
 */
public class RollbackRelease extends Aura {

    @ConfigString(defaultValue = "rel")
    public static final String PROP_REL_NAME = "relName";

    private String relName;

    @Override
    public String serviceName() {
        return "CleanupRelease";
    }

    private void rollbackWithDestroy(SnapshotFileSystem sfs) throws Exception {
        UUID ours = sfs.getUUID();

        BaseFileSystem fs = (BaseFileSystem) gu.getGrid().getResource(sfs.getParent());
        logger.info(String.format("Base FileSystem: " + fs.getName()));
        for(UUID su : fs.getSnapshots()) {
            if(!su.equals(ours)) {
                FileSystem dfs = (FileSystem) gu.getGrid().getResource(su);
                logger.info(String.format("Destroying " + dfs.getName()));
                dfs.destroy();
            }
        }

        logger.info(String.format("Rolling back: " + sfs.getName()));
        sfs.rollbackParent();
    }

    @Override
    public void start() {

        try {
            //
            // Snapshot the dist file system.
            SnapshotFileSystem fs = (SnapshotFileSystem) gu.getFS("aura.dist" +
                    "-" + relName, false);
            if(fs != null) {
                fs.rollbackParent();
            }

            //
            // Snapshot the logs.
            fs = (SnapshotFileSystem) gu.getFS("aura.logs" + "-" + relName,
                    false);
            if(fs != null) {
                rollbackWithDestroy(fs);
            }

            //
            // Snapshot the cache filesystem.
            fs = (SnapshotFileSystem) gu.getFS("cache" + "-" + relName, false);
            if(fs != null) {
                rollbackWithDestroy(fs);
            }

            //
            // Snapshot the replicant file systems.
            for(String prefix : repFSMap.keySet()) {
                fs = (SnapshotFileSystem) gu.getFS("replicant" + "-" + prefix +
                        "-" + relName, false);
                if(fs != null) {
                    rollbackWithDestroy(fs);
                }
            }
        } catch(Exception ex) {
            logger.log(Level.SEVERE, "Error rolling back release", ex);
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
