/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.impl.store;

import com.sun.labs.aura.aardvark.impl.store.item.EntryImpl;
import com.sun.labs.aura.aardvark.impl.store.item.FeedImpl;
import com.sun.labs.aura.aardvark.impl.store.item.ItemImpl;
import com.sun.labs.aura.aardvark.impl.store.item.UserImpl;
import com.sun.labs.aura.aardvark.store.Attention;
import com.sun.labs.aura.aardvark.store.ItemStore;
import com.sun.labs.aura.aardvark.store.ItemStoreStats;
import com.sun.labs.aura.aardvark.store.item.Entry;
import com.sun.labs.aura.aardvark.store.item.Feed;
import com.sun.labs.aura.aardvark.store.item.Item;
import com.sun.labs.aura.aardvark.store.item.ItemEvent;
import com.sun.labs.aura.aardvark.store.item.ItemListener;
import com.sun.labs.aura.aardvark.store.item.User;
import com.sun.labs.aura.aardvark.util.AuraException;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A simple item store implementation that keeps all items in memory.
 * Rev B will also serialize its data out to disk.
 * 
 * @author ja151348
 */
public class MemoryItemStore implements ItemStore {

    protected AtomicLong currItemID;
    
    protected Map<String,Item> keyToItem;
    
    protected Map<Long, Item> idToItem;
    
    protected Map<Class, List> typeToItems;
    
    protected Map<Class, List<ItemListener>> typeToListeners;
    
    public MemoryItemStore() {
        currItemID = new AtomicLong(0);
        keyToItem = new ConcurrentHashMap();
        idToItem = new ConcurrentHashMap();
        typeToItems = new ConcurrentHashMap();
        typeToListeners = new ConcurrentHashMap();
    }
    
    public <T extends Item> T newItem(Class<T> itemType, String key)
            throws AuraException {
        T ret = null;
        if (itemType.equals(User.class)) {
            ret = (T)new UserImpl(nextItemID(), key);
        } else if (itemType.equals(Feed.class)) {
            ret = (T)new FeedImpl(nextItemID(), key);
        } else if (itemType.equals(Entry.class)) {
            ret = (T)new EntryImpl(nextItemID(), key);
        }
        return ret;
    }

    public long getID(String key) {
        Item i = keyToItem.get(key);
        return i.getID();
    }

    public Item get(long id) {
        return idToItem.get(id);
    }

    public Item get(String key) {
        return keyToItem.get(key);
    }

    public void put(Item item) throws AuraException {
        String key = item.getKey();
        long id = item.getID();
        
        //
        // "index" this item
        boolean created = false;
        if (!keyToItem.containsKey(key)) {
            created = true;
        }
        keyToItem.put(key, item);
        idToItem.put(id, item);
        
        //
        // and store it by type as well
        List l = typeToItems.get(item.getClass());
        if (l == null) {
            l = Collections.synchronizedList(new ArrayList());
            l.add(item);
            typeToItems.put(item.getClass(), l);
        } else {
            if (!l.contains(item)) {
                l.add(item);
            }
        }
        
        //
        // Finally, notify the listeners that a change has occurred (both the
        // listeners for this type, and for all items)
        Set notified = new HashSet();
        Class typeInList = getMatchingKey(item.getClass(), typeToListeners);
        if (typeInList != null) {
            List<ItemListener> listeners = typeToListeners.get(typeInList);
            for (ItemListener il : listeners) {
                notified.add(il);

                if (created) {
                    il.itemCreated(new ItemEvent(new Item[] {item}));
                } else {
                    il.itemChanged(new ItemEvent(new Item[]{item},
                                   ItemEvent.ChangeType.AURA));
                }
            }
        }
        
        List<ItemListener> listeners = typeToListeners.get(Item.class);
        if (listeners != null) {
            for (ItemListener il : listeners) {
                if (!notified.contains(il)) {
                    if (created) {
                        il.itemCreated(new ItemEvent(new Item[] {item}));
                    } else {
                        il.itemChanged(new ItemEvent(new Item[] {item},
                                       ItemEvent.ChangeType.AURA));
                    }
                }
            }
        }
    }

    public void attend(Attention att) throws AuraException {
        //
        // Update the attention
        UserImpl u = (UserImpl)get(att.getUserID());
        ItemImpl i = (ItemImpl)get(att.getItemID());
        u.attend(att);
        i.attend(att);
        
        //
        // Notify interested parties of the change
        Set notified = new HashSet();
        Class typeInList = getMatchingKey(i.getClass(), typeToListeners);
        if (typeInList != null) {
            List<ItemListener> listeners = typeToListeners.get(typeInList);
            for (ItemListener il : listeners) {
                notified.add(il);
                il.itemChanged(new ItemEvent(new Item[] {i, u},
                                             ItemEvent.ChangeType.ATTENTION));
            }
        }
        
        List<ItemListener> listeners = typeToListeners.get(Item.class);
        if (listeners != null) {
            for (ItemListener il : listeners) {
                if (!notified.contains(il)) {
                    il.itemChanged(new ItemEvent(new Item[] {i, u},
                                           ItemEvent.ChangeType.ATTENTION));
                }
            }
        }
    }


    public <T extends Item> void addItemListener(Class<T> type,
                                                 ItemListener listener) {
        //
        // If no type was specified, put this in the all-items listener list.
        // Otherwise, put it in the right list for the type
        
        List l;
        if (type != null) {
            l = typeToListeners.get(type);
        } else {
            l = typeToListeners.get(Item.class);
        }
        
        if (l == null) {
            l = Collections.synchronizedList(new ArrayList());
            l.add(listener);
            if (type != null) {
                typeToListeners.put(type, l);
            } else {
                typeToListeners.put(Item.class, l);
            }
        } else {
            l.add(listener);
        }
    }

    public <T extends Item> void removeItemListener(Class<T> type,
                                                    ItemListener listener) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected long nextItemID() {
        return currItemID.incrementAndGet();
    }

    public <T extends Item> List<T> getAll(Class<T> itemType) {
        List<T> ret = new ArrayList();
        Class typeInList = null;
        for (Class c : typeToItems.keySet()) {
            if (itemType.isAssignableFrom(c)) {
                typeInList = c;
            }
        }
        if (typeInList != null) {
            ret.addAll(typeToItems.get(typeInList));
        }
        return ret;
    }

    public ItemStoreStats getStats() {
        long numAtt = 0;
        List<User> users = getAll(User.class);
        for (User u : users) {
            List attns = u.getAttentionData();
            numAtt += attns.size();
        }
        List<Entry> entries = getAll(Entry.class);
        return new ItemStoreStats(users.size(), entries.size(), numAtt);
    }
    
    public void newProperties(PropertySheet arg0) throws PropertyException {
        
    }
    
    protected Class getMatchingKey(Class subType, Map<Class,?> m) {
        for (Class c : m.keySet()) {
            if (c.isAssignableFrom(subType)) {
                return c;
            }
        }
        return null;
    }
}
