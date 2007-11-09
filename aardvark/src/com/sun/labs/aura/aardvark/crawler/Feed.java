/*
 *  Copyright 2007 Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES. 
 */

package com.sun.labs.aura.aardvark.crawler;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author plamere
 */
public class Feed implements Delayed {
    private String key;
    private URL feedUrl;
    private long lastPullTime;
    private long nextPullTime;
    private int totalPulls;
    private int totalErrors;
    private int consecutiveErrors;
    private List<UserAttention> interestedUsers;

    public Feed(String key, URL url) {
        this.key = key;
        this.feedUrl = url;
        interestedUsers = new ArrayList<UserAttention>();
    }
    
    public int getConsecutiveErrors() {
        return consecutiveErrors;
    }

    public URL getFeedUrl() {
        return feedUrl;
    }

    public long getNextPullTime() {
        return nextPullTime;
    }

    public void addInterestedUser(UserAttention userAttention) {
        interestedUsers.add(userAttention);
    }

    public List<UserAttention> getInterestedUsers() {
        return Collections.unmodifiableList(interestedUsers);
    }

    public void setStatus(boolean ok) {
        totalPulls++;
        lastPullTime = System.currentTimeMillis();
        if (ok) {
            consecutiveErrors = 0;
        } else {
            totalErrors++;
            consecutiveErrors++;
        }
    }

    public void setNextPullTime(long time) {
        nextPullTime = time;
    }

    public int getTotalErrors() {
        return totalErrors;
    }


    public int getTotalPulls() {
        return totalPulls;
    }


    public long getLastPullTime() {
        return lastPullTime;
    }

    public String getKey() {
        return key;
    }


    public long getDelay(TimeUnit unit) {
        long delay = nextPullTime - System.currentTimeMillis();
        return unit.convert(delay, TimeUnit.MILLISECONDS);
    }

    public int compareTo(Delayed o) {
        long result = getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS);
        return result < 0  ? -1 :  result > 0 ? 1 : 0;
    }
}

