package com.sun.labs.aura.aardvark.impl.bdb.store;

import com.sun.labs.aura.aardvark.impl.bdb.store.item.EntryImpl;
import com.sun.labs.aura.aardvark.impl.bdb.store.item.FeedImpl;
import com.sun.labs.aura.aardvark.impl.bdb.store.item.UserImpl;
import com.sun.labs.aura.aardvark.store.Attention;
import com.sun.labs.aura.aardvark.store.ItemStore;
import com.sun.labs.aura.aardvark.store.ItemStoreStats;
import com.sun.labs.aura.aardvark.store.item.Entry;
import com.sun.labs.aura.aardvark.store.item.Feed;
import com.sun.labs.aura.aardvark.store.item.Item;
import com.sun.labs.aura.aardvark.store.item.ItemListener;
import com.sun.labs.aura.aardvark.store.item.User;
import com.sun.labs.aura.aardvark.util.AuraException;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.util.List;

/**
 * This implementation of the ItemStore is backed by the Berkeley DB Java
 * Edition database.  It uses the Direct Persistence Layer to map its data
 * types into the database.
 */
public class BerkeleyItemStore implements ItemStore {

    public <T extends Item> T newItem(Class<T> itemType, String key)
            throws AuraException {
        T ret = null;
        if (itemType.equals(User.class)) {
            ret = (T)new UserImpl(key);
        } else if (itemType.equals(Feed.class)) {
            ret = (T)new FeedImpl(key);
        } else if (itemType.equals(Entry.class)) {
            ret = (T)new EntryImpl(key);
        }
        return ret;
    }

    /*public long getID(String itemKey) {
        throw new UnsupportedOperationException("Not supported yet.");
    }*/

    public <T extends Item> List<T> getAll(Class<T> itemType) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Item get(long id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Item get(String key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void put(Item item) throws AuraException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void attend(Attention att) throws AuraException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public <T extends Item> void addItemListener(Class<T> type, ItemListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public <T extends Item> void removeItemListener(Class<T> type, ItemListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ItemStoreStats getStats() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void newProperties(PropertySheet arg0) throws PropertyException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
