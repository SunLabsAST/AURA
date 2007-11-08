/*
 *  Copyright Expression year is undefined on line 4, column 30 in Templates/Licenses/license-default.txt. Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES. 
 */
package com.sun.labs.aura.aardvark;

import com.sun.labs.aura.aardvark.store.item.User;
import com.sun.labs.aura.aardvark.util.AuraException;
import com.sun.labs.util.props.ConfigurationManager;
import com.sun.syndication.feed.synd.SyndFeed;
import java.io.File;
import java.io.IOException;
import java.net.URL;
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

    public AardvarkSearchTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getDefault method, of class Aardvark.
     */
    @Test
    public void getDefault() throws Exception {
        Aardvark aardvark = getFreshAardvark();
        assertNotNull("getting default aardvark", aardvark);
        aardvark.shutdown();
    }

    /**
     * Test of getRecommendedFeed method, of class Aardvark.
     */
    @Test
    public void getRecommendedFeed() throws Exception {
        Aardvark aardvark = getFreshAardvark();

        try {
            aardvark.startup();
            URL feedURL1 = this.getClass().getResource("gr_pbl_starred.atom.xml");
            User user1 = aardvark.enrollUser("openid.sun.com/plamere", feedURL1.toString());
            assertNotNull("enrolled user can't be null", user1);
            assertTrue("last fetch time should be never", user1.getLastFetchTime() == 0L);
            Thread.sleep(5000L);

            SyndFeed feed = aardvark.getRecommendedFeed(user1);
            int entryCount = feed.getEntries().size();
            assertTrue("The random document should have been returned: " + entryCount, entryCount > 0);

            Thread.sleep(5000L);
            user1 = aardvark.getUser("openid.sun.com/plamere");
            long delta = System.currentTimeMillis() - user1.getLastFetchTime();
            assertTrue("user should be refreshed again", delta < 2000L);

            feed = aardvark.getRecommendedFeed(user1);
            entryCount = feed.getEntries().size();
            assertTrue("The random document should still have been returned: " + entryCount, entryCount > 0);

        } finally {
            aardvark.shutdown();
        }
    }

    @Test
    public void testMultiUserRecommendedFeed() throws Exception {
        Aardvark aardvark = getFreshAardvark();

        try {
            aardvark.startup();
            URL feedURL1 = this.getClass().getResource("gr_pbl_starred.atom.xml");
            User user1 = aardvark.enrollUser("openid.sun.com/plamere", feedURL1.toString());
            URL feedURL2 = this.getClass().getResource("gr_pbl_favorite.atom.xml");
            User user2 = aardvark.enrollUser("openid.sun.com/stgreen", feedURL2.toString());
            Thread.sleep(5000L);

            user1 = aardvark.getUser("openid.sun.com/plamere");
            long delta = System.currentTimeMillis() - user1.getLastFetchTime();
            assertTrue("user should be refreshed, delta time is " + delta, delta < 2000L);

            SyndFeed feed = aardvark.getRecommendedFeed(user1);
            int entryCount = feed.getEntries().size();
            assertTrue("The random document should have been returned: " + entryCount, entryCount > 0);

            user2 = aardvark.getUser("openid.sun.com/stgreen");
            delta = System.currentTimeMillis() - user2.getLastFetchTime();
            assertTrue("user should be refreshed, delta time is " + delta, delta < 2000L);

            feed = aardvark.getRecommendedFeed(user2);
            entryCount = feed.getEntries().size();
            assertTrue("The random document should have been returned: " + entryCount, entryCount > 0);
        } finally {
            aardvark.shutdown();
        }
    }

    @Test
    public void testFeedTortureTest() throws Exception {
        Aardvark aardvark = getFreshAardvark();

        try {
            aardvark.startup();

            assertTrue("empty aardvark users", aardvark.getStats().getNumUsers() == 0);
            assertTrue("empty aardvark items", aardvark.getStats().getNumItems() == 0);

            enroll(aardvark, "blogs.sun.com.rss");
            enroll(aardvark, "delicious.rss");
            enroll(aardvark, "digg.rss");
            enroll(aardvark, "googlenews.rss");
            enroll(aardvark, "reddit.rss");
            enroll(aardvark, "slashdot.rss");
            enroll(aardvark, "empty.rss");
            enroll(aardvark, "garbage.rss");

            assertTrue("full aardvark users", aardvark.getStats().getNumUsers() == 8);
            Thread.sleep(60000L);
            assertTrue("full aardvark items " + aardvark.getStats().getNumItems(), 
                    aardvark.getStats().getNumItems() == 142);

        } finally {
            aardvark.shutdown();
        }
    }
    
    private void enroll(Aardvark aardvark, String feedBaseName) throws AuraException {
        URL feedURL = this.getClass().getResource(feedBaseName);
        aardvark.enrollUser(feedBaseName, feedURL.toString());
    }

    private void deleteDirectory(File indexDir) {
        File[] fs = indexDir.listFiles();
        for(File f : fs) {
            if(f.isDirectory()) {
                deleteDirectory(f);
            } else {
                assertTrue(f.delete());
            }
        }
        assertTrue(indexDir.delete());
    }

    private Aardvark getFreshAardvark() throws IOException {
        ConfigurationManager cm = new ConfigurationManager();
        URL configFile = this.getClass().getResource("aardvarkSearchTestConfig.xml");
        cm.addProperties(configFile);
        File indexDir = new File(cm.getGlobalProperty("indexDir"));
        if(indexDir.exists()) {
            if(indexDir.isDirectory()) {
                deleteDirectory(indexDir);
            } else {
                assertTrue(indexDir.delete());
            }
        }

        return (Aardvark) cm.lookup("aardvark");
    }

}