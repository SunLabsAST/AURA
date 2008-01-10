/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.aardvark.crawler;

import com.sun.labs.aura.aardvark.util.OPMLProcessor;
import com.sun.labs.aura.aardvark.Aardvark;
import com.sun.labs.aura.aardvark.util.AuraException;
import com.sun.labs.util.LabsLogFormatter;
import com.sun.labs.util.props.ConfigurationManager;
import java.io.IOException;
import java.net.URL;
import java.util.List;
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
public class SimpleFeedCrawlerTest {

    private FeedCrawler crawler;

    public SimpleFeedCrawlerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
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
        prepareFreshCrawler();
    }

    @After
    public void tearDown() {
        if (crawler != null) {
            crawler.stop();
            crawler = null;
        }
    }

    /**
     * Test of createFeed method, of class SimpleFeedCrawler.
     */
    @Test
    public void megaTest() throws AuraException {
//        addLocalOpml("autoEnrolledFeeds.opml.xml");
//        assertTrue("top 100", crawler.getNumFeeds() == 100);
//
//       // crawler.crawlAllFeeds();
//
//        addLocalOpml("tech_blogs.opml");
//        assertTrue("tech blogs has " + crawler.getNumFeeds(), crawler.getNumFeeds() == 867);
//
//        addLocalOpml("politics_blogs.opml");
//        assertTrue("tech blogs has " + crawler.getNumFeeds(), crawler.getNumFeeds() == 1264);
//
//
//        addLocalOpml("news_blogs.opml");
//        assertTrue("tech blogs has " + crawler.getNumFeeds(), crawler.getNumFeeds() == 8337);

    }

    private void addLocalOpml(String name) {
        try {
            System.out.println("Enrolling local opml " + name);
            OPMLProcessor op = new OPMLProcessor();
            URL opmlFile = Aardvark.class.getResource(name);
            List<URL> urls = op.getFeedURLs(opmlFile);
            for (URL url : urls) {
                crawler.createFeed(url);
            }
        } catch (IOException ex) {
            fail("Problems loading opml " + name + " " + ex);
        }
    }
    
    private void prepareFreshCrawler() {
        try {
            ConfigurationManager cm = new ConfigurationManager();
            URL configFile = this.getClass().getResource("crawlerTestConfig.xml");
            cm.addProperties(configFile);
            crawler = (FeedCrawler) cm.lookup("feedCrawler");
            crawler.start();
        } catch (IOException ioe) {
        }
    }
    
}
