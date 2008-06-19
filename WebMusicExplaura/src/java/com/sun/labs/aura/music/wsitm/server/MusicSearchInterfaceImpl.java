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
import com.sun.labs.aura.music.wsitm.client.items.ListenerDetails;
import com.sun.labs.aura.util.AuraException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
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
        //logger = dm.getLogger();
        //logger.log("_system_", "startup", "");
    }

    public SearchResults tagSearch(String searchString, int maxResults) throws WebException {
        logger.info("MusicSearchInterfaceImpl::tagSearch: "+searchString);
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
        logger.info("MusicSearchInterfaceImpl::artistSearch: "+searchString);
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
        logger.info("MusicSearchInterfaceImpl::artistSearchByTag: "+searchString);
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
        logger.info("MusicSearchInterfaceImpl::getArtistDetails: "+id);
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
        logger.info("MusicSearchInterfaceImpl::getTagDetails: "+tagName);
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
        logger.info("MusicSearchInterfaceImpl::getCommonTags for "+artistID1+" and "+artistID2);
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
        return dm.getArtistOracle();
    }
    
    public List<String> getTagOracle() {
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

    public ListenerDetails getLogInDetails() throws WebException {
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

    public Map<String, String> getSimTypes() {
            return dm.getSimTypes();
    }

}