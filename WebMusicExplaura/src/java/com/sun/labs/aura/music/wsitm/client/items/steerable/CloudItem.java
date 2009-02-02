/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.items.steerable;


import com.google.gwt.user.client.ui.Image;
import com.sun.labs.aura.music.wsitm.client.items.ScoredTag;
import com.sun.labs.aura.music.wsitm.client.ui.TagDisplayLib;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author mailletf
 */
public interface CloudItem extends Comparable<CloudItem> {

    public enum CloudItemType {
        TAG,
        ARTIST
    }

    public String getId();
    public String getDisplayName();
    
    public double getWeight();
    public void setWeight(double weight);

    public HashSet<CloudItem> getContainedItems();
    public HashMap<String, ScoredTag> getTagMap();

    public void setSticky(boolean sticky);
    public boolean isSticky();

    public TagDisplayLib.TagColorType getTagColorType();
    public CloudItemType getCloudItemType();
    
    /**
     * If the item can be represented by an image, return its url
     * @return
     */
    public Image getImage();
    public Image getIcon();
}
