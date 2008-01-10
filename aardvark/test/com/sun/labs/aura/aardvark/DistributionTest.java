/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark;

import com.sun.labs.aura.aardvark.crawler.FeedCrawler;
import com.sun.labs.aura.aardvark.store.ItemStore;
import com.sun.labs.aura.aardvark.store.item.Feed;
import com.sun.labs.util.SimpleLabsLogFormatter;
import com.sun.labs.util.props.ConfigurationManager;
import java.net.URL;
import java.util.logging.Handler;
import java.util.logging.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author stgreen
 */
public class DistributionTest {

    public DistributionTest() {
    }
    
    private static Logger log;

    @BeforeClass
    public static void setUpClass() throws Exception {
        Logger l = Logger.getLogger("");
        for(Handler h : l.getHandlers()) {
            h.setFormatter(new SimpleLabsLogFormatter());
        }
        log = Logger.getLogger("com.sun.labs.aura.aardvark");
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

//    @Test
    public void registerItemStore() throws Exception {
        URL cu = getClass().getResource("distItemStoreConfig.xml");
        ConfigurationManager cm = new ConfigurationManager(cu);
        ItemStore is = (ItemStore) cm.lookup("itemStore");
        assertNotNull(is);
        cm.getComponentRegistry().unregister();
    }
    
    @Test
    public void crawlOneFeed() throws Exception {
        URL cu = getClass().getResource("distItemStoreConfig.xml");
        ConfigurationManager iscm = new ConfigurationManager(cu);
        ItemStore is = (ItemStore) iscm.lookup("itemStore");
        assertNotNull(is);
        cu = getClass().getResource("distCrawlerConfig.xml");
        ConfigurationManager crcm = new ConfigurationManager(cu);
        log.info("crcm registry: " + crcm.getComponentRegistry());
        FeedCrawler crawler = (FeedCrawler) crcm.lookup("feedCrawler");
        URL fu = getClass().getResource("blogs.sun.com.rss");
        Feed feed = crawler.createFeed(cu);
        assertNotNull(feed);
        crawler.crawlFeed(feed);
        log.info("stats: " + is.getStats());
        Thread.sleep(5000);
        iscm.getComponentRegistry().unregister();
    }
}