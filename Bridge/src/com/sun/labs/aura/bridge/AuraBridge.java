/*
 * Copyright 2005-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 *
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 *
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 *
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package com.sun.labs.aura.bridge;

import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.Attention.Type;
import com.sun.labs.aura.datastore.AttentionConfig;
import com.sun.labs.aura.datastore.DBIterator;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.FieldCapability;
import com.sun.labs.aura.datastore.Item.FieldType;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.ItemListener;
import com.sun.labs.aura.datastore.SimilarityConfig;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.datastore.impl.store.ItemStore;
import com.sun.labs.aura.music.ArtistTagRaw;
import com.sun.labs.aura.recommender.TypeFilter;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Counted;
import com.sun.labs.aura.util.ItemAdapter;
import com.sun.labs.aura.util.RemoteComponentManager;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.minion.FieldFrequency;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.Configurable;
import com.sun.labs.util.props.ConfigurationManager;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author mailletf
 */
public class AuraBridge implements Configurable, ItemStore {

    private static final String DEFAULT_PROP_SHEET = "/com/sun/labs/aura/bridge/resource/bridgeConfig.xml";
    
    private static RemoteComponentManager rcmStore;
    private ConfigurationManager cm;

    private MusicDBBridge mdb;

    private Map<String, DBIterator> openIterators;

    public AuraBridge(String configFile) {
        try {
            URL configFileURL = AuraBridge.class.getResource(DEFAULT_PROP_SHEET);
            cm = new ConfigurationManager();
            cm.addProperties(configFileURL);
            rcmStore = new RemoteComponentManager(cm, DataStore.class);
            mdb = new MusicDBBridge(cm);

            openIterators = new HashMap<String, DBIterator>();

        } catch(IOException ex) {
            System.err.println("Error parsing configuration file: " + configFile);
            ex.printStackTrace();
        } catch(PropertyException ex) {
            System.err.println("Error parsing configuration file: " + configFile);
            ex.printStackTrace();
        } catch(Exception e) {
            System.err.println("Other error: " + e);
            e.printStackTrace();
        }
    }

    public AuraBridge() throws IOException {
        this(DEFAULT_PROP_SHEET);
    }

    /**
     * Gets the datastore
     * @return the datastore
     */
    private DataStore getDataStore() throws AuraException {
        try {
            return (DataStore) rcmStore.getComponent();
        } catch (NullPointerException e) {
            throw new NullPointerException("Cannot access datastore");
        }
    }

