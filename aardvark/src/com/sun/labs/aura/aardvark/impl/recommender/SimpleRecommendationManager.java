package com.sun.labs.aura.aardvark.impl.recommender;

import com.sun.labs.aura.aardvark.recommender.RecommenderManager;
import com.sun.labs.aura.aardvark.store.Attention;
import com.sun.labs.aura.aardvark.store.ItemStore;
import com.sun.labs.aura.aardvark.store.item.Entry;
import com.sun.labs.aura.aardvark.store.item.User;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.util.ArrayList;
import java.util.List;

/**
 * A recommender manager that returns the starred items for a user.
 */
public class SimpleRecommendationManager implements RecommenderManager {

    private ItemStore itemStore;
    
    public List<Entry> getRecommendations(User user) {
        List<Attention> attends = user.getAttentionData();
        List<Entry> ret = new ArrayList<Entry>(attends.size());
        for(Attention a : attends) {
            ret.add((Entry) itemStore.get(a.getItemID()));
        }
        return ret;
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        itemStore = (ItemStore) ps.getComponent(PROP_ITEM_STORE);
    }
    
    @ConfigComponent(type=com.sun.labs.aura.aardvark.store.ItemStore.class)
    public static final String PROP_ITEM_STORE = "itemStore";
}
