/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.wsitm.client;

import com.sun.labs.aura.music.wsitm.client.event.WebListener;
import com.sun.labs.aura.music.wsitm.client.event.TaggingListener;
import com.sun.labs.aura.music.wsitm.client.event.RatingListener;
import com.sun.labs.aura.music.wsitm.client.event.LoginListener;
import com.sun.labs.aura.music.wsitm.client.ui.swidget.Swidget;
import com.sun.labs.aura.music.wsitm.client.ui.swidget.SimpleSearchSwidget;
import com.sun.labs.aura.music.wsitm.client.ui.Updatable;
import com.sun.labs.aura.music.wsitm.client.ui.widget.PageHeaderWidget;
import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.Info;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.gwtext.client.data.Store;
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

    private RatingListenerManager ratingListenerManager;
    private TaggingListenerManager taggingListenerManager;
    private LoginListenerManager loginListenerManager;

    /**
     * If true, steerableswidget will reload artist cloud if querystring is set
     * If false, steerableswidget will keep the current cloud
     */
    private boolean forceSteerableReset = false;

    private Set<Swidget> registeredSwidgets;

    private ListenerDetails lD;

    private Store artistOracle;
    private Store tagOracle;

    public ClientDataManager() {
        lD = new ListenerDetails();
        registeredSwidgets = new HashSet<Swidget>();
        
        ratingListenerManager = new RatingListenerManager();
        taggingListenerManager = new TaggingListenerManager();
        loginListenerManager = new LoginListenerManager();
    }

    public RatingListenerManager getRatingListenerManager() {
        return ratingListenerManager;
    }

    public TaggingListenerManager getTaggingListenerManager() {
        return taggingListenerManager;
    }

    public LoginListenerManager getLoginListenerManager() {
        return loginListenerManager;
    }

    public Store getTagOracle() {
        return tagOracle;
    }

    public Store getArtistOracle() {
        return artistOracle;
    }

    public void setTagOracle(Store tagOracle) {
        this.tagOracle = tagOracle;
    }

    public void setArtistOracle(Store artistOracle) {
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

    public void setListenerDetails(ListenerDetails newlD) {
        //
        // If the logged in state has changed, we need to fire events
        if (this.lD.loggedIn!=newlD.loggedIn) {
            this.lD=newlD;
            if (this.lD.loggedIn) {
                getLoginListenerManager().triggerOnLogin();
            } else {
                getLoginListenerManager().triggerOnLogout();
            }
        } else {
            this.lD=newlD;
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



    public class ListenerManager <T extends WebListener> {

        protected Set<T> listeners;

        public ListenerManager() {
            listeners = new HashSet<T>();
        }

        /**
         * Adds a RatingListener not bounded to a particular item id. Will be triggered whenever an item is rated
         * @param rL RatingListener to add
         */
        public void addListener(T rL) {
            listeners.add(rL);
        }

        /**
         * Removes the RatingListener that is not bounded to a particular item
         * @param rL RatingListener to remove
         */
        public void removeListener(T rL) {
            listeners.remove(rL);
        }

        public int countListeners() {
            return listeners.size();
        }
    }

    public class ItemBoundedListenerManager<T extends WebListener> extends ListenerManager<T> {

        protected Map<String, Set<T>> itemIdBoundedListeners;

        public ItemBoundedListenerManager() {
            super();
            itemIdBoundedListeners = new HashMap<String, Set<T>>();
        }

        /**
         * Adds a RatingListener bounded to the given itemId
         * @param itemId itemId to bound the listener to
         * @param rL RatingListener to add
         */
        public void addListener(String itemId, T rL) {
            if (!itemIdBoundedListeners.containsKey(itemId)) {
                itemIdBoundedListeners.put(itemId, new HashSet<T>());
            }
            itemIdBoundedListeners.get(itemId).add(rL);
        }

        public void removeListener(String itemId, T rL) {
            if (!itemIdBoundedListeners.get(itemId).contains(rL)) {
            }
            itemIdBoundedListeners.get(itemId).remove(rL);
            if (itemIdBoundedListeners.get(itemId).size() == 0) {
                itemIdBoundedListeners.remove(itemId);
            }
        }

        public int countItemBoundedListeners() {
            return itemIdBoundedListeners.size();
        }

    }

    public class LoginListenerManager extends ListenerManager<LoginListener> {

        public LoginListenerManager() {
            super();
        }

        public void triggerOnLogin() {
            for (LoginListener lL : listeners) {
                lL.onLogin(lD);
            }
        }

        public void triggerOnLogout() {
            for (LoginListener lL : listeners) {
                lL.onLogout();
            }
        }

    }

    public class RatingListenerManager extends ItemBoundedListenerManager<RatingListener> {

        public RatingListenerManager() {
            super();
        }

        public void triggerOnRate(String itemId, int rating) {
            for (RatingListener rL : listeners) {
                rL.onRate(itemId, rating);
            }
            if (itemIdBoundedListeners.containsKey(itemId)) {
                for (RatingListener rL : itemIdBoundedListeners.get(itemId)) {
                    rL.onRate(itemId, rating);
                }
            }
        }
    }

    public class TaggingListenerManager extends ItemBoundedListenerManager<TaggingListener> {

        public TaggingListenerManager() {
            super();
        }

        public void triggerOnTag(String itemId, Set<String> tags) {
            for (TaggingListener tL : listeners) {
                tL.onTag(itemId, tags);
            }
            if (itemIdBoundedListeners.containsKey(itemId)) {
                for (TaggingListener tL : itemIdBoundedListeners.get(itemId)) {
                    tL.onTag(itemId, tags);
                }
            }
        }
    }
}
