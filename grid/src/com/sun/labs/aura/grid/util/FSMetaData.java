
package com.sun.labs.aura.grid.util;

import com.sun.caroline.platform.BaseFileSystem;
import com.sun.caroline.platform.BaseFileSystemConfiguration;
import com.sun.labs.aura.grid.ServiceAdapter;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.logging.Level;

/**
 * Edit filesystem meta data
 * @author jalex
 */
public class FSMetaData extends ServiceAdapter {
    public String serviceName() {
        return "FSMetaData";
    }
    
    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
    }

    public void start() {
        BaseFileSystem fs;
        try {
            fs = (BaseFileSystem)grid.getFileSystem("jeff-replicant-00");
        } catch(RemoteException ex) {
            logger.severe("Error getting filesystem");
            return;
        }
        if(fs == null) {
            logger.severe("No such filesystem");
            return;
        }
        
        BaseFileSystemConfiguration fsc = fs.getConfiguration();
        Map<String,String> md = fsc.getMetadata();
        md.put("prefix", "00");
        fsc.setMetadata(md);
        try {
            fs.changeConfiguration(fsc);
        } catch (Exception rx) {
            logger.log(Level.SEVERE, "Error setting file system metadata for " + fs.getName(), rx);
        }
        logger.info("Done");
    }

    public void stop() {
    }


}
