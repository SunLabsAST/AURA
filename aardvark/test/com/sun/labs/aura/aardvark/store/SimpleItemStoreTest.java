package com.sun.labs.aura.aardvark.store;

import com.sun.labs.aura.aardvark.impl.store.bdb.BerkeleyItemStore;
import com.sun.labs.aura.aardvark.store.item.SimpleItem;
import com.sun.labs.aura.aardvark.store.item.SimpleItem.ItemType;
import com.sun.labs.aura.aardvark.store.item.SimpleUser;
import com.sun.labs.util.props.ConfigurationManager;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Run tests on an item store
 */
public class SimpleItemStoreTest {

    protected BerkeleyItemStore localStore = null;
    
    protected SimpleItemStore remoteStore = null;
    
    protected static Date runTime = new Date();
    
    protected HashSet<Attention> createdAttn = new HashSet<Attention>();
    
    protected ConfigurationManager mcm = null;
    
    protected ConfigurationManager rcm = null;
    
    public SimpleItemStoreTest() {
        
    }
    /*
    @BeforeClass
    public static void setUpClass() throws Exception {
        File f = new File("/tmp/aura-bdbtest");
        f.mkdir();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        File f = new File("/tmp/aura-bdbtest/dataStore.db");
        File[] content = f.listFiles();
        for (File c : content) {
            c.delete();
        }
        f.delete();
    }
    */
    
    @Before
    public void setUp() throws Exception {
        remoteStore = getFreshStore();
        assertTrue(remoteStore != null);
    }

    @After
    public void tearDown() throws Exception {
        localStore.closeAndDestroy();
        mcm.getComponentRegistry().unregister();
        rcm.getComponentRegistry().unregister();
    }
    
    private SimpleItemStore getFreshStore() throws IOException {
        //
        // First, get the local store for local control
        URL configFile = getClass().getResource("distMasterConfig.xml");
        mcm = new ConfigurationManager(configFile);
        localStore = (BerkeleyItemStore) mcm.lookup("dataStore");
        
        //
        // Now get the remote interface to the store
        configFile = getClass().getResource("distRemoteConfig.xml");
        rcm = new ConfigurationManager(configFile);
        remoteStore = (SimpleItemStore) rcm.lookup("dataStore");
        return remoteStore;
    }
        
    @Test
    public void addItems() throws Exception {
        SimpleUser u = SimpleItemFactory.newUser("user1@openid.sun.com",
                                                 "User One");
        HashMap<String,Serializable> map = u.getMap();
        assertTrue(map == null);
        map = new HashMap<String,Serializable>();
        map.put("Favorite Color", "Red");
        u.setMap(map);
        u = remoteStore.putUser(u);
        
        SimpleItem i = SimpleItemFactory.newItem(ItemType.FEED,
                "http://user1blogs.com/blog1", "User 1's Awesome blog");
        map = new HashMap<String,Serializable>();
        map.put("LastFetched", runTime);
        i.setMap(map);
        i = remoteStore.putItem(i);
        
        i = SimpleItemFactory.newItem(ItemType.FEED,
                "http://user1blogs.com/blog2", "User 1's second blog");
        map.put("NumErrors", 5);
        i.setMap(map);
        i = remoteStore.putItem(i);
        
        SimpleItem e = SimpleItemFactory.newItem(ItemType.BLOGENTRY,
                "http://user1blogs.com/blog1/someEntry", "Awesome blog entry");
        map = new HashMap<String,Serializable>();
        map.put("parentFeedID", i.getID());
        e.setMap(map);
        e = remoteStore.putItem(e);
    }
    
    @Test
    public void readItems() throws Exception {
        addItems();
        SimpleUser u = remoteStore.getUser("user1@openid.sun.com");
        assertTrue(u.getName().equals("User One"));
        HashMap<String,Serializable> map = u.getMap();
        String c = (String)map.get("Favorite Color");
        assertTrue(c.equals("Red"));
        
        SimpleItem i = remoteStore.getItem("http://user1blogs.com/blog1");
        assertTrue(i.getName().equals("User 1's Awesome blog"));
        map = i.getMap();
        Date d = (Date)map.get("LastFetched");
        assertTrue(d.equals(runTime));
        Integer numErr = (Integer)map.get("NumErrors");
        assertTrue(numErr == null);
        
        i = remoteStore.getItem("http://user1blogs.com/blog2");
        map = i.getMap();
        d = (Date)map.get("LastFetched");
        assertTrue(d.equals(runTime));
        numErr = (Integer)map.get("NumErrors");
        assertTrue(numErr.equals(5));
    }

    @Test
    public void makeAttention() throws Exception {
        addItems();
        SimpleUser u = remoteStore.getUser("user1@openid.sun.com");
        SimpleItem i = remoteStore.getItem("http://user1blogs.com/blog1");
        Attention att = new SimpleAttention(u, i,
                Attention.Type.SUBSCRIBED_FEED);
        remoteStore.attend(att);
        createdAttn.add(att);
        
        i = remoteStore.getItem("http://user1blogs.com/blog1/someEntry");
        att = new SimpleAttention(u, i, Attention.Type.STARRED);
        remoteStore.attend(att);
        createdAttn.add(att);        
    }
    
    @Test
    public void getAttention() throws Exception {
        makeAttention();
        DBIterator<Attention> dbit = remoteStore.getAttentionAddedSince(runTime);
        Set<Attention> dbattns = new HashSet<Attention>();
        while (dbit.hasNext()) {
            dbattns.add(dbit.next());
        }
        dbit.close();
        
        assertTrue(createdAttn.size() == dbattns.size());
        
        SimpleUser u = remoteStore.getUser("user1@openid.sun.com");
        dbattns = remoteStore.getAttention(u);
        assertTrue(createdAttn.size() == dbattns.size());
        
        SimpleItem i = remoteStore.getItem("http://user1blogs.com/blog1/someEntry");
        dbattns = remoteStore.getAttention(i);
        assertTrue(dbattns.size() == 1);
        Attention attn = (Attention) dbattns.toArray()[0];
        assertTrue(attn.getUserID() == u.getID());
        assertTrue(attn.getType().equals(Attention.Type.STARRED));
    }
    
    @Test
    public void changeItems() throws Exception {
        addItems();
        SimpleItem i = remoteStore.getItem("http://user1blogs.com/blog2");
        i.setName("Another blog");
        HashMap<String,Serializable> map = i.getMap();
        Integer numErrs = (Integer) map.get("NumErrors");
        assertTrue(numErrs.equals(5));
        map.put("NumErrors", 10);
        remoteStore.putItem(i);
        
        i = remoteStore.getItem("http://user1blogs.com/blog2");
        assertTrue(i.getName().equals("Another blog"));
        map = i.getMap();
        numErrs = (Integer) map.get("NumErrors");
        assertTrue(numErrs.equals(10));
    }
}
