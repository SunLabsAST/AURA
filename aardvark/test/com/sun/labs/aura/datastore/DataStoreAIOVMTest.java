package com.sun.labs.aura.datastore;

import com.sun.labs.aura.AuraServiceStarter;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.util.props.ConfigurationManager;
import com.sun.labs.util.props.PropertyException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for an all-in-one-VM data store.
 */
public class DataStoreAIOVMTest {

    AuraServiceStarter starter;
    DataStore store;
    static Date runTime = new Date();
    HashSet<Attention> createdAttn = new HashSet<Attention>();

    String auraHome = "/aura";
    
    public DataStoreAIOVMTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        
        try {
            //
            // Start up the services in the configuration and then get our
            // data store.
            ConfigurationManager cm =
                    new ConfigurationManager(getClass().
                    getResource("/com/sun/labs/aura/resource/dataStoreAIOVMConfig.xml"));
            starter = (AuraServiceStarter) cm.lookup("starter");
            store = (DataStore) cm.lookup("dataStoreHead");
        } catch(IOException ex) {
            Logger.getLogger(DataStoreAIOVMTest.class.getName()).
                    log(Level.SEVERE, null, ex);
        } catch(PropertyException ex) {
            Logger.getLogger(DataStoreAIOVMTest.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
        
    }

    @After
    public void tearDown() {
        starter.stopServices();
        
        //
        // Delete anything from disk.
        rmDir(new File(auraHome + "/0"));
        rmDir(new File(auraHome + "/1"));
    }

    @Test
    public void addItems() throws Exception {
        User u = StoreFactory.newUser("user1@openid.sun.com",
                                                 "User One");
        HashMap<String,Serializable> map;
        map = new HashMap<String,Serializable>();
        map.put("Favorite Color", "Red");
        u.setMap(map);
        u = store.putUser(u);
        
        Item i = StoreFactory.newItem(ItemType.FEED,
                "http://user1blogs.com/blog1", "User 1's Awesome blog");
        map = new HashMap<String,Serializable>();
        map.put("LastFetched", runTime);
        i.setMap(map);
        i = store.putItem(i);
        
        i = StoreFactory.newItem(ItemType.FEED,
                "http://user1blogs.com/blog2", "User 1's second blog");
        map.put("NumErrors", 5);
        i.setMap(map);
        i = store.putItem(i);
        
        Item e = StoreFactory.newItem(ItemType.BLOGENTRY,
                "http://user1blogs.com/blog1/someEntry", "Awesome blog entry");
        map = new HashMap<String,Serializable>();
        map.put("parentFeedKey", i.getKey());
        e.setMap(map);
        e = store.putItem(e);
    }

    @Test
    public void readItems() throws Exception {
        addItems();
        User u = store.getUser("user1@openid.sun.com");
        assertTrue(u.getName().equals("User One"));
        HashMap<String,Serializable> map = u.getMap();
        String c = (String)map.get("Favorite Color");
        assertTrue(c.equals("Red"));
        
        Item i = store.getItem("http://user1blogs.com/blog1");
        assertTrue(i.getName().equals("User 1's Awesome blog"));
        map = i.getMap();
        Date d = (Date)map.get("LastFetched");
        assertTrue(d.equals(runTime));
        Integer numErr = (Integer)map.get("NumErrors");
        assertTrue(numErr == null);
        
        i = store.getItem("http://user1blogs.com/blog2");
        map = i.getMap();
        d = (Date)map.get("LastFetched");
        assertTrue(d.equals(runTime));
        numErr = (Integer)map.get("NumErrors");
        assertTrue(numErr.equals(5));
    }

    @Test
    public void makeAttention() throws Exception {
        addItems();
        User u = store.getUser("user1@openid.sun.com");
        Item i = store.getItem("http://user1blogs.com/blog1");
        Attention att = StoreFactory.newAttention(u, i,
                Attention.Type.SUBSCRIBED_FEED);
        store.attend(att);
        createdAttn.add(att);
        
        i = store.getItem("http://user1blogs.com/blog1/someEntry");
        att = StoreFactory.newAttention(u, i, Attention.Type.STARRED);
        store.attend(att);
        createdAttn.add(att);        
    }
    
    @Test
    public void getAttention() throws Exception {
        makeAttention();
        DBIterator<Attention> dbit = store.getAttentionAddedSince(runTime);
        Set<Attention> dbattns = new HashSet<Attention>();
        try {
            while (dbit.hasNext()) {
                dbattns.add(dbit.next());
            }
        } finally {
            dbit.close();
        }
        
        assertTrue(createdAttn.size() == dbattns.size());
        
        User u = store.getUser("user1@openid.sun.com");
        dbattns = store.getAttentionForSource(u.getKey());
        assertTrue(createdAttn.size() == dbattns.size());
        
        Item i = store.getItem("http://user1blogs.com/blog1/someEntry");
        dbattns = store.getAttentionForTarget(i.getKey());
        assertTrue(dbattns.size() == 1);
        Attention attn = (Attention) dbattns.toArray()[0];
        assertTrue(attn.getSourceKey().equals(u.getKey()));
        assertTrue(attn.getType().equals(Attention.Type.STARRED));
    }
    
    @Test
    public void changeItems() throws Exception {
        addItems();
        Item i = store.getItem("http://user1blogs.com/blog2");
        i.setName("Another blog");
        HashMap<String,Serializable> map = i.getMap();
        Integer numErrs = (Integer) map.get("NumErrors");
        assertTrue(numErrs.equals(5));
        map.put("NumErrors", 10);
        store.putItem(i);
        
        i = store.getItem("http://user1blogs.com/blog2");
        assertTrue(i.getName().equals("Another blog"));
        map = i.getMap();
        numErrs = (Integer) map.get("NumErrors");
        assertTrue(numErrs.equals(10));
    }
    
    public void rmDir(File dir) {
        File[] content = dir.listFiles();
        for(File c : content) {
            if (c.isDirectory()) {
                rmDir(c);
            } else {
                c.delete();
            }
        }
        dir.delete();
    }
}