    public MusicDBBridge getMdb() {
        return mdb;
    }

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        rcmStore = new RemoteComponentManager(ps.getConfigurationManager(), DataStore.class);
    }


    @Override
    public void defineField(String fieldName) throws AuraException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void defineField(String fieldName, FieldType fieldType, EnumSet<FieldCapability> caps) throws AuraException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Item> getAll(ItemType itemType) throws AuraException, RemoteException {
        return getDataStore().getAll(itemType);
    }

    @Override
    public Item getItem(String key) throws AuraException, RemoteException {
        return getDataStore().getItem(key);
    }

    @Override
    public Collection<Item> getItems(Collection<String> keys) throws AuraException, RemoteException {
        return getDataStore().getItems(keys);
    }

    @Override
    public User getUser(String key) throws AuraException, RemoteException {
        return getDataStore().getUser(key);
    }

    @Override
    public User getUserForRandomString(String randStr) throws AuraException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteUser(String key) throws AuraException, RemoteException {
        getDataStore().deleteUser(key);
    }

    @Override
    public void deleteItem(String key) throws AuraException, RemoteException {
        getDataStore().deleteItem(key);
    }

    public void flushItem(ItemAdapter item) throws AuraException, RemoteException {
        item.flush(getDataStore());
    }

    @Override
    public Item putItem(Item item) throws AuraException, RemoteException {
        return getDataStore().putItem(item);
    }

    @Override
    public User putUser(User user) throws AuraException, RemoteException {
        return getDataStore().putUser(user);
    }

    @Override
    public List<Item> getItems(User user, Type attnType, ItemType itemType) throws AuraException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Attention> getAttention(AttentionConfig ac) throws AuraException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DBIterator<Attention> getAttentionIterator(AttentionConfig ac) throws AuraException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Long getAttentionCount(AttentionConfig ac) throws AuraException, RemoteException {
        return getDataStore().getAttentionCount(ac);
    }

    @Override
    public Object processAttention(AttentionConfig ac, String script, String language) throws AuraException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Attention> getAttentionSince(AttentionConfig ac, Date timeStamp) throws AuraException, RemoteException {
        return getDataStore().getAttentionSince(ac, timeStamp);
    }

    @Override
    public DBIterator<Attention> getAttentionSinceIterator(AttentionConfig ac, Date timeStamp) throws AuraException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Long getAttentionSinceCount(AttentionConfig ac, Date timeStamp) throws AuraException, RemoteException {
        return getDataStore().getAttentionSinceCount(ac, timeStamp);
    }

    @Override
    public List<Attention> getLastAttention(AttentionConfig ac, int count) throws AuraException, RemoteException {
        return getDataStore().getLastAttention(ac, count);
    }

    @Override
    public Attention attend(Attention att) throws AuraException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Attention> attend(List<Attention> attns) throws AuraException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeAttention(String srcKey, String targetKey, Type type) throws AuraException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addItemListener(ItemType itemType, ItemListener listener) throws AuraException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeItemListener(ItemType itemType, ItemListener listener) throws AuraException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long getItemCount(ItemType itemType) throws AuraException, RemoteException {
        return getDataStore().getItemCount(itemType);
    }

    public List<FieldFrequency> getTopValues(String field, int n,
            boolean ignoreCase) throws AuraException, RemoteException {
        return getDataStore().getTopValues(field, n, ignoreCase);
    }

    public List<Counted<String>> getTermCounts(String term, String field, int n, ItemType itemTypeFilter)
            throws AuraException, RemoteException {
        return getDataStore().getTermCounts(term, field, n, new TypeFilter(itemTypeFilter));
    }

    @Override
    public List<String> getSupportedScriptLanguages() throws AuraException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() throws AuraException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    public float getTagSimilarity(String key1, String key2) throws RemoteException, AuraException {
        return getSimilarity(key1, key2, ArtistTagRaw.FIELD_TAGGED_ARTISTS);
    }

    public float getSimilarity(String key1, String key2, String field) throws RemoteException, AuraException {
        return getDataStore().getSimilarity(key1, key2, field);
    }

    public List<Scored<String>> explainSimilarity(String key1, String key2,
            String field, int n) throws AuraException, RemoteException {
        return getDataStore().explainSimilarity(key1, key2, new SimilarityConfig(field, n));
    }


    /**********************
     * Iterator functions
     ***********************/

    @Override
    public DBIterator<Item> getAllIterator(ItemType itemType) throws AuraException, RemoteException {
        throw new UnsupportedOperationException("Not supported. Use initAllItemsIterator() instead.");
    }

    public String allItemsIteratorInit(ItemType itemType) throws RemoteException, AuraException {
        String itId = String.valueOf(new Date().getTime());
        openIterators.put(itId, getDataStore().getAllIterator(itemType));
        return itId;
    }

    @Override
    public DBIterator<Item> getItemsAddedSince(ItemType type, Date timeStamp) throws AuraException, RemoteException {
        throw new UnsupportedOperationException("Not supported. Use initGetItemsAddedSinceIterator() instead.");
    }

    public String initGetItemsAddedSinceIterator(ItemType type, Date timeStamp) throws AuraException, RemoteException {
        String itId = String.valueOf(new Date().getTime());
        openIterators.put(itId, getDataStore().getItemsAddedSince(type, timeStamp));
        return itId;
    }

    public Object iteratorNext(String itId) throws RemoteException {
        return openIterators.get(itId).next();
    }

    public boolean iteratorHasNext(String itId) throws RemoteException {
        return openIterators.get(itId).hasNext();
    }

    public void iteratorClose(String itId) throws RemoteException {
        if (openIterators.containsKey(itId)) {
            openIterators.get(itId).close();
            openIterators.remove(itId);
        }
    }

    public void iteratorCloseAll() throws RemoteException {
        for (DBIterator i : openIterators.values()) {
            i.close();
        }
        openIterators.clear();
    }


    
    /**
     * the configurable property for the itemstore used by this manager
     */
    @ConfigComponent(type = DataStore.class)
    public final static String PROP_DATA_STORE = "dataStore";


}
