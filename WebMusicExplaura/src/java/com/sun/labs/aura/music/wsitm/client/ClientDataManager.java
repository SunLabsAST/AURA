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
import com.sun.labs.aura.music.wsitm.client.ui.Updatable;
import com.sun.labs.aura.music.wsitm.client.ui.widget.PageHeaderWidget;
import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.Info;
import com.sun.labs.aura.music.wsitm.client.event.PlayedListener;
import com.sun.labs.aura.music.wsitm.client.event.TagCloudListener;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.items.ArtistDetails;
import com.sun.labs.aura.music.wsitm.client.items.ListenerDetails;
import com.sun.labs.aura.music.wsitm.client.ui.SharedSteeringMenu;
import com.sun.labs.aura.music.wsitm.client.ui.SharedTagMenu;
import com.sun.labs.aura.music.wsitm.client.ui.widget.steerable.TagWidgetContainer;
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
    private Map<String, String> recTypes;
    private String currRecTypeName;
    private String currSimTypeName;
    
    private Map<String, Double> favArtist;
    
    private PageHeaderWidget phw;

    private RatingListenerManager ratingListenerManager;
    private TaggingListenerManager taggingListenerManager;
    private LoginListenerManager loginListenerManager;
    private TagCloudListenerManager tagCloudListenerManager;
    private PlayedListenerManager playedListenerManager;
    
    private SteerableTagCloudExternalController steerableTagCloudExternalController;

    /**
     * If true, steerableswidget will reload artist cloud if querystring is set
     * If false, steerableswidget will keep the current cloud
     */
    private boolean forceSteerableReset = false;

    private Set<Swidget> registeredSwidgets;

    private ListenerDetails lD;

    private UniqueStore artistOracle;
    private UniqueStore tagOracle;

    private SharedTagMenu sharedTagMenu;
    private SharedSteeringMenu sharedSteeringMenu;

    public ClientDataManager() {
        lD = new ListenerDetails();
        registeredSwidgets = new HashSet<Swidget>();
        
        ratingListenerManager = new RatingListenerManager();
        taggingListenerManager = new TaggingListenerManager();
        loginListenerManager = new LoginListenerManager();
        tagCloudListenerManager = new TagCloudListenerManager();
        playedListenerManager = new PlayedListenerManager();
        steerableTagCloudExternalController = new SteerableTagCloudExternalController();

        sharedTagMenu = new SharedTagMenu(this);
        sharedSteeringMenu = new SharedSteeringMenu(this);

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
    
    public TagCloudListenerManager getTagCloudListenerManager() {
        return tagCloudListenerManager;
    }

    public PlayedListenerManager getPlayedListenerManager() {
        return playedListenerManager;
    }
    
    public SteerableTagCloudExternalController getSteerableTagCloudExternalController() {
        return steerableTagCloudExternalController;
    }

    public SharedTagMenu getSharedTagMenu() {
        return sharedTagMenu;
    }
    
    public SharedSteeringMenu getSharedSteeringMenu() {
        return sharedSteeringMenu;
    }

    public UniqueStore getTagOracle() {
        return tagOracle;
    }

    public UniqueStore getArtistOracle() {
        return artistOracle;
    }

    public void setTagOracle(UniqueStore tagOracle) {
        this.tagOracle = tagOracle;
    }

    public void setArtistOracle(UniqueStore artistOracle) {
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

    public void setWidgets(PageHeaderWidget phw) {
        this.phw = phw;
    }

    public PageHeaderWidget getPageHeaderWidget() {
        return phw;
    }

    public boolean getSteerableReset() {
        return forceSteerableReset;
    }

    public void setSteerableReset(boolean newVal) {
        forceSteerableReset = newVal;
    }

    public String getLastFmUser() {
        return lD.getLastFmUser();
    }

    public void setListenerDetails(ListenerDetails newlD) {
        //
        // If the logged in state has changed, we need to fire events
        if (lD.isLoggedIn()!=newlD.isLoggedIn()) {
            lD=newlD;
            if (lD.isLoggedIn()) {
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
        setListenerDetails(new ListenerDetails());
    }

    public boolean isLoggedIn() {
        return lD.isLoggedIn();
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

    public Map<String, String> getRecTypes() {
        return recTypes;
    }
    
    public void setRecTypes(Map<String, String> recTypes) {
        this.recTypes=recTypes;
    }
    
    public String getCurrRecTypeName() {
        return currRecTypeName;
    }
    
    public void setCurrRecTypeName(String currName) {
        this.currRecTypeName=currName;
    }

    
    public static String nameToKey(String name) {
        return "artist-tag:" + normalizeKey(name);
    }

    private static String normalizeKey(String key) {
        key = key.replaceAll("\\W", "").toLowerCase();
        return key;
    }
/*
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
   */
    
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
            if (itemIdBoundedListeners.containsKey(itemId) &&
                    itemIdBoundedListeners.get(itemId).contains(rL)) {
                itemIdBoundedListeners.get(itemId).remove(rL);
                if (itemIdBoundedListeners.get(itemId).size() == 0) {
                    itemIdBoundedListeners.remove(itemId);
                }
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

    public class PlayedListenerManager extends ListenerManager<PlayedListener> {

        public PlayedListenerManager() {
            super();
        }

        public void triggerOnPlay(String artistId) {
            for (PlayedListener pL : listeners) {
                pL.onPlay(artistId);
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
    
    public class TagCloudListenerManager extends ItemBoundedListenerManager<TagCloudListener> {

        private boolean active = true;

        public TagCloudListenerManager() {
            super();
        }
        
        public void triggerOnTagAdd(String tagId) {
            if (active) {
                for (TagCloudListener tcL : listeners) {
                    tcL.onTagAdd(tagId);
                }
                if (itemIdBoundedListeners.containsKey(tagId)) {
                    for (TagCloudListener tcL : itemIdBoundedListeners.get(tagId)) {
                        tcL.onTagAdd(tagId);
                    }
                }
            }
        }

        public void triggerOnTagDelete(String tagId) {
            if (active) {
                for (TagCloudListener tcL : listeners) {
                    tcL.onTagDelete(tagId);
                }
                if (itemIdBoundedListeners.containsKey(tagId)) {
                    for (TagCloudListener tcL : itemIdBoundedListeners.get(tagId)) {
                        tcL.onTagDelete(tagId);
                    }
                }
            }
        }
        
        public void triggerOnTagDeleteAll() {
            if (active) {
                for (TagCloudListener tcL : listeners) {
                    tcL.onTagDeleteAll();
                }
                for (String key : itemIdBoundedListeners.keySet()) {
                    for (TagCloudListener tcL : itemIdBoundedListeners.get(key)) {
                        tcL.onTagDeleteAll();
                    }
                }
            }
        }

        /**
         * Prevents any notifications from reaching listeners until notifications are reenabled
         */
        public void disableNotifications() {
            active = false;
        }

        /**
         * Allow notifications to reach listeners if they were previously disabled
         */
        public void enableNotifications() {
            active = true;
        }
    }
    
    public class SteerableTagCloudExternalController {
        
        private TagWidgetContainer tagLand;
        private boolean init;

        public SteerableTagCloudExternalController() {
            tagLand = null;
            init = false;
        }
        
        public SteerableTagCloudExternalController(TagWidgetContainer tagLand) {
            if (tagLand != null) {
                this.tagLand = tagLand;
                init = true;
            }
        }

        public void setTagWidget(TagWidgetContainer tagLand) {
            if (tagLand != null) {
                this.tagLand = tagLand;
                init = true;
            }
        }
        
        public void addTag(ItemInfo tag) {
            if (init) {
                tagLand.addTag(tag, 0, true);
            }
        }
        
        public boolean containsItem(String itemId) {
            if (init) {
                return tagLand.containsItem(itemId);
            } else {
                return false;
            }
        }

        public void addTags(ItemInfo[] tags) {
            if (init) {
                tagLand.addTags(tags, 10);
            }
        }
        
        public void addArtist(ArtistCompact aC) {
            if (init) {
                tagLand.addArtist(aC, 0);
            }
        }

        /**
         * Returns the weight of the maximum weighted item in the tag cloud
         * @return
         */
        public double getMaxWeight() {
            if (init) {
                return tagLand.getMaxWeight();
            } else {
                return -1;
            }
        }
    }
}
