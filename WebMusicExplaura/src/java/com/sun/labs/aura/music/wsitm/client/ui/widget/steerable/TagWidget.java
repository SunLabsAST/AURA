/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui.widget.steerable;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.ui.Composite;
import com.sun.labs.aura.music.wsitm.client.ClientDataManager;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.items.ScoredTag;
import com.sun.labs.aura.music.wsitm.client.items.steerable.CloudArtist;
import com.sun.labs.aura.music.wsitm.client.items.steerable.CloudItem;
import com.sun.labs.aura.music.wsitm.client.items.steerable.CloudTag;
import com.sun.labs.aura.music.wsitm.client.ui.swidget.SteeringSwidget.MainPanel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;


/**
 * All tag cloud manipulation UIs extend this class
 * @author mailletf
 */
public abstract class TagWidget extends Composite {

    /**
     * Use aboslue to add items using the exact specified weight and relative if
     * the items need to be assigned a relative weight based on the UI widget they
     * are being added to
     */
    public enum ITEM_WEIGHT_TYPE {
        RELATIVE,
        ABSOLUTE
    }
    
    public static final int NBR_TOP_TAGS_TO_ADD = 10;
    private MainPanel mainPanel;

    public TagWidget(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
    }

    public abstract void addItem(CloudItem item, boolean updateRecommendations);
    public abstract void addItems(HashMap<String, CloudItem> items, ITEM_WEIGHT_TYPE weightType, int limit);
    public abstract boolean containsItem(String itemId);
    public abstract HashMap<String, CloudItem> getItemsMap();
    public abstract double getMaxWeight();
    public abstract void redrawTagCloud();
    public abstract void removeItem(String itemId);
    public abstract void removeAllItems(boolean updateRecommendations);

    public void updateRecommendations() {
        mainPanel.invokeFetchNewRecommendations();
    }

    public final HashMap<String, ScoredTag> getTagMap() {

        double maxVal = 0;
        double newVal = 0;
        HashMap<String, ScoredTag> tagMap = new HashMap<String, ScoredTag>();
        
        for (CloudItem cI : getItemsMap().values()) {
            HashMap<String, ScoredTag> itemTags = cI.getTagMap();
            
            for (String tagName : itemTags.keySet()) {
                // @todo remove lowercase when engine is fixed
                String tagLower = tagName.toLowerCase();
                
                if (tagMap.containsKey(tagLower)) {
                    newVal = tagMap.get(tagLower).getScore() + cI.getWeight() * itemTags.get(tagName).getScore();
                    tagMap.get(tagLower).setScore(newVal);

                    // Check if new copy of tag is sticky. If so, make old one sticky
                    if (itemTags.get(tagName).isSticky()) {
                        tagMap.get(tagLower).setSticky(true);
                    }
                } else {
                    newVal = cI.getWeight() * itemTags.get(tagName).getScore();
                    tagMap.put(tagLower, new ScoredTag(tagLower, newVal, itemTags.get(tagName).isSticky()));
                }
                
                if (Math.abs(newVal) > maxVal) {
                    maxVal = Math.abs(newVal);
                }
            }
        }
        
        // Normalise all values
        for (String key : tagMap.keySet()) {
            tagMap.get(key).setScore( tagMap.get(key).getScore() / maxVal );
        }

        return tagMap;
    }
    
    /**
     * Add the first 'limit' tags from the hashmap. Tags will not be sorted before
     * picking the first ones
     * @param tagMap
     * @param limit
     */
    public final void addTags(HashMap<String, Double> tagMap, int limit) {
        if (tagMap != null && !tagMap.isEmpty()) {
            int max = tagMap.size();
            if (limit > 0 && limit < max) {
                max = limit;
            }

            HashMap<String, CloudItem> tags = new HashMap<String, CloudItem>();
            int index = 0;
            for (String key : tagMap.keySet()) {
                Double val = tagMap.get(key);
                tags.put(ClientDataManager.nameToKey(key),
                        new CloudTag(ClientDataManager.nameToKey(key), key, val));
                if (index++ > limit) {
                    break;
                }
            }
            addItems(tags, ITEM_WEIGHT_TYPE.RELATIVE);
        }
    }

    public final void addArtist(ArtistCompact aC, double weight) {
        addItem(new CloudArtist(aC, weight), true);
    }

    public final void addTags(ItemInfo[] tag, int limit) {

        if (tag != null && tag.length > 0) {
            if (limit == 0) {
                limit = tag.length;
            }

            ArrayList<ItemInfo> iIList = ItemInfo.arrayToList(tag);
            HashMap<String, CloudItem> tags = new HashMap<String, CloudItem>();

            // Use only the top ten tags with the biggest score
            Collections.sort(iIList, ItemInfo.getScoreSorter());
            int nbr = 0;
            for (ItemInfo i : iIList) {
                tags.put(i.getId(), new CloudTag(i));
                if (nbr++ >= limit) {
                    break;
                }
            }

            addItems(tags, ITEM_WEIGHT_TYPE.RELATIVE, tags.size());

            DeferredCommand.addCommand(new Command() {

                public void execute() {
                    updateRecommendations();
                }
            });
        }
    }

    public final void addItems(HashMap<String, CloudItem> items, ITEM_WEIGHT_TYPE weightType) {
        addItems(items, weightType, items.size());
    }

    public final void addTag(ItemInfo tag, boolean updateRecommendations) {
        addItem(new CloudTag(tag), updateRecommendations);
    }

    public final void addTag(ItemInfo tag, double tagSize, boolean updateRecommendations) {
        CloudTag cT = new CloudTag(tag);
        cT.setWeight(tagSize);
        addItem(cT, updateRecommendations);
    }

    protected class CloudItemWeightSorter implements Comparator<CloudItem> {

        public int compare(CloudItem o1, CloudItem o2) {
            return -1 * new Double(o1.getWeight()).compareTo(o2.getWeight());
        }
    }

    protected class RandomSorter implements Comparator {

        public int compare(Object o1, Object o2) {
            if (Random.nextBoolean()) {
                return -1;
            } else {
                return 1;
            }
        }
    }
}