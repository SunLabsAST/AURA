/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.wsitm.client;

import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.items.ArtistDetails;
import com.sun.labs.aura.music.wsitm.client.items.ListenerDetails;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author mailletf
 */
public class ClientDataManager {

    private String currArtist;
    
    private List<Updatable> updatableWidgets;
    
    private Map<String, String> simTypes;
    private String currSimTypeName;
    
    private ItemInfo[] tagCloud;
    private Map<String, Integer> tagMap; // maps the tag name to the index at which it is in the tagCloud
    
    private Double maxScore;
    private Map<String, Double> favArtist;
    private boolean isLoggedIn = false;
    
    private PageHeaderWidget phw;
    private SimpleSearchWidget ssw;

    private ListenerDetails lD;

    public ClientDataManager() {
        lD = new ListenerDetails();
    }

    public void setWidgets(PageHeaderWidget phw, SimpleSearchWidget ssw) {
        this.phw = phw;
        this.ssw = ssw;
    }

    public PageHeaderWidget getPageHeaderWidget() {
        return phw;
    }

    public SimpleSearchWidget getSimpleSearchWidget() {
        return ssw;
    }

    public Double getMaxScore() {
        return maxScore;
    }

    public ItemInfo[] getTagCloud() {
        return tagCloud;
    }

    public Map<String, Integer> getTagMap() {
        return tagMap;
    }

    public void setTagCloud(ItemInfo[] tagCloud, String lastFmUser,
            ArtistDetails[] artistDetails) {

        isLoggedIn = true;

        this.tagCloud = tagCloud;
        //this.lastFmUser = lastFmUser;

        tagMap = new HashMap<String, Integer>();
        for (int i = 0; i < tagCloud.length; i++) {
            tagMap.put(tagCloud[i].getId(), i);
        }

        favArtist = new HashMap<String, Double>();
        maxScore = -1.0;
        for (ArtistDetails aD : artistDetails) {
            double score = computeTastauraMeterScore(aD);
            if (score > maxScore) {
                maxScore = score;
            }
            //Window.alert("putting "+aD.getName()+" with "+score);
            favArtist.put(aD.getName(), score);
        }
    }

    public String getLastFmUser() {
        return lD.lastfmUser;
    }

    public void setListenerDetails(ListenerDetails lD) {
        this.lD=lD;
    }

    public ListenerDetails getListenerDetails() {
        return lD;
    }

    public Map<String, Double> getFavArtist() {
        return favArtist;
    }

    public void resetUser() {
        tagCloud = null;
        tagMap = null;
//        lastFmUser = null;

        isLoggedIn = false;
        lD = new ListenerDetails();
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public Map<String, String> getSimTypes() {
        return simTypes;
    }
    
    public void setSimTypes(Map<String, String> simTypes) {
        this.simTypes=simTypes;
    }
    
    public String getCurrSimTypeName() {
        return currSimTypeName;
    }
    
    public void setCurrSimTypeName(String currName) {
        this.currSimTypeName=currName;
    }
    
    public static String nameToKey(String name) {
        return "artist-tag:" + normalizeKey(name);
    }

    private static String normalizeKey(String key) {
        key = key.replaceAll("\\W", "").toLowerCase();
        return key;
    }

    public double computeTastauraMeterScore(ArtistDetails aD) {

        if (!isLoggedIn) {
            return -1;
        }

        //String s="";
        double score = 0.0;
        boolean a = false;
        for (ItemInfo i : aD.getFrequentTags()) {
            String nameKey = ClientDataManager.nameToKey(i.getItemName());
            if (tagMap.containsKey(nameKey)) {
                //s+=i.getItemName()+" "+((int)(tagCloud[tagMap.get(nameKey)].getScore()*100))+" x "+i.getScore()+"\n";
                int key = tagMap.get(nameKey);
                ItemInfo t = tagCloud[key];
                score += i.getScore() * t.getScore();
            }
        }
        //int normScore = (int)((double)score/cdm.getMaxScore()*100.0);
        //Window.alert(s);
        //Window.alert("Score:"+score+"\nMax score:"+cdm.getMaxScore());
        return score;
    }
    
    /**
     * Updates all the registered widgets with the new artist details information
     * @param aD new artist details
     */
    public void updateUpdatableWidgets(ArtistDetails aD) {
        for (Updatable u : updatableWidgets) {
            u.update(aD);
        }
    }
    
    /**
     * Clear all currently registered updatable widgets
     */
    public void clearUpdatableWidgets() {
        updatableWidgets = new LinkedList();
    }
    
    /**
     * Register new updatable widget
     * @param u
     */
    public void addUpdatableWidget(Updatable u) {
        if (updatableWidgets==null) {
            updatableWidgets = new LinkedList();
        }
        updatableWidgets.add(u);
    }
    
    public void displayWaitIconUpdatableWidgets() {
        for (Updatable u : updatableWidgets) {
            u.displayWaitIcon();
        }
    }
    
    public void setCurrArtistID(String id) {
        this.currArtist=id;
    }
    
    public String getCurrArtistID() {
        return currArtist;
    }
}
