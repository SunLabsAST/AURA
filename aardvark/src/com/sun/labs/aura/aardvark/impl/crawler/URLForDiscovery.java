/*
 * Project Aura,
 * 
 * Copyright (c) 2008,  Sun Microsystems Inc
 * See license.txt for licensing info.
 */
package com.sun.labs.aura.aardvark.impl.crawler;

import com.sun.labs.aura.datastore.Attention;
import java.io.Serializable;

/**
 *
 * @author plamere
 */
public class URLForDiscovery implements Serializable, Comparable<URLForDiscovery> {

    public final static float LOW_PRIORITY = 0F;
    public final static float HIGH_PRIORITY = 1000F;
    public final static float ULTRA_HIGH_PRIORITY = 10000F;
    public final static float DEFAULT_PRIORITY = LOW_PRIORITY;
    private String surl = null;
    private float priority = DEFAULT_PRIORITY;
    private Attention attention = null;

    public URLForDiscovery(String url, float priority, Attention attention) {
        this.surl = url;
        this.priority = priority;
        this.attention = attention;
    }

    public URLForDiscovery(String url, float priority) {
        this(url, priority, null);
    }

    public URLForDiscovery(String url) {
        this(url, DEFAULT_PRIORITY);
    }

    public URLForDiscovery() {
    }

    public Attention getAttention() {
        return attention;
    }

    public String getUrl() {
        return surl;
    }

    public float getPriority() {
        return priority;
    }

    /**
     * The sense of this sort is reversed so that high priority items
     * come first in the queue
     * @param o
     * @return
     */
    public int compareTo(URLForDiscovery o) {
        return (int) -Math.signum(getPriority() - o.getPriority());
    }

    public String toString() {
        return "ufd " + surl + " pri " + priority + " " + attention;
    }
}
