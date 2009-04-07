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

package com.sun.labs.aura.music.test;

import com.sun.labs.aura.music.webservices.api.Item;
import com.sun.labs.aura.music.webservices.api.Scored;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author plamere
 */
public class User implements Delayed {

    static Random rng = new Random();
    private Control control;
    private double averageActivityTime;
    private long nextTime;
    private Operation nextOperation;
    private List<OpEntry> operations;
    private String name;
    private boolean readOnly = true;
    private long lateSum = 0;
    private int lateCount = 0;

    public User(String name, Control control, boolean readOnly) {
        this.name = name;
        this.control = control;
        this.readOnly = true;

        operations = new ArrayList<OpEntry>();
        averageActivityTime = randomBetween(2, 150);

        createUser(name);

        if (!readOnly) {
            addOperation(new OpPlay(), 1);
            addOperation(new OPTag(), .1);
            addOperation(new OPRate(), .1);
            addOperation(new OPTagItem(), .1);
        }

        if (false) {
            addOperation(new OPGetAPML(), .01);
            addOperation(new OPGetListenerTags(), .01);
            addOperation(new OPGetRecommendations(), 1);
            addOperation(new OPFindSimilarListener(), .01);
            addOperation(new OPGetAttentionData(), 1);
        }

        addOperation(new OPArtistSearch(), .3);
        addOperation(new OPArtistSocialTags(), 1);
        addOperation(new OPArtistTagSearch(), .05);
        addOperation(new OPFindSimilarArtist(), 1);
        addOperation(new OPFindSimilarArtistFromWordCloud(), 2);
        addOperation(new OPFindSimilarArtistTags(), 1);
        addOperation(new OPGetItem(), 3);
        addOperation(new OPShowArtist(), 5);
        addOperation(new OPGetStats(), .001);

        calculateNextActivityTime();
        chooseNextOperation();
    }

    public void simulate() {
        if (nextOperation != null) {
            long late = -getDelay(TimeUnit.MILLISECONDS);
            if (late > 100L) {
                control.late(late);
            }
            long startTime = control.getMonitor().opStart();
            boolean ok;
            try {
                ok = nextOperation.go();
            } catch (IOException ex) {
                ok = false;
            }
            control.getMonitor().opFinish(name, nextOperation.toString(), startTime, ok);
        }
        calculateNextActivityTime();
        chooseNextOperation();
    }

