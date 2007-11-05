
package com.sun.labs.aura.aardvark.store;

import com.sun.labs.aura.aardvark.impl.store.MemoryItemStore;
import com.sun.labs.aura.aardvark.store.item.*;
import com.sun.labs.aura.aardvark.store.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import static org.junit.Assert.*;


/**
 * JUnit test for basic item store functionality
 * 
 * @author ja151348
 */
public class ItemStoreTest {
    public static MemoryItemStore mis;
    
    protected boolean gotCreated;
    protected boolean gotChanged;
    
    protected static List<Item> itemList;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        mis = new MemoryItemStore();
        itemList = new ArrayList();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        gotCreated = false;
        gotChanged = false;
    }

    @After
    public void tearDown() {
    }

    /**
     * Create items and make sure we get new IDs for each one
     * @throws java.lang.Exception
     */
    @Test public void createItems() throws Exception {
        User u = mis.newItem(User.class, "jalex");
        u.setRecommenderFeedKey("paul-is-a-rockstar");
        u.setStarredItemFeedURL(new URL("http://jalexurl.google.com/"));
        long id = u.getID();
        itemList.add(u);
        
        Entry e = mis.newItem(Entry.class, "pauls-blog-post1");
        e.setContent("music is awesome!");
        assertTrue(e.getID() == (++id));
        itemList.add(e);
        
        e = mis.newItem(Entry.class, "steves-blog-post1");
        e.setContent("search is awesome!");
        assertTrue(e.getID() == (++id));
        itemList.add(e);
    }
    
    /**
     * Add the items and make sure we get appropriate events for them.
     * @throws java.lang.Exception
     */
    @Test public void newItemEvents() throws Exception {
        //
        // This test checks that we get events when we add new items.
        ItemListener il = new MyListener();
        mis.addItemListener(User.class, il);
        
        //
        // Get a ceated event for a user
        Item i = itemList.get(0);
        mis.put(i);
        assertTrue("Failed to receive User create event", gotCreated);
        assertFalse("Got changed event when User was create", gotChanged);
        gotCreated = false;
        
        //
        // Get a changed event for a user
        mis.put(i);
        assertFalse("Got created event when a User was changed", gotCreated);
        assertTrue("Failed to receive User changed event", gotChanged);
        gotChanged = false;
        
        //
        // Don't get an event for an Entry (since we haven't registered for it)
        i = itemList.get(1);
        mis.put(i);
        assertFalse("Got created event for Entry but wasn't registered",
                    gotCreated);
        assertFalse("Got changed event for Entry but wasn't registered",
                    gotChanged);
        
        //
        // Now register for all events and make sure we get entry
        i = itemList.get(2);
        mis.addItemListener(null, il);
        mis.put(i);
        assertTrue("Failed to get created event for an Item", gotCreated);
        assertFalse("Got changed event when item was created", gotChanged);
    }
    
    /**
     * Retrieve some items and make sure they look how we expect
     * 
     * @throws java.lang.Exception
     */
    @Test public void retrieveItems() throws Exception {
        User u = (User)mis.get("jalex");
        assertTrue("User data (rFeedKey) got munged",
                   u.getRecommenderFeedKey().equals("paul-is-a-rockstar"));
        assertTrue("User data (sURL) got munged",
                   u.getStarredItemFeedURL().
                               equals(new URL("http://jalexurl.google.com/")));
        
        Entry e = (Entry)mis.get("pauls-blog-post1");
        assertTrue("Paul blog entry got munged",
                   e.getContent().equals("music is awesome!"));
        e = (Entry)mis.get("steves-blog-post1");
        assertTrue("Steve blog entry got munged",
                   e.getContent().equals("search is awesome!"));
        
        List<Entry> l = mis.getAll(Entry.class);
        assertTrue(l.size() == 2);
        
        ItemStoreStats stats = mis.getStats();
        assertTrue("Stat's numUsers should be 1, was " + stats.getNumUsers(),
                   stats.getNumUsers() == 1);
        assertTrue("Stat's numEntries should be 2", stats.getNumEntries() == 2);
    }
    
    /**
     * Item listener for item store
     */
    class MyListener implements ItemListener {
        public MyListener() {};
        public void itemCreated(ItemEvent e) {
            gotCreated = true;
        }

        public void itemChanged(ItemEvent e) {
            gotChanged = true;
        }

        public void itemDeleted(ItemEvent e) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
