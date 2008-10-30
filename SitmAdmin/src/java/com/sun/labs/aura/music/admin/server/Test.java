/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.admin.server;

import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.admin.client.TestStatus;
import com.sun.labs.aura.util.AuraException;
import java.io.PrintWriter;
import java.io.StringWriter;
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
        } catch (Exception ex) {
            captureStackTrace(ts, ex);
            ts.fail(getExceptionExplanation(ex));
        }
        long delta = System.currentTimeMillis() - start;
        ts.setTime(delta);
        return ts;
    }

    private String getExceptionExplanation(Throwable t) {
        String exceptionName = t.getClass().getSimpleName();
        String msg = t.getMessage();
        if (t.getCause() != null) {
            msg = msg + " cause: " + getExceptionExplanation(t.getCause());
        }


        return exceptionName + ":" + msg;
    }

    private void captureStackTrace(TestStatus ts, Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.close();
        String trace = sw.toString();
        if (trace.length() > 0) {
            ts.setStackTrace(trace);
        }
    }

}
