/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.  Which I did, which is why this text is here.
 */
package com.sun.labs.aura.aardvark.crawler;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import java.io.IOException;
import java.net.URL;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author plamere
 */
public class FeedCrawlerTest {

    private SyndFeed feed;

    public FeedCrawlerTest() {
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
            URL feedURL = this.getClass().getResource("gr_pbl_starred.atom.xml");
            SyndFeedInput syndFeedInput = new SyndFeedInput();
            feed = syndFeedInput.build(new XmlReader(feedURL));
        } catch (IllegalArgumentException ex) {
            fail("Can't load feed");
        } catch (FeedException ex) {
            fail("Can't load feed");
        } catch (IOException ex) {
            fail("Can't load feed");
        }
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testFeedOK() {
        assertTrue("Feed loaded", feed != null);
    }

    @Test
    public void testEntryCount() {
        int feedCount = feed.getEntries().size();
        assertTrue("Feed count matches", feedCount == 13);

        for (Object o : feed.getEntries()) {
            SyndEntry se = (SyndEntry) o;
            assertNotNull("Feed has contents", FeedUtils.getContent(se));
        }
    }

    @Test
    public void testEntryContents() {
        for (Object o : feed.getEntries()) {
            SyndEntry se = (SyndEntry) o;
            assertNotNull("Feed has contents", FeedUtils.getContent(se));
        }
    }

    @Test
    public void testEntryKeys() {
        for (Object o : feed.getEntries()) {
            SyndEntry se = (SyndEntry) o;
            assertNotNull("Feed has contents", FeedUtils.getKey(se));
        }
    }

    @Test
    public void testExpired() {
        long now = System.currentTimeMillis();
        for (Object o : feed.getEntries()) {
            SyndEntry se = (SyndEntry) o;
            assertFalse("Feed should be expired", FeedUtils.isFresh(se, now));
        }
    }

    @Test
    public void testFeedsAreOrderedByTime() {
        // Feeds are not ordered by time, so this test in not valid
        long lastTime = System.currentTimeMillis();
        for (Object o : feed.getEntries()) {
            SyndEntry se = (SyndEntry) o;
            long publishedTime = se.getPublishedDate().getTime();
            //System.out.printf("%d, %d\n", lastTime, publishedTime);
            //assertTrue("ordered feed time", lastTime >= publishedTime);
            lastTime = publishedTime;
        }
    }

    @Test
    public void testFresh() {
        long now = System.currentTimeMillis() - 5 * 365 * 24 * 60 * 60 * 1000L;
        for (Object o : feed.getEntries()) {
            SyndEntry se = (SyndEntry) o;
            assertTrue("Feed should be fresh", FeedUtils.isFresh(se, now));
        }
    }

    private void dump(SyndEntry se) {
        System.out.println("-----------------");
        System.out.println("  title:" + se.getTitle());
        System.out.println(" Author:" + se.getAuthor());
        System.out.println(FeedUtils.getContent(se));
    }

    private void dumpRaw(SyndEntry se) {
        System.out.println("-----------------");
        System.out.println(se);
    }

    @Test
    public void testEntry() {
        int feedCount = feed.getEntries().size();
        assertTrue("Feed count matches", feedCount == 13);
    }
}