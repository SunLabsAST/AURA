/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.crawler;

import com.sun.labs.util.LabsLogFormatter;
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
public class UserRefreshManagerTest {

    public UserRefreshManagerTest() {
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
    }

    @After
    public void tearDown() {
    }

    @Test public void testJunit() {
        assertTrue("Testing Junit, this should pass", true);
    }

    @Test public void ifAtFirstYouDontSucceedYouFail() {
        //assertTrue("Testing Junit, this should fail", false);
    }
}
