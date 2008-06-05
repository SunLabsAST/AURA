/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author mailletf
 */
public class ClientDataManager {

    private ItemInfo[] tagCloud;
    private Map<String,Integer> tagMap;
    private String lastFmUser;
    private Double maxScore;
    private String favArtistName;
    private boolean isLoggedIn=false;
    
    private PageHeaderWidget phw;
    private SimpleSearchWidget ssw;
    
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
            Double maxScore, String favArtistName) {
        
        isLoggedIn=true;
        
        this.tagCloud = tagCloud;
        this.lastFmUser = lastFmUser;
        this.maxScore = maxScore;
        this.favArtistName = favArtistName;
        
        tagMap = new HashMap<String, Integer>();
        for (int i = 0; i < tagCloud.length; i++) {
            tagMap.put(tagCloud[i].getItemName(), i);
        }
    }
    
    public String getLastFmUser() {
        return lastFmUser;
    }
    
    public String getFavArtistName() {
        return favArtistName;
    }
    
    public void resetUser() {
        tagCloud=null;
        tagMap=null;
        lastFmUser=null;
        
        isLoggedIn=false;
    }
    
    public boolean isLoggedIn() {
        return isLoggedIn;
    }
    
}
