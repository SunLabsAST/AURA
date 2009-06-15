/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 37 * hash + (this.name != null ? this.name.hashCode() : 0);
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