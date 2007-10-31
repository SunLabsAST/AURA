
/*
 * FeedCrawler.java
 * 
 * Created on Oct 28, 2007, 9:00:00 PM
 * 
 */
package com.sun.labs.aura.aardvark.crawler;

import com.sun.labs.aura.aardvark.store.Attention;
import com.sun.labs.aura.aardvark.store.item.Entry;
import com.sun.labs.aura.aardvark.store.item.Item;
import com.sun.labs.aura.aardvark.store.ItemStore;
import com.sun.labs.aura.aardvark.store.item.User;
import com.sun.labs.aura.aardvark.store.SimpleAttention;
import com.sun.labs.aura.aardvark.store.item.User;
import com.sun.labs.aura.aardvark.util.AuraException;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import edu.cmu.sphinx.util.props.Configurable;
import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;
import edu.cmu.sphinx.util.props.S4Component;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The FeedCrawler is responsible for crawling rss/atom feeds and adding
 * items and attention data associated with the feeds to the item store.
 * 
 * Specific tasks:
 * <ul>
 *  <li> Noticing when a new user item is added to the item store and adding that
 *       user's 'starred item feed' item to the set of feeds in the item store 
 *       if it doesn't already exist
 *  </li>
 *  <li> For each feed in the item store:
 *      <ul>
 *          <li>Periodically fetch the feed,  
 *      </ul>
 *  </li>
 * </ul>
 *    
 * @author plamere
 */
public class FeedCrawler implements Configurable {

    /**
     * the configurable property for the itemstore used by this manager
     */
    @S4Component(type = UserRefreshManager.class)
    public final static String PROP_USER_REFRESH_MANAGER = "userRefreshManager";
    private UserRefreshManager userRefreshManager;

    @S4Component(type = ItemStore.class)
    public final static String PROP_ITEM_STORE = "itemStore";
    private ItemStore itemStore;

    private final static Entry[] EMPTY_ENTRY = new Entry[0];

    private SyndFeedInput syndFeedInput = new SyndFeedInput();

    private volatile Thread crawlerThread;


    public FeedCrawler() {
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        userRefreshManager = (UserRefreshManager) ps.getComponent(PROP_USER_REFRESH_MANAGER);
        itemStore = (ItemStore) ps.getComponent(PROP_ITEM_STORE);
    }

    public void start() {
    }

    public void stop() {
    }

    public void run() {
        while (crawlerThread != null) {
            User user = null;
            try {
                user = userRefreshManager.getNextUserForRefresh();
                if (user != null) {
                    collectDataForUser(user);
                    userRefreshManager.release(user);
                    user = null;
                }
            } catch (AuraException ex) {
                Logger.getLogger(FeedCrawler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
            } finally {
                if (user != null) {
                    userRefreshManager.release(user);
                }
            }
        }
    }

    private void collectDataForUser(User user) throws AuraException {
        long lastFeedPullTime = user.getLastFetchTime();
        long now = System.currentTimeMillis();

        Entry[] entries = getNewStarredEntriesFromUser(user, lastFeedPullTime);
        for (Entry entry : entries) {
            attend(user, entry, Attention.Type.STARRED);
        }
        user.setLastFetchTime(now);
        itemStore.put(user);
    }

    private Entry[] getNewStarredEntriesFromUser(User user, long lastFeedPullTime) throws AuraException {
        URL feed = user.getStarredItemFeedURL();
        Entry[] entries = getNewestEntries(feed, lastFeedPullTime);
        return entries;
    }

    private void attend(User user, Item item, Attention.Type type) throws AuraException {
        Attention attention = new SimpleAttention(user.getID(), item.getID(), type);
        if (isUniqueAttention(attention)) {
            itemStore.attend(attention);
        }
    }

    private Entry[] getNewestEntries(URL feedUrl, long lastPullTime) throws AuraException {
        try {
            SyndFeed feed = syndFeedInput.build(new XmlReader(feedUrl));
            List<Entry> entries = new ArrayList<Entry>();
            List entryList = feed.getEntries();
            for (Object o : entryList) {
                SyndEntry syndEntry = (SyndEntry) o;
                if (isFreshSyndEntry(syndEntry, lastPullTime)) {
                    String key = getKeyFromSyndEntry(syndEntry);
                    Entry entry = (Entry) itemStore.get(key);
                    if (entry == null) {
                        entry = createEntryFromSyndEntry(key, syndEntry);
                    }
                    entries.add(entry);
                } else {
                // TODO:
                    // QUESTION: Are entries always in reverse time order?
                    // if so we can 'break' here.
                }
            }
            return entries.toArray(EMPTY_ENTRY);
        } catch (IOException ex) {
            throw new AuraException("IOException while reading " + feedUrl, ex);
        } catch (FeedException ex) {
            throw new AuraException("FeedException while reading " + feedUrl, ex);
        }
    }

    private boolean isUniqueAttention(Attention attention) {
        Item item = itemStore.get(attention.getItemID());
        List<Attention> attentionList = item.getAttentionData();
        for (Attention att : attentionList) {
            if (att.getType() == attention.getType() && att.getUserID() == attention.getUserID()) {
                return false;
            }
        }
        return true;
    }

    private Entry createEntryFromSyndEntry(String key, SyndEntry syndEntry) throws AuraException {
        Entry entry = itemStore.newItem(Entry.class, key);
        // TODO, BUG: This is a placeholder, we need to figureout the best
        // way to get the content for an entry.
        entry.setField("content", syndEntry.getDescription().getValue());
        itemStore.put(entry);
        return entry;
    }

    private boolean isFreshSyndEntry(SyndEntry syndEntry, long lastRefreshTime) {
        return true;    // TODO write me
    }

    private String getKeyFromSyndEntry(SyndEntry syndEntry) {
        String key = syndEntry.getLink();
        if (key == null) {
            key = syndEntry.getTitle();
        }

        if (key == null) {
            // TODO: we want to guarantee that the key is never null
            // but what is the best way to do that?  Using the hashcode
            // is particularly unsatisfying.
            key = Integer.toString(syndEntry.hashCode());
        }
        return key;
    }
}