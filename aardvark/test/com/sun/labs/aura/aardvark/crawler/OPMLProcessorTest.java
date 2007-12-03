/*
 *  Copyright 2007 Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES. 
 */

package com.sun.labs.aura.aardvark.crawler;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author plamere
 */
public class OPMLProcessorTest {

    public OPMLProcessorTest() {
    }

    @Test public void testTop100() throws Exception {
        OPMLProcessor op = new OPMLProcessor();
        URL opmlFile = this.getClass().getResource("top100.opml.xml");
        List<URL> urls = op.getFeedURLs(opmlFile);
        
        assertNotNull("should be url list", urls);
        assertTrue("should be 100", urls.size() == 100);
        assertTrue("first should be techcrunch", urls.get(0).toExternalForm().equals("http://feeds.feedburner.com/Techcrunch"));
        assertTrue("last  should be cnn", urls.get(99).toExternalForm().equals("http://rss.cnn.com/rss/cnn_topstories.rss"));
    }

    
    @Test public void testLargeOpml() throws Exception {
        OPMLProcessor op = new OPMLProcessor();
        URL opmlFile = this.getClass().getResource("news_blogs.opml.xml");
        List<URL> urls = op.getFeedURLs(opmlFile);
        assertNotNull("should be url list", urls);
        assertTrue("should be 31373, was" + urls.size(), urls.size() == 31373);
    }


    @Test public void testRemoteOPML() throws Exception {
        OPMLProcessor op = new OPMLProcessor();
        URL opmlFile = new URL("http://share.opml.org/opml/top100.opml");
        List<URL> urls = op.getFeedURLs(opmlFile);
        assertNotNull("should be url list", urls);
        assertTrue("should be 100", urls.size() == 100);

        // should contain techcruch
        for (URL url  : urls) {
            if (url.toExternalForm().equals("http://feeds.feedburner.com/Techcrunch")) {
                return;
            }
        }
        fail("no techcrunch in top 100");
    }

    @Test public void testGoogleReaderOpmlProcessing() throws Exception {
        OPMLProcessor op = new OPMLProcessor();
        URL opmlFile = this.getClass().getResource("google-reader-subscriptions.xml");
        List<URL> urls = op.getFeedURLs(opmlFile);
        
        assertNotNull("should be url list", urls);
        assertTrue("should be more than 0", urls.size() > 0);
        int last = urls.size() - 1;
        assertTrue("last  should be cnn", urls.get(last).toExternalForm().equals("http://feeds.feedburner.com/YoutubeApiBlog"));
    }

   @Test(expected=IOException.class)
    public void testMissingFile() throws Exception {
        OPMLProcessor op = new OPMLProcessor();
        URL opmlFile = new URL("missing.opml.xml");
        op.getFeedURLs(opmlFile);
    }

   @Test(expected=IOException.class)
    public void testCorruptFile() throws Exception {
        OPMLProcessor op = new OPMLProcessor();
        URL opmlFile = this.getClass().getResource("top100.opml.corrupt.xml");
        op.getFeedURLs(opmlFile);
    }
}