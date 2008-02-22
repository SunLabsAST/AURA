/*
 *  Copyright 2007 Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES.
 */


package com.sun.labs.aura.aardvark.impl.recommender;

import com.sun.labs.aura.aardvark.AardvarkService;
import com.sun.labs.aura.aardvark.BlogEntry;
import com.sun.labs.aura.aardvark.recommender.RecommenderManager;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A recommender manager that returns the starred items for a user.
 */
public class SimpleRecommenderManager implements RecommenderManager, AardvarkService {

    private DataStore dataStore;

    private Logger log;

    public List<BlogEntry> getRecommendations(User user) throws RemoteException {
        List<BlogEntry> ret = new ArrayList<BlogEntry>();
        try {
            Set<Item> items = dataStore.getItems(user, Attention.Type.STARRED, Item.ItemType.BLOGENTRY);
            // we should probably sort these by date
            for(Item item : items) {
                ret.add((BlogEntry) item);
            }
        } catch(AuraException ex) {
            log.log(Level.SEVERE, "Error getting recommendations", ex);
        } finally {
            return ret;
        }
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        dataStore = (DataStore) ps.getComponent(PROP_DATA_STORE);
        log = ps.getLogger();
    }

    public void start() {
    }

    public void stop() {
    }

    @ConfigComponent(type = DataStore.class)
    public static final String PROP_DATA_STORE = "dataStore";
}
