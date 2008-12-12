/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.grid.aura;

import com.sun.caroline.platform.FileSystem;
import com.sun.caroline.platform.FileSystemMountParameters;
import com.sun.caroline.platform.ProcessRegistration;
import com.sun.labs.util.props.ConfigString;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.io.File;
import java.util.logging.Level;

/**
 *
 * @author jalex
 */
public class RewriteBDB extends Aura {

    @ConfigString
    public final static String PROP_REP = "replicant";
    protected String repPrefix = "";

    @Override
    public String serviceName() {
        return "ReadWriteBDB";
    }

    @Override
    public void start() {
        //
        // Use replicant to get the rep file system
        FileSystem repFS = repFSMap.get(repPrefix);
        if (repFS == null) {
            logger.severe("Failed to get FS for prefix: " + repPrefix);
            return;
        }

        FileSystemMountParameters mountParams = new FileSystemMountParameters(
                repFS.getUUID(),
                "data");
        //
        // Get this process registration and mount the appropriate filesystem
        ProcessRegistration pr = gu.lookupProcessRegistration("rewrite");
        try {
            pr.mountFileSystem(mountParams);
        } catch (Exception e) {
            logger.log(Level.SEVERE,
                    "Failed to mount rep FS for " + repPrefix, e);
            return;
        }

        //
        // Make a migrate class and start going
        String source = "/files/data/db";
        String dest = "/files/data/db-new";
        File s = new File(source);
        if (!s.exists()) {
            logger.severe("Source directory does not exist");
            return;
        }
        File d = new File(dest);
        if (!d.mkdir()) {
            //
            // Failed to make dest dir
            logger.severe("Failed to create destination directory");
            return;
        }
        try {
            com.sun.labs.aura.util.RewriteBDB rewriter
                    = new com.sun.labs.aura.util.RewriteBDB(source, dest);
            rewriter.migrate();
            rewriter.close();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to rewrite!", e);
        }
    }

    @Override
    public void stop() {
        
    }

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        repPrefix = ps.getString(PROP_REP);
    }


}