    public long getDelay(TimeUnit unit) {
        return unit.convert(nextTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    public int compareTo(Delayed o) {
        long result = getDelay(TimeUnit.MILLISECONDS) -
                o.getDelay(TimeUnit.MILLISECONDS);
        return result < 0 ? -1 : result > 0 ? 1 : 0;
    }

    private void addOperation(Operation op, double prob) {
        OpEntry entry = new OpEntry(op, prob);
        operations.add(entry);
    }

    private double randomBetween(double low, double high) {
        double range = high - low;
        return low + range * rng.nextDouble();
    }

    private void chooseNextOperation() {
        double totalProb = getTotalProb();
        double choice = rng.nextDouble() * totalProb;
        nextOperation = null;

        double sum = 0;
        for (OpEntry entry : operations) {
            sum += entry.getProbability();
            if (choice < sum) {
                nextOperation = entry.getOp();
                break;
            }
        }
    }

    private double getTotalProb() {
        double sum = 0;
        for (OpEntry entry : operations) {
            sum += entry.getProbability();
        }
        return sum;
    }

    private double randomGaussianBetween(double low, double high) {
        int count = 5;
        double sum = 0;

        for (int i = 0; i < count; i++) {
            sum += randomBetween(low, high);
        }
        return sum / count;
    }

    private void calculateNextActivityTime() {
        long nextDelta = (long) (randomGaussianBetween(averageActivityTime * .6, averageActivityTime * 1.4) * 1000);
        nextTime = System.currentTimeMillis() + nextDelta;
    // System.out.println(name + " next activity is in " + nextDelta + " ms");
    }

    private String selectRandomArtistKey() {
        return control.getRandomArtistKey();
    }

    private String selectRandomArtistTagKey() {
        return control.getRandomArtistTagKey();
    }

    private void createUser(String name) {
        // System.out.println("Creating user: " + name + " avg activity time is " + averageActivityTime + " secs");
    }

    private void randomDelay() {
        double delay = randomGaussianBetween(.1, 5);
        try {
            Thread.sleep((long) (delay * 1000L));
        } catch (InterruptedException ex) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    abstract class Operation {

        private String name;

        Operation(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }

        public abstract boolean go() throws IOException;
    }

    class OpPlay extends Operation {

        OpPlay() {
            super("play");
        }

        public boolean go() {
            randomDelay();
            return true;
        }
    }

    class OPTag extends Operation {

        OPTag() {
            super("artisttag");
        }

        public boolean go() {
            randomDelay();
            return true;
        }
    }

    class OPRate extends Operation {

        OPRate() {
            super("rate");
        }

        public boolean go() {
            randomDelay();
            return true;
        }
    }

    class OPArtistSearch extends Operation {
        OPArtistSearch() {
            super("ArtistSearch");
        }

        public boolean go() throws IOException {
            String query = control.getRandomArtistName();
            control.getSitm().artistSearch(query);
            return true;
        }
    }

    class OPArtistSocialTags extends Operation {

        OPArtistSocialTags() {
            super("ArtistSocialTags");
        }

        public boolean go() throws IOException {
            control.getSitm().artistSocialTags(selectRandomArtistKey(), 20);
            return true;
        }
    }

    class OPShowArtist extends Operation {
        private String curArtistID = selectRandomArtistKey();
        private final static int MAX_GET = 15;

        OPShowArtist() {
            super("ShowArtist");
        }

        public boolean go() throws IOException {
            List<Scored<Item>> items = control.getSitm().findSimilarArtistsByKey(curArtistID, MAX_GET);
            if (items.size() > 0) {
                Collections.shuffle(items);
                curArtistID = items.get(0).getItem().getKey();
                control.getSitm().getItems(scoredItemsToKeyList(items), true);
            }
            return true;
        }
    }

    private List<String> scoredItemsToKeyList(List<Scored<Item>> items) {
        List<String> retlist = new ArrayList<String>();
        for (Scored<Item> sitem : items) {
            retlist.add(sitem.getItem().getKey());
        }
        return retlist;
    }

    class OPArtistTagSearch extends Operation {
        OPArtistTagSearch() {
            super("ArtistTagSearch");
        }

        public boolean go() throws IOException {
            String query = control.getRandomArtistTagName();
            control.getSitm().tagSearch(query, 20);
            return true;
        }
    }

    class OPFindSimilarArtist extends Operation {

        OPFindSimilarArtist() {
            super("FindSimilarArtist");
        }

        public boolean go() throws IOException {
            String key = selectRandomArtistKey();
            control.getSitm().findSimilarArtistsByKey(key, 100);
            return true;
        }
    }

    class OPFindSimilarArtistFromWordCloud extends Operation {

        OPFindSimilarArtistFromWordCloud() {
            super("ArtistFindSimilarArtistFromWordCloud");
        }

        public boolean go() throws IOException {
            String wordCloud = control.getRandomWordCloud();
            control.getSitm().findSimilarArtistFromWordCloud(wordCloud, 20);
            return true;
        }
    }

    class OPFindSimilarArtistTags extends Operation {

        OPFindSimilarArtistTags() {
            super("FindSimilarArtistTags");
        }

        public boolean go() throws IOException {
            String key = selectRandomArtistTagKey();
            control.getSitm().findSimilarArtistTags(key, 100);
            return true;
        }
    }

    class OPFindSimilarListener extends Operation {

        OPFindSimilarListener() {
            super("FindSimilarListener");
        }

        public boolean go() {
            return true;
        }
    }

    class OPGetAPML extends Operation {

        OPGetAPML() {
            super("GetAPML");
        }

        public boolean go() {
            randomDelay();
            return true;
        }
    }

    class OPGetAttentionData extends Operation {

        OPGetAttentionData() {
            super("GetAttentionData");
        }

        public boolean go() {
            randomDelay();
            return true;
        }
    }

    class OPGetItem extends Operation {

        OPGetItem() {
            super("GetItem");
        }

        public boolean go() throws IOException {
            String key = selectRandomArtistKey();
            control.getSitm().getItem(key, false);
            return true;
        }
    }

    class OPGetListenerTags extends Operation {

        OPGetListenerTags() {
            super("GetListenerTags");
        }

        public boolean go() {
            randomDelay();
            return true;
        }
    }

    class OPGetRecommendations extends Operation {

        OPGetRecommendations() {
            super("GetRecommendations");
        }

        public boolean go() {
            randomDelay();
            return true;
        }
    }

    class OPGetStats extends Operation {

        OPGetStats() {
            super("GetStats");
        }

        public boolean go() throws IOException {
            control.getSitm().getStats();
            return true;
        }
    }

    class OPTagItem extends Operation {

        OPTagItem() {
            super("TagItem");
        }

        public boolean go() {
            randomDelay();
            return true;
        }
    }

    class OpEntry {

        private Operation op;
        private double probability;

        public OpEntry(Operation op, double probability) {
            this.op = op;
            this.probability = probability;
        }

        public Operation getOp() {
            return op;
        }

        public double getProbability() {
            return probability;
        }
    }
}
