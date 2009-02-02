/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.items.steerable;

import com.google.gwt.user.client.ui.Image;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.items.ScoredTag;
import com.sun.labs.aura.music.wsitm.client.ui.TagDisplayLib.TagColorType;
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
    private boolean sticky;
   
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

    @Override
    public String getDisplayName() {
        return tagName;
    }

    @Override
    public double getWeight() {
        return tagWeight;
    }

    @Override
    public void setWeight(double tagWeight) {
        this.tagWeight = tagWeight;
    }

    @Override
    public HashSet<CloudItem> getContainedItems() {
        HashSet<CloudItem> ciL = new HashSet<CloudItem>();
        ciL.add(this);
        return ciL;
    }

    @Override
    public HashMap<String, ScoredTag> getTagMap() {
        HashMap<String, ScoredTag> tagMap = new HashMap<String, ScoredTag>();
        // Take the absolute of tagWeight because this weight will be used multiplied
        // with itself by TagWidget which will make it always positive
        tagMap.put(tagName, new ScoredTag(tagName, Math.abs(tagWeight), sticky));
        return tagMap;
    }

    @Override
    public Image getImage() {
        return null;
    }

    @Override
    public Image getIcon() {
        return new Image("icon-t.jpg");
    }

    @Override
    public String getId() {
        return tagId;
    }

    @Override
    public int compareTo(CloudItem o) {
        return new Double(getWeight()).compareTo(o.getWeight());
    }

    @Override
    public void setSticky(boolean sticky) {
        this.sticky = sticky;
    }

    @Override
    public boolean isSticky() {
        return this.sticky;
    }

    @Override
    public TagColorType getTagColorType() {
        if (this.isSticky()) {
            return TagColorType.STICKY_TAG;
        } else {
            return TagColorType.TAG;
        }
    }

    @Override
    public CloudItemType getCloudItemType() {
        return CloudItemType.TAG;
    }
}
