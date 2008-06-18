/*
 * ItemInfo.java
 *
 * Created on March 5, 2007, 2:44 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.items;

import com.google.gwt.user.client.rpc.IsSerializable;

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
}
