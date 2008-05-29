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
import com.sun.labs.aura.music.wsitm.client.SearchResults;
import com.sun.labs.aura.music.wsitm.client.ArtistDetails;
import com.sun.labs.aura.music.wsitm.client.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.MusicSearchInterface;
import com.sun.labs.aura.music.wsitm.client.SearchResults;
import com.sun.labs.aura.music.wsitm.client.TagDetails;
import com.sun.labs.aura.music.wsitm.client.TagTree;
import com.sun.labs.aura.util.AuraException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

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
        super.init(sc);
        dm = ServletTools.getDataManager(sc);
        //logger = dm.getLogger();
        //logger.log("_system_", "startup", "");
    }

    public SearchResults tagSearch(String searchString, int maxResults) {
        //logger.log("anon", "tagSearch", searchString);
        return dm.tagSearch(searchString, maxResults);
    }

    public SearchResults artistSearch(String searchString, int maxResults) throws Exception {
        logger.info("MusicSearchInterfaceImpl::artistSearch: "+searchString);
        try {
            return dm.artistSearch(searchString, maxResults);
        } catch (Exception e) {
            logger.severe("MusicSearchInterfaceImpl::artistSearch Exception: "+e.getMessage());
            logger.severe(traceToString(e));
            throw e;
        }
           
    }

    public SearchResults artistSearchByTag(String searchString, int maxResults) 
            throws Exception {
        logger.info("MusicSearchInterfaceImpl::artistSearchByTag: "+searchString);
        try {
            return dm.artistSearchByTag(searchString, maxResults);
        } catch (Exception e) {
            logger.info("MusicSearchInterfaceImpl::artistSearchByTag Exception: "+e.getMessage());
            logger.severe(traceToString(e));
            throw e;
        }
                
    }

    public ArtistDetails getArtistDetails(String id, boolean refresh) throws Exception {      
        logger.info("MusicSearchInterfaceImpl::getArtistDetails: "+id);
        try {
            return dm.getArtistDetails(id, false);
        } catch (Exception e) {
            logger.severe("MusicSearchInterfaceImpl::getArtistDetails Exception: "+e.getMessage()+" / "+e.getCause());
            logger.severe(traceToString(e));
            throw e;
        }
    }

    public TagDetails getTagDetails(String id, boolean refresh) {
        //logger.log("anon", "getTagDetails", id);
        return dm.getTagDetails(id, refresh);
    }

    public TagTree getTagTree() {
        return dm.getTagTree();
    }
    
    public ItemInfo[] getCommonTags(String artistID1, String artistID2, int num) 
            throws Exception {
        logger.info("MusicSearchInterfaceImpl::getCommonTags for "+artistID1+" and "+artistID2);
        try {
            return dm.getCommonTags(artistID1, artistID2, num);
        } catch (Exception e) {
            logger.severe("MusicSearchInterfaceImpl::getCommonTags Exception: "+e.getMessage()+" / "+e.getCause());
            logger.severe(traceToString(e));
            throw e;
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
        String trace = "";
        for (StackTraceElement s : e.getStackTrace()) {
            trace += "    **   " + s + "\n";
        }
        return trace;
    }
}