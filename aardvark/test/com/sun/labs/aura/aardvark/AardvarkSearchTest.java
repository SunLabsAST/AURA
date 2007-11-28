/*
 *  Copyright Expression year is undefined on line 4, column 30 in Templates/Licenses/license-default.txt. Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES. 
 */
package com.sun.labs.aura.aardvark;

import com.sun.labs.aura.aardvark.crawler.FeedCrawler;
import com.sun.labs.aura.aardvark.store.item.User;
import com.sun.labs.aura.aardvark.util.AuraException;
import com.sun.labs.util.LabsLogFormatter;
import com.sun.labs.util.props.ConfigurationManager;
import com.sun.syndication.feed.synd.SyndFeed;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Handler;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for search engine based content recommendation.
 */
public class AardvarkSearchTest {

    private Aardvark aardvark = null;
    private FeedCrawler crawler = null;

    public AardvarkSearchTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        //
        // Use the labs format logging.
        Logger rl = Logger.getLogger("");
        for (Handler h : rl.getHandlers()) {
            h.setFormatter(new LabsLogFormatter());
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        prepareFreshAardvark();
    }

    @After
    public void tearDown() throws Exception {
        if (aardvark != null) {
            aardvark.shutdown();
            aardvark = null;
        }
    }

    /**
     * Test of getDefault method, of class Aardvark.
     */
    @Test
    public void getDefault() throws Exception {
        assertNotNull("getting default aardvark", aardvark);
        assertNotNull("getting default crawler", crawler);
    }

    /**
     * Test of getRecommendedFeed method, of class Aardvark.
     */
    @Test
    public void getRecommendedFeed() throws Exception {

        URL feedURL1 = this.getClass().getResource("gr_pbl_starred.atom.xml");
        User user1 = aardvark.enrollUser("openid.sun.com/plamere", feedURL1.toString());

        URL feedURL2 = this.getClass().getResource("reddit.rss");
        User user2 = aardvark.enrollUser("reddit", feedURL2.toString());

        assertNotNull("enrolled user can't be null", user1);
        assertNotNull("enrolled user can't be null", user2);

        crawler.crawlAllFeeds();

        user1 = aardvark.getUser(user1.getKey());
        assertTrue("user1 should have attention data", user1.getAttentionData().size() > 0);
        SyndFeed feed = aardvark.getRecommendedFeed(user1);
        int entryCount = feed.getEntries().size();
        assertTrue("The random document should have been returned: " + entryCount, entryCount > 0);

        crawler.crawlAllFeeds();

        feed = aardvark.getRecommendedFeed(user1);
        entryCount = feed.getEntries().size();
        assertTrue("The random document should still have been returned: " + entryCount, entryCount > 0);

        // we should be able to get recommendations until there's nothing left to recommend:

        int count = 0;
        while (aardvark.getRecommendedFeed(user1).getEntries().size() > 0) {
            if (count++ > 10) {
                fail("too many recommendations");
                break;
            }
        }

        assertTrue("there should be zero recommendations left",
                aardvark.getRecommendedFeed(user1).getEntries().size() == 0);

    }

    @Test
    public void testMultiUserRecommendedFeed() throws Exception {

        URL feedURL1 = this.getClass().getResource("gr_pbl_starred.atom.xml");
        User user1 = aardvark.enrollUser("openid.sun.com/plamere", feedURL1.toString());

        URL feedURL2 = this.getClass().getResource("reddit.rss");
        User user2 = aardvark.enrollUser("reddit", feedURL2.toString());

        crawler.crawlAllFeeds();

        SyndFeed feed = aardvark.getRecommendedFeed(user1);
        int entryCount = feed.getEntries().size();
        assertTrue("The random document should have been returned: " + entryCount, entryCount > 0);

        feed = aardvark.getRecommendedFeed(user2);
        entryCount = feed.getEntries().size();
        assertTrue("The random document should have been returned: " + entryCount, entryCount > 0);
    }

    @Test
    public void testFeedTortureTest() throws Exception {


        enroll(aardvark, "blogs.sun.com.rss");
        enroll(aardvark, "delicious.rss");
        enroll(aardvark, "digg.rss");

        try {
            enroll(aardvark, "empty.rss");
            //fail("need exception for empty feed empty.rss");
        } catch (AuraException ex) {
            assertTrue("exception for empty feed", true);
        }

        try {
            enroll(aardvark, "garbage.rss");
            //fail("need exception for garbage feed garbage.rss");
        } catch (AuraException ex) {
            assertTrue("exeption for garbage feed", true);
        }

        enroll(aardvark, "googlenews.rss");
        enroll(aardvark, "reddit.rss");
        enroll(aardvark, "slashdot.rss");

        crawler.crawlAllFeeds();

        assertTrue("full aardvark users", aardvark.getStats().getNumUsers() == 8);
        assertTrue("full aardvark items " + aardvark.getStats().getNumItems(),
                aardvark.getStats().getNumItems() == 142);

    }

    private void enroll(Aardvark aardvark, String feedBaseName) throws AuraException {
        URL feedURL = this.getClass().getResource(feedBaseName);
        aardvark.enrollUser(feedBaseName, feedURL.toString());
    }

    private void deleteDirectory(File indexDir) {
        File[] fs = indexDir.listFiles();
        for (File f : fs) {
            if (f.isDirectory()) {
                deleteDirectory(f);
            } else {
                assertTrue(f.delete());
            }
        }
        assertTrue(indexDir.delete());
    }


    private void prepareFreshAardvark() {
        try {
            ConfigurationManager cm = new ConfigurationManager();
            URL configFile = this.getClass().getResource("aardvarkSearchTestConfig.xml");
            cm.addProperties(configFile);
            File indexDir = new File(cm.getGlobalProperty("indexDir"));
            if (indexDir.exists()) {
                if (indexDir.isDirectory()) {
                    deleteDirectory(indexDir);
                } else {
                    assertTrue("can't delete " + indexDir, indexDir.delete());
                }
            }
            aardvark = (Aardvark) cm.lookup("aardvark");
            aardvark.startup();
            crawler = (FeedCrawler) cm.lookup("feedCrawler");
        } catch (IOException ioe) {
        }
    }
}
