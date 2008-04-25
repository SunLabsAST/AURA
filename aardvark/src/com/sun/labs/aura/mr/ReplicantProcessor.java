package com.sun.labs.aura.mr;

import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.impl.Replicant;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.util.props.Component;
import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * An interface for classes that want to be distributed and process the data
 * in a replicant.  Implementors of this class that wish to be used in a
 * distributed system should implement <code>Serializable</code>.
 */
public interface ReplicantProcessor extends Component, Remote {
    
    /**
     * Processes a replicant, extracting whatever data is required and possibly
     * writing an output file that can be used later.
     * @param rep the replicant to process
     * @param store the data store of which the replicant is part.  This can be
     * used to add or modify data in the replicant
     * @param outputDir a directory where an output file may be written
     * @return an output file.  If no output file is created, <code>null</code>
     * may be returned.
     */
    public File process(Replicant rep, DataStore store, File outputDir) 
            throws AuraException, RemoteException;
}
