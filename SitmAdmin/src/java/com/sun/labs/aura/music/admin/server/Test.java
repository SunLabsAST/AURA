/*
 * Copyright 2008-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
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
