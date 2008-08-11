/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.items.steerable;

import com.google.gwt.user.client.ui.Image;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.ui.ColorConfig;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author mailletf
 */
public class CloudTag implements CloudItem {

    private String tagId;
    private String tagName;
    private double tagWeight;
    
    private static final ColorConfig[] color = {
        new ColorConfig("#D4C790", "#D49090"),
        new ColorConfig("#ADA376", "#AD7676")
    };
    
    public CloudTag(String tagId, String tagName, double tagWeight) {
        this.tagId = tagId;
        this.tagName = tagName;
        this.tagWeight = tagWeight;
    }

    public CloudTag(ItemInfo tag) {
        this(tag, false);
    }

    public CloudTag(ItemInfo tag, boolean useDefaultSize) {
        this.tagId = tag.getId();
        this.tagName = tag.getItemName();
        if (useDefaultSize) {
            this.tagWeight = 0;
        } else {
            this.tagWeight = tag.getScore();
        }
    }
    
    public String getDisplayName() {
        return tagName;
    }
    
    public double getWeight() {
        return tagWeight;
    }

    public void setWeight(double tagWeight) {
        this.tagWeight = tagWeight;
    }

    public HashSet<CloudItem> getContainedItems() {
        HashSet<CloudItem> ciL = new HashSet<CloudItem>();
        ciL.add(this);
        return ciL;
    }

    public HashMap<String, Double> getTagMap() {
        HashMap<String, Double> tagMap = new HashMap<String, Double>();
        tagMap.put(tagName, 1.0);
        return tagMap;
    }

    public Image getImage() {
        return null;
    }

    public Image getIcon() {
        return new Image("icon-t.jpg");
    }
    
    public String getId() {
        return tagId;
    }

    public int compareTo(CloudItem o) {
        return new Double(getWeight()).compareTo(o.getWeight());
    }

    public ColorConfig[] getColorConfig() {
        return color;
    }
}
