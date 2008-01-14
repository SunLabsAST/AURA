package com.sun.labs.aura.aardvark.impl.recommender;

import com.sun.labs.aura.aardvark.recommender.RecommenderManager;
import com.sun.labs.aura.aardvark.store.Attention;
import com.sun.labs.aura.aardvark.store.ItemStore;
import com.sun.labs.aura.aardvark.store.item.Entry;
import com.sun.labs.aura.aardvark.store.item.Item;
import com.sun.labs.aura.aardvark.store.item.User;
import com.sun.labs.aura.aardvark.util.AuraException;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A recommender manager that returns the starred items for a user.
 */
public class SimpleRecommenderManager implements RecommenderManager {

    private ItemStore itemStore;

    private Logger log;

    public List<Entry> getRecommendations(User user) throws RemoteException {
        List<Entry> ret = new ArrayList<Entry>();
        try {
            List<Attention> attends = itemStore.getAttentionData(user);
            for(Attention a : attends) {
                Item item = itemStore.get(a.getItemID());

                if(item instanceof Entry) {
                    ret.add((Entry) itemStore.get(a.getItemID()));
                }
            }
        } catch(AuraException ex) {
            log.log(Level.SEVERE, "Error getting recommendations", ex);
        } finally {
            return ret;
        }
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        itemStore = (ItemStore) ps.getComponent(PROP_ITEM_STORE);
        log = ps.getLogger();
    }

    public void shutdown() throws RemoteException {
    }
    @ConfigComponent(type = com.sun.labs.aura.aardvark.store.ItemStore.class)
    public static final String PROP_ITEM_STORE = "itemStore";

}
