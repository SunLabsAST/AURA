/*
 * ItemInfo.java
 *
 * Created on March 5, 2007, 2:44 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.items;

import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.sun.labs.aura.music.wsitm.client.ClientDataManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

/**
 *
 * @author plamere
 */
public class ItemInfo implements IsSerializable {
    private String id;
    private String itemName;
    private double score;
    private double popularity;

    public ItemInfo() {
    }
    
    /**
     * Creates a new instance of ItemInfo
     * @param id 
     * @param itemName 
     * @param score 
     * @param popularity 
     */
    public ItemInfo(String id, String itemName, double score, double popularity)  {
        this.id = id;
        this.itemName = itemName;
        this.score = score;
        this.popularity = popularity;
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

    /**
     * Converts a map of tag names to score to an ItemInfo array
     * @param map Map of (tag name, score)
     * @return
     */
    public static ItemInfo[] mapToArray(HashMap<String, ScoredTag> map) {
        
        ItemInfo[] iIArray = new ItemInfo[map.size()];
        int index = 0;
        for (String tagName : map.keySet()) {
            double val = map.get(tagName).getScore();
            iIArray[index++] = new ItemInfo(ClientDataManager.nameToKey(tagName), tagName, val, val);
        }
        return iIArray;
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
            if (Random.nextBoolean()) {
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
