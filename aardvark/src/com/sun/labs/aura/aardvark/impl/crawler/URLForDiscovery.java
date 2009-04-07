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

package com.sun.labs.aura.aardvark.impl.crawler;

import com.sun.labs.aura.datastore.Attention;
import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author plamere
 */
public class URLForDiscovery implements Serializable, Delayed {

    public static enum Priority { NORMAL, HIGH }; 
    private String surl = null;
    private Priority priority = Priority.NORMAL;
    private Attention attention = null;
    private long nextProcessingTime = 0;

    public URLForDiscovery(String url, Priority priority, Attention attention) {
        this.surl = url;
        this.priority = priority;
        this.attention = attention;
    }


    public URLForDiscovery(String url, Priority priority) {
        this(url, priority, null);
    }

    public URLForDiscovery(String url) {
        this(url, Priority.NORMAL);
    }

    public URLForDiscovery() {
    }

    public Attention getAttention() {
        return attention;
    }

    public String getUrl() {
        return surl;
    }

    public Priority getPriority() {
        return priority;
    }


    public long getDelay(TimeUnit unit) {
        return unit.convert(nextProcessingTime - System.currentTimeMillis(),
                TimeUnit.MILLISECONDS);
    }

    public String toString() {
        return "ufd " + new Date(nextProcessingTime) + " " + surl;
    }

    long getNextProcessingTime() {
        return nextProcessingTime;
    }

    void setNextProcessingTime(long nextProcessingTime) {
        this.nextProcessingTime = nextProcessingTime;
    }

    public int compareTo(Delayed o) {
        long result = getDelay(TimeUnit.MILLISECONDS) -
                o.getDelay(TimeUnit.MILLISECONDS);
        return result < 0 ? -1 : result > 0 ? 1 : 0;
    }
}
