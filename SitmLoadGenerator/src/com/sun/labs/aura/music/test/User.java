/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.test;

import java.io.IOException;
import java.util.ArrayList;
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

    public User(String name, Control control) {
        this.name = name;
        this.control = control;

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
        addOperation(new OPGetStats(), .001);

        calculateNextActivityTime();
        chooseNextOperation();
    }

    public void simulate() {
        if (nextOperation != null) {
            long late = -getDelay(TimeUnit.MILLISECONDS);
            if (late > 100L) {
                System.out.printf("WARNING - simulator is overloaded, processed user %d ms late\n", late);
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

    private String selectKey() {
        return control.getRandomArtistKey();

    }

    private void createUser(String name) {
        System.out.println("Creating user: " + name + " avg activity time is " + averageActivityTime + " secs");
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
        int which = 0;
        private String[] searchString = {
            "beatles",
            "britney",
            "the",
            "breaking",
            "weezer",
            "deerhoof",
            "the the",
            "the who",
            "bj*rk",
        };

        OPArtistSearch() {
            super("ArtistSearch");
        }

        public boolean go() throws IOException {
            control.getSitm().artistSearch(searchString[which++ % searchString.length]);
            return true;
        }
    }

    class OPArtistSocialTags extends Operation {

        OPArtistSocialTags() {
            super("ArtistSocialTags");
        }

        public boolean go() throws IOException {
            control.getSitm().artistSocialTags(selectKey(), 20);
            return true;
        }
    }

    class OPArtistTagSearch extends Operation {
        private int which = 0;
        private String[] tagSearchStrings = {
            "emo", "punk", "metal", "rock", "female", "wrist", "fast", "trum", "sax"
        };

        OPArtistTagSearch() {
            super("ArtistTagSearch");
        }

        public boolean go() throws IOException {
            control.getSitm().tagSearch(tagSearchStrings[which++ % tagSearchStrings.length], 20);
            return true;
        }
    }

    class OPFindSimilarArtist extends Operation {

        OPFindSimilarArtist() {
            super("FindSimilarArtist");
        }

        public boolean go() throws IOException {
            String key = selectKey();
            control.getSitm().findSimilarArtists(key, 100);
            return true;
        }
    }

    class OPFindSimilarArtistFromWordCloud extends Operation {

        OPFindSimilarArtistFromWordCloud() {
            super("ArtistFindSimilarArtistFromWordCloud");
        }

        public boolean go() throws IOException {
            control.getSitm().findSimilarArtistFromWordCloud("emo,punk,metal,rock", 20);
            return true;
        }
    }

    class OPFindSimilarArtistTags extends Operation {

        OPFindSimilarArtistTags() {
            super("FindSimilarArtistTags");
        }

        public boolean go() throws IOException {
            String key = selectKey();
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
            String key = selectKey();
            control.getSitm().getItem(key);
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