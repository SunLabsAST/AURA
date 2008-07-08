/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.grid.rp;

import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.grid.ServiceAdapter;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.ConfigInteger;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class for processing (some of) the contents of a data store on a 
 * replicant-by-replicant basis.
 */
public class DataStoreProcessor extends ServiceAdapter {

    @ConfigComponent(type=com.sun.labs.aura.datastore.DataStore.class)
    public static final String PROP_DATA_STORE = "dataStore";
    
    private DataStore dataStore;
    
    @ConfigInteger(defaultValue=4)
    public static final String PROP_NUM_PROCESSORS = "numProcessors";
    
    private int numProcessors;
    
    private List<String> prefixes;
    
    @Override
    public String serviceName() {
        return "DataStoreProcessor";
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        dataStore = (DataStore) ps.getComponent(PROP_DATA_STORE);
        numProcessors = ps.getInt(PROP_NUM_PROCESSORS);
        try {
            prefixes = dataStore.getPrefixes();
        } catch(RemoteException ex) {
            throw new PropertyException(ex, ps.getInstanceName(), PROP_DATA_STORE, "Error getting prefixes from data store");
        }
    }
    
    public void start() {
    }

    public void stop() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
