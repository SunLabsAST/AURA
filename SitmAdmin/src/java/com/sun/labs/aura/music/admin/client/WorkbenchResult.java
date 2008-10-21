/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.admin.client;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author plamere
 */
public class WorkbenchResult implements IsSerializable {

    private boolean ok = true;
    private long time;
    private String failReason = "";
    private List<String> output;

    public WorkbenchResult() {
        output = new ArrayList<String>();
    }

    public WorkbenchResult(boolean ok, String failReason) {
        this();
        fail(failReason);
    }

    public void output(String s) {
        output.add(s);
    }

    public WorkbenchResult(long time, List<String> output) {
        this();
        this.time = time;
        this.output = output;
    }

    public String getFailReason() {
        return failReason;
    }

    public List<String> getOutput() {
        return output;
    }

    public boolean isOk() {
        return ok;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void fail(String failReason) {
        this.ok = false;
        this.failReason = failReason;
    }
}
