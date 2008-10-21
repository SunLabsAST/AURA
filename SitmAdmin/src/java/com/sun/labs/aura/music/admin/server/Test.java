/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.admin.server;

import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.admin.client.TestStatus;
import com.sun.labs.aura.util.AuraException;
import java.rmi.RemoteException;

/**
 *
 * @author plamere
 */
public abstract class Test extends MDBHelper {

    private String name;

    Test(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    abstract protected void go(MusicDatabase mdb, TestStatus ts) throws AuraException, RemoteException;

    public TestStatus runTest(MusicDatabase mdb) {
        TestStatus ts = new TestStatus();
        long start = System.currentTimeMillis();
        try {
            go(mdb, ts);
        } catch (AuraException ex) {
            ts.fail("AuraException " + ex.getMessage());
        } catch (RemoteException ex) {
            ts.fail("RemoteException " + ex.getMessage());
        }
        long delta = System.currentTimeMillis() - start;
        ts.setTime(delta);
        return ts;
    }

}
