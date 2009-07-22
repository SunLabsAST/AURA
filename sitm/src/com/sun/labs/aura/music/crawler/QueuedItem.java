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

package com.sun.labs.aura.music.crawler;

import com.sun.labs.aura.music.web.lastfm.LastArtist;
import java.io.Serializable;

/**
 *
 * @author mailletf
 */
/**
 * Represents an artist in the queue. The queue is sorted by inverse popularity
 * (highly popular artists move to the head of the queue).
 */
public class QueuedItem implements Serializable {

    private String name;
    private String key;
    private int popularity;

    public QueuedItem() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof QueuedItem) {
            QueuedItem qI = ((QueuedItem) obj);
            if (key != null && qI.getKey() != null) {
                return qI.getKey().equals(this.getKey());
            } else if (name != null && qI.getName() != null) {
                return qI.getName().equals(this.getName());
            } else {
                throw new RuntimeException("Trying to compare QueuedItem with " +
                        "name and key fields not set.");
            }
        } else if (obj instanceof String) {
            // If we're comparing with a string, it has to be an mbid
            if (key != null && key.equals(obj)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + (this.key != null ? this.key.hashCode() : 0);
        return hash;
    }

    /**
     * Creates a QueuedArtist
     * @param artist the artist of interest
     * @param popularity the artist popularity
     */
    public QueuedItem(LastArtist artist, int popularity) {
        this.name = artist.getArtistName();
        this.key = artist.getMbaid();
        this.popularity = popularity;
    }

    public QueuedItem(String key, int popularity) {
        this.key = key;
        this.popularity = popularity;
    }

    public QueuedItem(String key) {
        this.key = key;
    }

    /**
     * Gets the artist name
     * @return the artist name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the musicbrainz ID for the artist
     * @return the MB ID for the artist
     */
    public String getKey() {
        return key;
    }

    /**
     * Gets the priority for this queued artist
     * @return the priority
     */
    public int getPriority() {
        return -popularity;
    }

    /**
     * Gets the popularity for this artist
     * @return the popularity (higher is more popular)
     */
    public int getPopularity() {
        return popularity;
    }

    @Override
    public String toString() {
        return getPopularity() + "/" + getName();
    }
}
