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

    public void fail(String failReason) {
        this.passed = false;
        this.failReason = failReason;
    }
}
