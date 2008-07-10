/*
 * MusicSearchInterfaceImpl.java
 *
 * Created on March 3, 2007, 7:47 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sun.labs.aura.music.ArtistTag;
import com.sun.labs.aura.music.wsitm.client.items.ArtistDetails;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.MusicSearchInterface;
import com.sun.labs.aura.music.wsitm.client.SearchResults;
import com.sun.labs.aura.music.wsitm.client.items.TagDetails;
import com.sun.labs.aura.music.wsitm.client.TagTree;
import com.sun.labs.aura.music.wsitm.client.WebException;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import com.sun.labs.aura.music.wsitm.client.items.ListenerDetails;
import com.sun.labs.aura.util.AuraException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

/**
 *
 * @author plamere
 */
public class MusicSearchInterfaceImpl extends RemoteServiceServlet 
        implements MusicSearchInterface {

    static int count;
    private DataManager dm;
    private Logger logger = Logger.getLogger("");

    @Override
    public void init(ServletConfig sc) throws ServletException {
        logger.info("Init");
        super.init(sc);
        dm = ServletTools.getDataManager(sc);
    }

    public SearchResults tagSearch(String searchString, int maxResults) throws WebException {
        logger.info("tagSearch: "+searchString);
        try {
            return dm.tagSearch(searchString, maxResults);
        } catch (AuraException ex) {
            logger.severe(traceToString(ex));
            throw new WebException(ex.getMessage(), ex);
        } catch (RemoteException ex) {
            logger.severe(traceToString(ex));
            throw new WebException(WebException.errorMessages.ITEM_STORE_COMMUNICATION_FAILED, ex);
        }

    }

    public SearchResults artistSearch(String searchString, int maxResults) throws WebException {
        logger.info("artistSearch: "+searchString);
        try {
            return dm.artistSearch(searchString, maxResults);
        } catch (AuraException ex) {
            logger.severe(traceToString(ex));
            throw new WebException(ex.getMessage(), ex);
        } catch (RemoteException ex) {
            logger.severe(traceToString(ex));
            throw new WebException(WebException.errorMessages.ITEM_STORE_COMMUNICATION_FAILED, ex);
        }
           
    }

    public SearchResults artistSearchByTag(String searchString, int maxResults) 
            throws WebException {
        logger.info("artistSearchByTag: "+searchString);
        try {
            // Make sure the tag has the right header
            if (!searchString.startsWith("artist-tag:")) {
                searchString=ArtistTag.nameToKey(searchString);
            }
            return dm.artistSearchByTag(searchString, maxResults);
        } catch (AuraException ex) {
            logger.severe(traceToString(ex));
            throw new WebException(ex.getMessage(), ex);
        } catch (RemoteException ex) {
            logger.severe(traceToString(ex));
            throw new WebException(WebException.errorMessages.ITEM_STORE_COMMUNICATION_FAILED, ex);
        }
                
    }

    public ArtistDetails getArtistDetails(String id, boolean refresh, String simTypeName) throws WebException {
        logger.info("getArtistDetails: "+id);
        try {
            return dm.getArtistDetails(id, false, simTypeName);
        } catch (AuraException ex) {
            logger.severe(traceToString(ex));
            throw new WebException(ex.getMessage(), ex);
        } catch (RemoteException ex) {
            logger.severe(traceToString(ex));
            throw new WebException(WebException.errorMessages.ITEM_STORE_COMMUNICATION_FAILED, ex);
        }
    }

    public TagDetails getTagDetails(String tagName, boolean refresh, String simTypeName) throws WebException {
        logger.info("getTagDetails: "+tagName);
        try {
            if (!tagName.startsWith("artist-tag:"))
                tagName=ArtistTag.nameToKey(tagName);
            return dm.getTagDetails(tagName, refresh, simTypeName);
        } catch (AuraException ex) {
            logger.severe(traceToString(ex));
            throw new WebException(ex.getMessage(), ex);
        } catch (RemoteException ex) {
            logger.severe(traceToString(ex));
            throw new WebException(WebException.errorMessages.ITEM_STORE_COMMUNICATION_FAILED, ex);
        }
    }

    public TagTree getTagTree() {
        return dm.getTagTree();
    }
    
    public ItemInfo[] getCommonTags(String artistID1, String artistID2, int num, String simType) 
            throws WebException {
        logger.info("getCommonTags for "+artistID1+" and "+artistID2);
        try {
            return dm.getCommonTags(artistID1, artistID2, num, simType);
        } catch (AuraException ex) {
            logger.severe(traceToString(ex));
            throw new WebException(ex.getMessage(), ex);
        } catch (RemoteException ex) {
            logger.severe(traceToString(ex));
            throw new WebException(WebException.errorMessages.ITEM_STORE_COMMUNICATION_FAILED, ex);
        }
    }

    public ItemInfo[] getCommonTags(Map<String, Double> tagMap, String artistID, int num) throws WebException {
        String stringMap = "";
        for (String key : tagMap.keySet()) {
            stringMap += key+":"+tagMap.get(key)+",";
        }
        logger.info("getCommonTags for "+artistID+" and cloud={"+stringMap+"}");
        try {
            return dm.getCommonTags(tagMap, artistID, num);
        } catch (AuraException ex) {
            logger.severe(traceToString(ex));
            throw new WebException(ex.getMessage(), ex);
        }
    }

    public void destroy() {
        super.destroy();
        try {
            //logger.log("_system_", "shutdown", "");
            dm.close();
        } catch (AuraException ex) {
            Logger.getLogger(MusicSearchInterfaceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(MusicSearchInterfaceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private String traceToString(Exception e) {
        String trace = "\n"+e.getClass()+"\n";
        for (StackTraceElement s : e.getStackTrace()) {
            trace += "    at  " + s + "\n";
        }
        return trace;
    }

    public List<String> getArtistOracle() {
        logger.info("getArtistOracle");
        return dm.getArtistOracle();
    }
    
    public List<String> getTagOracle() {
        logger.info("getTagOracle");
        return dm.getTagOracle();
    }
    
    public ListenerDetails getUserTagCloud(String lastfmUser, String simTypeName) throws WebException {
        try {
            return dm.getUserTagCloud(lastfmUser, simTypeName);
        } catch (AuraException ex) {
            logger.severe(traceToString(ex));
            throw new WebException(ex.getMessage(), ex);
        }
    }

    public ListenerDetails getNonOpenIdLogInDetails(String userKey) throws WebException {
        logger.info("getNonOpenIdLogInDetails for key:"+userKey);
        try {
            return dm.establishNonOpenIdUserConnection(userKey);
        } catch (AuraException ex) {
            logger.severe(traceToString(ex));
            throw new WebException(ex.getMessage(), ex);
        } catch (RemoteException ex) {
            logger.severe(traceToString(ex));
            throw new WebException(WebException.errorMessages.ITEM_STORE_COMMUNICATION_FAILED, ex);
        }
    }

    public ListenerDetails getLogInDetails() throws WebException {
        logger.info("getLogInDetails");
        try {
            ListenerDetails lD = new ListenerDetails();

            HttpSession session = this.getThreadLocalRequest().getSession();
            if (session.getAttribute(OpenIDServlet.openIdCookieName) != null) {
                lD.openID = (String) session.getAttribute(OpenIDServlet.openIdCookieName);
            }
            if (session.getAttribute(OpenIDServlet.ATTR_BIRTHDATE) != null) {
                lD.birthDate = (String) session.getAttribute(OpenIDServlet.ATTR_BIRTHDATE);
            }
            if (session.getAttribute(OpenIDServlet.ATTR_COUNTRY) != null) {
                lD.country = (String) session.getAttribute(OpenIDServlet.ATTR_COUNTRY);
            }
            if (session.getAttribute(OpenIDServlet.ATTR_EMAIL) != null) {
                lD.email = (String) session.getAttribute(OpenIDServlet.ATTR_EMAIL);
            }
            if (session.getAttribute(OpenIDServlet.ATTR_GENDER) != null) {
                lD.gender = (String) session.getAttribute(OpenIDServlet.ATTR_GENDER);
            }
            if (session.getAttribute(OpenIDServlet.ATTR_LANGUAGE) != null) {
                lD.language = (String) session.getAttribute(OpenIDServlet.ATTR_LANGUAGE);
            }
            if (session.getAttribute(OpenIDServlet.ATTR_NICKNAME) != null) {
                lD.nickName = (String) session.getAttribute(OpenIDServlet.ATTR_NICKNAME);
            }
            if (session.getAttribute(OpenIDServlet.ATTR_NAME) != null) {
                lD.realName = (String) session.getAttribute(OpenIDServlet.ATTR_NAME);
            }
            if (session.getAttribute(OpenIDServlet.ATTR_STATE) != null) {
                lD.state = (String) session.getAttribute(OpenIDServlet.ATTR_STATE);
            }

            if (lD.openID != null && (lD.realName != null || lD.nickName != null)) {
                lD.loggedIn = true;
                dm.establishUserConnection(lD);
            }
            return lD;

        } catch (AuraException ex) {
            logger.severe(traceToString(ex));
            throw new WebException(ex.getMessage(), ex);
        } catch (RemoteException ex) {
            logger.severe(traceToString(ex));
            throw new WebException(WebException.errorMessages.ITEM_STORE_COMMUNICATION_FAILED, ex);
        }
    }

    public void terminateSession() {
        logger.info("terminateSession");
        HttpSession session = this.getThreadLocalRequest().getSession();
        session.setAttribute(OpenIDServlet.openIdCookieName, null);
    }

    public void updateListener(ListenerDetails lD) throws WebException {
        logger.info("UpdateListener :: "+lD.openID);
        try {
            dm.updateUser(lD);
        } catch (AuraException ex) {
            logger.severe(traceToString(ex));
            throw new WebException(ex.getMessage(), ex);
        } catch (RemoteException ex) {
            logger.severe(traceToString(ex));
            throw new WebException(WebException.errorMessages.ITEM_STORE_COMMUNICATION_FAILED, ex);
        }
    }

    public void updateUserSongRating(ListenerDetails lD, int rating, String artistID) throws WebException {
        logger.info("UpdateUserSongRating :: user:"+lD.openID+" artist:"+artistID+" rating:"+rating);
        try {
            dm.updateUserSongRating(lD, rating, artistID);
        } catch (AuraException ex) {
            logger.severe(traceToString(ex));
            throw new WebException(ex.getMessage(), ex);
        } catch (RemoteException ex) {
            logger.severe(traceToString(ex));
            throw new WebException(WebException.errorMessages.ITEM_STORE_COMMUNICATION_FAILED, ex);
        }
    }

    public Integer fetchUserSongRating(ListenerDetails lD, String artistID) throws WebException {
        logger.info("fetchUserSongRating :: user:"+lD.openID+" artist:"+artistID);
        try {
            return new Integer(dm.fetchUserSongRating(lD, artistID));
        } catch (AuraException ex) {
            logger.severe(traceToString(ex));
            throw new WebException(ex.getMessage(), ex);
        } catch (RemoteException ex) {
            logger.severe(traceToString(ex));
            throw new WebException(WebException.errorMessages.ITEM_STORE_COMMUNICATION_FAILED, ex);
        }
    }

    public Map<String,Integer> fetchUserSongRating(ListenerDetails lD, Set<String> artistID) throws WebException {
        try {
            return dm.fetchUserSongRating(lD, artistID);
        } catch (AuraException ex) {
            logger.severe(traceToString(ex));
            throw new WebException(ex.getMessage(), ex);
        } catch (RemoteException ex) {
            logger.severe(traceToString(ex));
            throw new WebException(WebException.errorMessages.ITEM_STORE_COMMUNICATION_FAILED, ex);
        }
    }

    public Map<String, String> getSimTypes() {
         return dm.getSimTypes();
    }

    public ItemInfo[] getDistinctiveTags(String artistID, int count) throws WebException {
        try {
            return dm.getDistinctiveTags(artistID, count);
        } catch (AuraException ex) {
            logger.severe(traceToString(ex));
            throw new WebException(ex.getMessage(), ex);
        }
    }

    public ArtistCompact[] getSteerableRecommendations(Map<String, Double> tagMap) throws WebException {
        String stringMap = "";
        for (String key : tagMap.keySet()) {
            stringMap += key+":"+tagMap.get(key)+",";
        }
        logger.info("getSteerableRecommendations for cloud:{"+stringMap+"}");
        try {
            return dm.getSteerableRecommendations(tagMap);
        } catch (AuraException ex) {
            logger.severe(traceToString(ex));
            throw new WebException(ex.getMessage(), ex);
        } catch (RemoteException ex) {
            logger.severe(traceToString(ex));
            throw new WebException(WebException.errorMessages.ITEM_STORE_COMMUNICATION_FAILED, ex);
        }
    }

    public ArtistCompact getArtistCompact(String artistId) throws WebException {
        try {
            return dm.getArtistCompact(artistId);
        } catch (AuraException ex) {
            logger.severe(traceToString(ex));
            throw new WebException(ex.getMessage(), ex);
        } catch (RemoteException ex) {
            logger.severe(traceToString(ex));
            throw new WebException(WebException.errorMessages.ITEM_STORE_COMMUNICATION_FAILED, ex);
        }
    }
}