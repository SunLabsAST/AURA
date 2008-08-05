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
public interface CloudItem extends Comparable<CloudItem> {

    public String getId();
    public String getDisplayName();
    
    public double getWeight();
    public void setWeight(double weight);

    public HashSet<CloudItem> getContainedItems();
    public HashMap<String, Double> getTagMap();

    /**
     * If the item can be represented by an image, return its url
     * @return
     */
    public Image getImage();
    
}
