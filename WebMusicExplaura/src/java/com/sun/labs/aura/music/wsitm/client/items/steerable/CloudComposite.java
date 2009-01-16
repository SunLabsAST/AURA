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
public abstract class CloudComposite implements CloudItem {

    protected String itemId;
    protected String displayName;
    protected double weight;
    protected boolean sticky;

    protected HashSet<CloudItem> items;

    public CloudComposite(String itemId, String displayName, double weight, HashSet<CloudItem> items) {
        this.itemId = itemId;
        this.displayName = displayName;
        this.items = items;
        this.weight = weight;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void addItem(CloudItem cI) {
        items.add(cI);
    }

    public void removeItem(CloudItem cI) {
        items.remove(cI);
    }

    @Override
    public HashSet<CloudItem> getContainedItems() {
        return items;
    }

    @Override
    public HashMap<String, Double> getTagMap() {
        double maxVal = 0;
        double newVal = 0;
        HashMap<String, Double> tagMap = new HashMap<String, Double>();
        for (CloudItem cI : items) {
            HashMap<String, Double> ttM = cI.getTagMap();
            for (String k : ttM.keySet()) {
                if (tagMap.containsKey(k)) {
                    newVal = tagMap.get(k) + cI.getWeight() * ttM.get(k);
                } else {
                    newVal = cI.getWeight() * ttM.get(k);
                }
                tagMap.put(k, newVal);
                
                if (newVal > maxVal) {
                    maxVal = newVal;
                }
            }
        }
        
        // Normalise all values
        for (String key : tagMap.keySet()) {
            tagMap.put(key, tagMap.get(key) / maxVal);
        }
        
        return tagMap;
    }
    
    @Override
    public Image getImage() {
        return null;
    }

    @Override
    public String getId() {
        return itemId;
    }

    @Override
    public int compareTo(CloudItem o) {
        return new Double(getWeight()).compareTo(o.getWeight());
    }

    @Override
    public void setSticky(boolean sticky) {
        this.sticky = sticky;
        for (CloudItem cI : items) {
            cI.setSticky(sticky);
        }
    }

    @Override
    public boolean isSticky() {
        return this.sticky;
    }

}
