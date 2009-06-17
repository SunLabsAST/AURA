/*
 * Copyright 2007-2009 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.labs.aura.music.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author mailletf
 */
public class ExpiringLRUCache {

    private LRUCache<String, Object> cache;
    private long time2live; // time objects can live in cache in secs

    public ExpiringLRUCache(int maxSize, int secs2live) {
        cache = new LRUCache(maxSize);
        this.time2live = secs2live * 1000;
    }

    public synchronized int getSize() {
        return cache.size();
    }

    public synchronized Object sget(String s) {
        ObjectContainer o = (ObjectContainer) cache.sget(s);
        if (o == null || o.getExpiration() < System.currentTimeMillis()) {
            return null;
        } else {
            return o.getObject();
        }
    }

    public synchronized Object sput(String s, Object o) {
        return cache.sput(s, new ObjectContainer(System.currentTimeMillis() + time2live, o));
    }

    private class LRUCache<String, Object> extends LinkedHashMap<String, Object> {

        private int maxSize;

        LRUCache(int maxSize) {
            this.maxSize = maxSize;
        }

        // BUG sort out this sync stuff
        synchronized public Object sget(String s) {
            return get(s);
        }

        synchronized public Object sput(String s, Object o) {
            return put(s, o);
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            boolean remove = size() > maxSize;
            return remove;
        }
    }

    private class ObjectContainer {

        private Object obj;
        private long expiration;

        public ObjectContainer(long creationTime, Object obj) {
            this.expiration = creationTime;
            this.obj = obj;
        }

        public Object getObject() {
            return obj;
        }

        public long getExpiration() {
            return expiration;
        }
    }
}
