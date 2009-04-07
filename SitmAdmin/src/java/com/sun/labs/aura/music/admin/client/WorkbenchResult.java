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
