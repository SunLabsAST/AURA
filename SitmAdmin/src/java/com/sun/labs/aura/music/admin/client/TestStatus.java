/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
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
