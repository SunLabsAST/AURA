/*
 * Project Aura,
 * 
 * Copyright (c) 2008,  Sun Microsystems Inc
 * See license.txt for licensing info.
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
