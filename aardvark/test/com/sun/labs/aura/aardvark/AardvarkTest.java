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
import com.sun.labs.util.LabsLogFormatter;
import com.sun.labs.util.props.ConfigurationManager;
import com.sun.syndication.feed.synd.SyndFeed;
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
public class AardvarkTest {

    Aardvark aardvark = null;

    public AardvarkTest() {
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
        aardvark = getFreshAardvark();
    }

    @After
    public void tearDown() {
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
    }

    /**
     * Test of getUser method, of class Aardvark.
     */
    @Test
    public void getMissingUser() throws Exception {
        assertNotNull("getting default aardvark", aardvark);
        assertNull("missing user must be null", aardvark.getUser("missingUser"));
    }

    /**
     * Test of enrollUser method, of class Aardvark.
     */
    @Test
    public void enrollUser() throws Exception {
        assertNotNull("getting default aardvark", aardvark);
        assertNull("missing user must be null", aardvark.getUser("missingUser"));
        URL feedURL1 = this.getClass().getResource("gr_pbl_starred.atom.xml");
        URL feedURL2 = this.getClass().getResource("gr_pbl_favorite.atom.xml");
        User user1 = aardvark.enrollUser("openid.sun.com/plamere", feedURL1.toString());
        assertNotNull("enrolled user can't be null", user1);

        User user2 = aardvark.getUser("openid.sun.com/plamere");
        assertTrue("users should match", user1 == user2);

        User user3 = aardvark.enrollUser("openid.sun.com/stgreen", feedURL2.toString());
        assertNotNull("enrolled user can't be null", user3);

        assertTrue("different users should not match", user1 == user2);

        try {
            User user4 = aardvark.enrollUser("openid.sun.com/plamere", feedURL1.toString());
            fail("can't enroll duplicate user");
        } catch (AuraException e) {
            assertTrue("can't enroll duplicate user", true);
        }
    }

    /**
     * Test of getRecommendedFeed method, of class Aardvark.
     */
    @Test
    public void getRecommendedFeed() throws Exception {
        assertNotNull("getting default aardvark", aardvark);
        URL feedURL1 = this.getClass().getResource("gr_pbl_starred.atom.xml");
        User user1 = aardvark.enrollUser("openid.sun.com/plamere", feedURL1.toString());
        assertNotNull("enrolled user can't be null", user1);
        user1 = aardvark.getUser("openid.sun.com/plamere");

        Thread.sleep(5000L);
        SyndFeed feed = aardvark.getRecommendedFeed(user1);
        int entryCount = feed.getEntries().size();
        assertTrue("proper recommendation feed count count: " + entryCount, entryCount == 13);

        user1 = aardvark.getUser("openid.sun.com/plamere");
        feed = aardvark.getRecommendedFeed(user1);
        entryCount = feed.getEntries().size();
        assertTrue("Still proper recommendation feed count: " + entryCount, entryCount == 13);
    }

    @Test public void testMultiUserRecommendedFeed() throws Exception {
        assertNotNull("getting default aardvark", aardvark);
        URL feedURL1 = this.getClass().getResource("gr_pbl_starred.atom.xml");
        User user1 = aardvark.enrollUser("openid.sun.com/plamere", feedURL1.toString());
        URL feedURL2 = this.getClass().getResource("gr_pbl_favorite.atom.xml");
        User user2 = aardvark.enrollUser("openid.sun.com/stgreen", feedURL2.toString());
        Thread.sleep(1000L);

        user1 = aardvark.getUser("openid.sun.com/plamere");

        SyndFeed feed = aardvark.getRecommendedFeed(user1);
        int entryCount = feed.getEntries().size();
        assertTrue("proper recommendation feed count count: " + entryCount, entryCount == 13);

        user2 = aardvark.getUser("openid.sun.com/stgreen");

        feed = aardvark.getRecommendedFeed(user2);
        entryCount = feed.getEntries().size();
        assertTrue("proper recommendation feed count count: " + entryCount, entryCount == 4);
    }

    private Aardvark getFreshAardvark() {
        try {
            ConfigurationManager cm = new ConfigurationManager();
            URL configFile = this.getClass().getResource("aardvarkTestConfig.xml");
            cm.addProperties(configFile);
            Aardvark a = (Aardvark) cm.lookup("aardvark");
            a.startup();
            return a;
        } catch (IOException ioe) {
            return null;
        }
    }
}