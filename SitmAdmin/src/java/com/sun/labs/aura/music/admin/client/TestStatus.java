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

package com.sun.labs.aura.music.admin.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 *
 * @author plamere
 */
public class TestStatus implements IsSerializable {
    private boolean passed = true;
    private long time;
    private String failReason = "";
    private String stackTrace = null;
    private String mostRecentQuery;

    public TestStatus() {
    }

    public TestStatus(boolean passed, long time, String failReason) {
        this.passed = passed;
        this.time = time;
        this.failReason = failReason;
    }

    public TestStatus(long time, String failReason) {
        this(false, time, failReason);
    }

    public TestStatus(long time) {
        this(true, time, "");
    }

    public String getFailReason() {
        return failReason;
    }

    public boolean isPassed() {
        return passed;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    

    public void fail(String failReason) {
        this.passed = false;
        this.failReason = failReason;
    }

    public String getMostRecentQuery() {
        return mostRecentQuery;
    }

    public void setMostRecentQuery(String mostRecentQuery) {
        this.mostRecentQuery = mostRecentQuery;
    }

    
}
