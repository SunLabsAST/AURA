/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.grid.aura;

import com.sun.caroline.platform.FileSystem;
import com.sun.caroline.platform.FileSystemMountParameters;
import com.sun.caroline.platform.GridFactory;
import com.sun.caroline.platform.ProcessRegistration;
import com.sun.labs.util.props.ConfigBoolean;
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

    @ConfigString(defaultValue="all")
    public final static String PROP_REP = "replicant";
    protected String repPrefix = "";

    @ConfigBoolean(defaultValue=true)
    public static final String PROP_RENAME = "rename";
    private boolean rename;

    @Override
    public String serviceName() {
        return "ReadWriteBDB";
    }

    private void rewrite(FileSystem repFS) {
        FileSystemMountParameters mountParams = new FileSystemMountParameters(
                repFS.getUUID(),
                "data");

        //
        // Get this process registration and mount the appropriate filesystem
        ProcessRegistration pr = null;
        try {
            pr = gu.getGrid().getProcessRegistration(GridFactory.getProcessContext().getProcessRegistrationName());
            pr.mountFileSystem(mountParams);
        } catch(Exception e) {
            logger.log(Level.SEVERE,
                    "Failed to mount rep FS for " + repFS.getName(), e);
            return;
        }

        //
        // Make a migrate class and start going
        String source = "/files/data/db";
        String dest = "/files/data/db-new";
        File s = new File(source);
        if(!s.exists()) {
            logger.severe("Source directory does not exist");
            return;
        }
        File d = new File(dest);
        if(!d.mkdir()) {
            //
            // Failed to make dest dir
            logger.severe("Failed to create destination directory");
            return;
        }
        try {
            logger.info("Rewriting database on " + repFS.getName());
            com.sun.labs.aura.util.RewriteBDB rewriter =
                    new com.sun.labs.aura.util.RewriteBDB(source, dest);
            rewriter.migrate();
            rewriter.close();
            logger.info("Finished database on " + repFS.getName());
        } catch(Exception e) {
            logger.log(Level.SEVERE, "Failed to rewrite!", e);
            return;
        }

        if(rename) {
            try {
                File sf = new File(source);
                File df = new File(dest);
                File nsf = new File("/files/data/db-old");
                sf.renameTo(nsf);
                df.renameTo(sf);
                //
                // If we've re-written the DB and we're renaming, then we want to
                // move the index out of the way so that we can re-index at
                // startup.
                sf = new File("/files/data/itemIndex.idx");
                df = new File("/files/data/itemIndex.idx-old");
                sf.renameTo(df);
            } catch(Exception ex) {
                logger.log(Level.SEVERE, "Failed to rename databases!", ex);
                return;
            }
        }

        try {
            boolean success = pr.unmountFileSystem("data");
            logger.info("unmount: " + success);
        } catch(Exception ex) {
            logger.log(Level.SEVERE, "Failed to unmount file system", ex);
        }

    }
    @Override
    public void start() {
        if(repPrefix.equals("all")) {
            for(FileSystem repFS : repFSMap.values()) {
                rewrite(repFS);
            }
        } else {
            //
            // Use replicant to get the rep file system
            FileSystem repFS = repFSMap.get(repPrefix);
            if(repFS == null) {
                logger.severe("Failed to get FS for prefix: " + repPrefix);
                return;
            }
        }
    }

    @Override
    public void stop() {
        
    }

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        repPrefix = ps.getString(PROP_REP);
        rename = ps.getBoolean(PROP_RENAME);
    }


}
