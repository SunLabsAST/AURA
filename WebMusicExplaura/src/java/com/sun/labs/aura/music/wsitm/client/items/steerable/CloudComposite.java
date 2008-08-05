/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.items.steerable;

import com.google.gwt.user.client.ui.Image;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author mailletf
 */
public class CloudComposite implements CloudItem {

    protected String itemId;
    protected String displayName;
    protected double weight;

    protected HashSet<CloudItem> items;

    public CloudComposite(String itemId, String displayName, double weight, HashSet<CloudItem> items) {
        this.itemId = itemId;
        this.displayName = displayName;
        this.items = items;
        this.weight = weight;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void addItem(CloudItem cI) {
        items.add(cI);
    }

    public void removeItem(CloudItem cI) {
        items.remove(cI);
    }

    public HashSet<CloudItem> getContainedItems() {
        return items;
    }

    public HashMap<String, Double> getTagMap() {
        HashMap<String, Double> tagMap = new HashMap<String, Double>();
        for (CloudItem cI : items) {
            HashMap<String, Double> ttM = cI.getTagMap();
            for (String k : ttM.keySet()) {
                if (tagMap.containsKey(k)) {
                    tagMap.put(k, tagMap.get(k) + cI.getWeight() * ttM.get(k));
                } else {
                    tagMap.put(k, ttM.get(k));
                }
            }
        }
        return tagMap;
    }
    
    public Image getImage() {
        return null;
    }

    public String getId() {
        return itemId;
    }

    public int compareTo(CloudItem o) {
        return new Double(getWeight()).compareTo(o.getWeight());
    }

}
