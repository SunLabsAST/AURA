package com.sun.labs.aura.aardvark.store;

import com.sun.labs.aura.aardvark.impl.bdb.store.BerkeleyItemStore;
import com.sun.labs.aura.aardvark.store.item.Entry;
import com.sun.labs.aura.aardvark.store.item.Feed;
import com.sun.labs.aura.aardvark.store.item.ItemEvent;
import com.sun.labs.aura.aardvark.store.item.ItemListener;
import com.sun.labs.aura.aardvark.store.item.User;
import com.sun.labs.aura.aardvark.util.AuraException;
import com.sun.labs.util.props.ConfigurationManager;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * A class that performs some tests of the item store
 */
public class BerkeleyItemStoreTest {

    protected BerkeleyItemStore store = null;

    public BerkeleyItemStoreTest() {
        
    }
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        File f = new File("/tmp/aura");
        f.mkdir();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        File f = new File("/tmp/aura");
        File[] content = f.listFiles();
        for (File c : content) {
            c.delete();
        }
        f.delete();
    }

    @Before
    public void setUp() throws Exception {
        store = getStore();
    }

    @After
    public void tearDown() throws Exception {
        store.close();
    }
    
    /**
     * Create items and make sure we get new IDs for each one
     * @throws java.lang.Exception
     */
    @Test
    public void a_addItems() throws Exception {
        MyListener listener = new MyListener();
        store.addItemListener(null, listener);
        User u = store.newItem(User.class, "jalex");
        u.setRecommenderFeedKey("paul-is-a-rockstar");
        u.setStarredItemFeedURL(new URL("http://jalexurl.google.com/"));
        store.put(u);
        long id = u.getID();
        assertTrue(id > 0);
        assertTrue(listener.gotCreated);
        assertFalse(listener.gotChanged);
        listener.gotCreated = false;
        
        Entry e = store.newItem(Entry.class, "pauls-blog-post1");
        e.setContent("music is awesome!");
        store.put(e);
        assertTrue(e.getID() == ++id);
        assertTrue(listener.gotCreated);
        assertFalse(listener.gotChanged);
        listener.gotCreated = false;

        e = store.newItem(Entry.class, "steves-blog-post1");
        e.setContent("search is awesome!");
        store.put(e);
        assertTrue(e.getID() == ++id);
        assertTrue(listener.gotCreated);
        assertFalse(listener.gotChanged);
        listener.gotCreated = false;
    }

    @Test
    public void b_getItems() throws AuraException {
        User u = (User) store.get("jalex");
        assertTrue(u.getRecommenderFeedKey().equals("paul-is-a-rockstar"));
        assertTrue(u.getStarredItemFeedURL().toString().
                equals("http://jalexurl.google.com/"));
        assertTrue(u.getID() == 1);
        
        Entry e = (Entry) store.get(2);
        assertTrue(e.getKey().equals("pauls-blog-post1"));
        assertTrue(e.getContent().equals("music is awesome!"));
        
        e = (Entry) store.get("steves-blog-post1");
        assertTrue(e.getID() == 3);
        assertTrue(e.getContent().equals("search is awesome!"));
    }

    @Test
    public void c_changeItems() throws Exception {
        MyListener l = new MyListener();
        store.addItemListener(User.class, l);
        User u = (User) store.get("jalex");
        u.setRecommenderFeedKey("rockstar-is-paul");
        store.put(u);
        assertTrue(l.gotChanged);
        assertFalse(l.gotCreated);
        l.gotChanged = false;
        
        u = (User) store.get("jalex");
        assertTrue(u.getRecommenderFeedKey().equals("rockstar-is-paul"));
        assertTrue(u.getID() == 1);
        
        store.removeItemListener(User.class, l);
        u.setStarredItemFeedURL(new URL("http://newurl.google.com/"));
        store.put(u);
        assertFalse(l.gotChanged);
        assertFalse(l.gotCreated);
        
        store.addItemListener(Entry.class, l);
        Entry e = (Entry) store.get("steves-blog-post1");
        e.setContent("search is super awesome!");
        store.put(e);
        assertTrue(l.gotChanged);
        assertFalse(l.gotCreated);
        l.gotChanged = false;
        e = (Entry) store.get("steves-blog-post1");
        assertTrue(e.getContent().equals("search is super awesome!"));
    }
    
    @Test
    public void d_attendItems() throws AuraException {
        User u = (User) store.get("jalex");
        Entry e = (Entry) store.get(2);
        List l = u.getAttentionData();
        assertTrue(l.isEmpty());
        l = e.getAttentionData();
        assertTrue(l.isEmpty());
        SimpleAttention sattn = new SimpleAttention(u, e,
                                                    Attention.Type.STARRED);
        store.attend(sattn);
        // refresh objects
        u = (User) store.get(u.getID());
        e = (Entry) store.get(e.getID());
        l = u.getAttentionData();
        assertTrue(l.size() == 1);
        Attention a = (Attention) l.get(0);
        
        l = e.getAttentionData();
        assertTrue(l.size() == 1);
        Attention b = (Attention) l.get(0);
        assertTrue(a.equals(b));
    }
    
    public void e_multipleFeeds() throws AuraException {
        //
        // Make some data
        User u = (User) store.get("jalex");
        Feed f1 = store.newItem(Feed.class, "http://pauls.blog/");
        f1.setNumPulls(10);
        store.put(f1);
        u.addFeed(f1, Attention.Type.SUBSCRIBED_FEED);
        Feed f2 = store.newItem(Feed.class, "http://steves.blog/");
        f2.setNumPulls(1);
        f2.setNumErrors(1);
        store.put(f2);
        u.addFeed(f2, Attention.Type.STARRED_FEED);
        
        //
        // Check that we get those items when we ask for all attentions
        u = (User)store.get("jalex");
        boolean found1 = false, found2 = false;
        List<Attention> attns = u.getAttentionData();
        for (Attention attn : attns) {
            if (attn.getItemID() == f1.getID()) {
                found1 = true;
                assertTrue("Wrong attention type!",
                        attn.getType() == Attention.Type.SUBSCRIBED_FEED);
            }
            if (attn.getItemID() == f2.getID()) {
                found2 = true;
                assertTrue("Wrong attention type!",
                        attn.getType() == Attention.Type.STARRED_FEED);
            }
        }
        
        //
        // Check that we get the right feeds by type
        Set<Feed> feeds = u.getFeeds(Attention.Type.SUBSCRIBED_FEED);
        assertTrue("Got wrong number of subscribed feeds", feeds.size() == 1);
        Feed test = (Feed)feeds.toArray()[0];
        assertTrue("Wrong feed for subscribed test",
                test.getKey().equals("http://pauls.blog/"));
        feeds = u.getFeeds(Attention.Type.STARRED_FEED);
        assertTrue("Got wrong number of starred feeds", feeds.size() == 1);
        test = (Feed)feeds.toArray()[0];
        assertTrue("Wrong feed for starred test",
                test.getKey().equals("http://steves.blog"));
    }
    
    private BerkeleyItemStore getStore() throws IOException {
        ConfigurationManager cm = new ConfigurationManager();
        URL configFile = this.getClass().getResource("berkeleyConfig.xml");
        cm.addProperties(configFile);
        return (BerkeleyItemStore) cm.lookup("itemStore");

    }
    
    /**
     * Item listener for item store
     */
    class MyListener implements ItemListener {
        public boolean gotCreated = false;
        public boolean gotChanged = false;
        
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
