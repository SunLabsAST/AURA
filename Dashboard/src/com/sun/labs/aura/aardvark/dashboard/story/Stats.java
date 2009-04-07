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

package com.sun.labs.aura.aardvark.dashboard.story;

/**
 *
 * 
 */
public class Stats {
    private long entries;
    private long feeds;
    private long pulls;
    private long users;
    private long taste;
    private float entriesPerMinute;

    public Stats(long entries, long feeds, long pulls, long users, long taste, float entriesPerMinute) {
        this.entries = entries;
        this.feeds = feeds;
        this.pulls = pulls;
        this.users = users;
        this.taste = taste;
        this.entriesPerMinute = entriesPerMinute;
    }

    public long getEntries() {
        return entries;
    }

    public float getEntriesPerMinute() {
        return entriesPerMinute;
    }

    public long getFeeds() {
        return feeds;
    }

    public long getPulls() {
        return pulls;
    }

    public long getTaste() {
        return taste;
    }

    public long getUsers() {
        return users;
    }

    public String toString() {
        return 
            "entries: " + entries + " " +
            "feeds: " + feeds + " " +
            "pulls: " + pulls + " " +
            "users: " + users + " " +
            "taste: " + taste + " " +
            "entriesPerMinute: " + entriesPerMinute;
    }
}
