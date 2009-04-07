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

package com.sun.labs.aura.fb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

/**
 * A holder for tags and artists, lifted from the WME code.
 * 
 * @author plamere
 */
public class ItemInfo {
    private String id;
    private String itemName;
    private double score;
    private double popularity;
    private CONTENT_TYPE cT;
    private static Random random = new Random();

    public enum CONTENT_TYPE {
        TAG,
        ARTIST,
        ND
    }

    public ItemInfo() {
    }

    public ItemInfo(String id, String itemName, double score, double popularity)  {
        this.id = id;
        this.itemName = itemName;
        this.score = score;
        this.popularity = popularity;
    }

    /**
     * Creates a new instance of ItemInfo
     * @param id 
     * @param itemName 
     * @param score 
     * @param popularity 
     */
    public ItemInfo(String id, String itemName, double score, double popularity, CONTENT_TYPE cT)  {
        this.id = id;
        this.itemName = itemName;
        this.score = score;
        this.popularity = popularity;
        this.cT = cT;
    }

    public String getId() {
        return id;
    }

    public String getItemName() {
        return itemName;
    }

    public double getScore() {
        return score;
    }

    public double getPopularity() {
        return popularity;
    }
    
    public static Comparator<ItemInfo> getPopularitySorter() {
        return new PopularitySorter();
    }

    public static Comparator<ItemInfo> getNameSorter() {
        return new NameSorter();
    }

    public static Comparator<ItemInfo> getScoreSorter() {
        return new ScoreSorter();
    }

    public static Comparator<ItemInfo> getRandomSorter() {
        return new RandomSorter();
    }

    public static ArrayList<ItemInfo> arrayToList(ItemInfo[] iIArray) {
        ArrayList<ItemInfo> iI = new ArrayList<ItemInfo>();
        for (ItemInfo i : iIArray) {
            iI.add(i);
        }
        return iI;
    }


    public static class PopularitySorter implements Comparator<ItemInfo> {

        public int compare(ItemInfo o1, ItemInfo o2) {
            // Descending order
            return -1 * (new Double(o1.getPopularity()).compareTo(new Double(o2.getPopularity())));
        }
    }

    public static class ScoreSorter implements Comparator<ItemInfo> {

        public int compare(ItemInfo o1, ItemInfo o2) {
            // Descending order
            return -1 * (new Double(o1.getScore()).compareTo(new Double(o2.getScore())));
        }
    }

    public static class NameSorter implements Comparator<ItemInfo> {

        public int compare(ItemInfo o1, ItemInfo o2) {
            return o1.getItemName().compareTo(o2.getItemName());
        }
    }

    public static class RandomSorter implements Comparator<ItemInfo> {

        public int compare(ItemInfo o1, ItemInfo o2) {
            if (random.nextBoolean()) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    public static ItemInfo[] shuffle(ItemInfo[] itemInfo) {

        ItemInfo[] ii = new ItemInfo[itemInfo.length];

        for (int i = 0; i < itemInfo.length; i++) {
            ii[i] = itemInfo[i];
        }

        Arrays.sort(ii, getRandomSorter());
        return ii;
    }
}
