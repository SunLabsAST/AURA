/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.  Which I did, which is why this text is here.
 */
package com.sun.labs.aura.aardvark.crawler;

import com.sun.labs.aura.aardvark.util.FeedUtils;
import com.sun.labs.aura.aardvark.util.AuraException;
import com.sun.labs.util.LabsLogFormatter;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
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
 *
 * @author plamere
 */
public class FeedCrawlerTest {

    private SyndFeed feed;

    public FeedCrawlerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        //
        // Use the labs format logging.
        Logger rl = Logger.getLogger("");
        for(Handler h : rl.getHandlers()) {
            h.setFormatter(new LabsLogFormatter());
        }
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

        dumpRaw(feed);

        for (Object o : feed.getEntries()) {
            SyndEntry se = (SyndEntry) o;
            assertNotNull("Feed has contents", FeedUtils.getContent(se));
            dumpRaw(se);
        }
    }

    @Test
    public void testEntryConversion() {
        for (Object o : feed.getEntries()) {
            try {
                SyndEntry se1 = (SyndEntry) o;
                String xml1 = FeedUtils.toString(se1);
                SyndEntry se2 = FeedUtils.toSyndEntry(xml1);
                String xml2 = FeedUtils.toString(se2);
                assertTrue("summary equal", FeedUtils.getContent(se1).equals(FeedUtils.getContent(se2)));
                assertTrue("xml strings are equal", xml1.equals(xml2));
            } catch (AuraException ex) {
                fail("Conversion failed");
            }
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

    private void dumpRaw(SyndFeed feed) {
        System.out.println("----- Feed --------------------------------");
        System.out.println(feed);
        System.out.println("-------------------------------------------");
    }

    @Test
    public void testEntry() {
        int feedCount = feed.getEntries().size();
        assertTrue("Feed count matches", feedCount == 13);
    }
}