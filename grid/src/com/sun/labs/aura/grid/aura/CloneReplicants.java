/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.grid.aura;

import com.sun.caroline.platform.BaseFileSystem;
import com.sun.caroline.platform.BaseFileSystemConfiguration;
import com.sun.caroline.platform.SnapshotFileSystem;
import com.sun.labs.util.props.ConfigBoolean;
import com.sun.labs.util.props.ConfigInteger;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 */
public class CloneReplicants extends Aura {

    /**
     * The number of bits that we want the prefix of our cloned replicants to
     * have.
     */
    @ConfigInteger(defaultValue = 4)
    public static final String PROP_N_BITS = "nBits";

    private int nBits;

    @ConfigString(defaultValue = "devel")
    public static final String PROP_NEW_INSTANCE = "newInstance";
    private String newInstance;

    @ConfigBoolean(defaultValue = false)
    public static final String PROP_CONVERT_FOR_HA = "convertForHA";
    private boolean convertForHA;

    @Override
    public String serviceName() {
        return getClass().getSimpleName();
    }

    private void clone(BaseFileSystem fs, String np) throws Exception {
        String idStr = np;
        if (convertForHA) {
            idStr += ":A";
        }
        String snapName = String.format("%s-replicant-%s-snap",
                                        newInstance, idStr);

        String cloneName = String.format("%s-replicant-%s",
                                         newInstance, idStr);

        logger.info(String.format("Snapshotting %s", fs.getName()));
        SnapshotFileSystem sfs = fs.createSnapshot(snapName);
        BaseFileSystemConfiguration config = fs.getConfiguration();

        //
        // Update the metadata in the configuration.
        Map<String, String> mdm = config.getMetadata();
        mdm.put("prefix", np);
        mdm.put("instance", newInstance);
        mdm.put("type", "replicant");
        if (convertForHA) {
            mdm.put("nodeName", "A");
        }
        config.setMetadata(mdm);
        BaseFileSystem cfs = sfs.createClone(cloneName, config);
    }

    @Override
    public void start() {

        //
        // The number of replicants that we'll be keeping.
        int keeping = 1 << nBits;

        //
        // Get the current list of file systems, sorted by their prefix.
        List<String> rfs = new ArrayList<String>(repFSMap.keySet());
        Collections.sort(rfs);

        if(keeping > rfs.size()) {
            logger.severe(String.format(
                    "%d bits requires %d replicants, we only have %d!",
                    nBits, keeping, rfs.size()));
            return;
        }
        logger.info(String.format("Got filesystems: %s", rfs));

        for(int i = 0; i < keeping; i++) {
            String prefix = rfs.get(i);
            String np = prefix.substring(prefix.length() - nBits);
            try {
                clone((BaseFileSystem) repFSMap.get(prefix), np);
            } catch(Exception ex) {
                logger.log(Level.SEVERE, "Exception creating replicant for " +
                        np, ex);
            }
        }
    }

    @Override
    public void stop() {
    }

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        nBits = ps.getInt(PROP_N_BITS);
        newInstance = ps.getString(PROP_NEW_INSTANCE);
        convertForHA = ps.getBoolean(PROP_CONVERT_FOR_HA);
    }
}
