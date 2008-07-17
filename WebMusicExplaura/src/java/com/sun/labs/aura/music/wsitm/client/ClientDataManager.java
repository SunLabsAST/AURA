/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.wsitm.client;

import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.Info;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.items.ArtistDetails;
import com.sun.labs.aura.music.wsitm.client.items.ListenerDetails;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author mailletf
 */
public class ClientDataManager {

    private String currArtist;
    private String currArtistName;
    private String currSearchWidgetToken = "searchHome:";

    private List<Updatable> updatableWidgets;
    
    private Map<String, String> simTypes;
    private String currSimTypeName;
    
    private ItemInfo[] tagCloud;
    private Map<String, Integer> tagMap; // maps the tag name to the index at which it is in the tagCloud
    
    private Double maxScore;
    private Map<String, Double> favArtist;
    
    private PageHeaderWidget phw;
    //private SimpleSearchSwidget ssw;

    /**
     * If true, steerableswidget will reload artist cloud if querystring is set
     * If false, steerableswidget will keep the current cloud
     */
    private boolean forceSteerableReset = false;

    private Set<Swidget> registeredSwidgets;

    private ListenerDetails lD;

    private MultiWordSuggestOracle artistOracle;
    private MultiWordSuggestOracle tagOracle;

    public ClientDataManager() {
        lD = new ListenerDetails();
        registeredSwidgets = new HashSet<Swidget>();
    }

    public MultiWordSuggestOracle getTagOracle() {
        return tagOracle;
    }

    public MultiWordSuggestOracle getArtistOracle() {
        return artistOracle;
    }

    public void setTagOracle(MultiWordSuggestOracle tagOracle) {
        this.tagOracle = tagOracle;
    }

    public void setArtistOracle(MultiWordSuggestOracle artistOracle) {
        this.artistOracle = artistOracle;
    }

    /**
     * Add swidget to the list of swidgets that will be notified of webevents such as login
     * @param s swidget to add
     */
    public void registerSwidget(Swidget s) {
        registeredSwidgets.add(s);
    }

    /**
     * Remove swidget from the list of swidgets that will be notified of webevents such as login
     * @param s swidget to add
     */
    public void unregisterSwidget(Swidget s) {
        registeredSwidgets.remove(s);
    }

    public void setWidgets(PageHeaderWidget phw, SimpleSearchSwidget ssw) {
        this.phw = phw;
        //this.ssw = ssw;
    }

    public PageHeaderWidget getPageHeaderWidget() {
        return phw;
    }

    /*
    public SimpleSearchSwidget getSimpleSearchWidget() {
        return ssw;
    }
     * */

    public boolean getSteerableReset() {
        return forceSteerableReset;
    }

    public void setSteerableReset(boolean newVal) {
        forceSteerableReset = newVal;
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
        //
        // If the logged in state has changed, we need to fire events
        if (this.lD.loggedIn!=lD.loggedIn) {
            if (this.lD.loggedIn) {
                this.lD=lD;
                for (Swidget s : registeredSwidgets) {
                    s.triggerLogout();
                }
            } else {
                this.lD=lD;
                for (Swidget s : registeredSwidgets) {
                    s.triggerLogin(lD);
                }
            }
        } else {
            this.lD=lD;
        }
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

        setListenerDetails(new ListenerDetails());

    }

    public boolean isLoggedIn() {
        return lD.loggedIn;
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

        if (true) {
            return 0;
        }

        if (!isLoggedIn()) {
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

    public void setCurrArtistInfo(String id, String name) {
        this.currArtist = id;
        this.currArtistName = name;
    }
    
    public String getCurrArtistID() {
        return currArtist;
    }

    public String getCurrArtistName() {
        return currArtistName;
    }

    public void setCurrSearchWidgetToken(String token) {
        this.currSearchWidgetToken=token;
    }

    public String getCurrSearchWidgetToken() {
        return currSearchWidgetToken;
    }
}
