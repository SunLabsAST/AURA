/*
 * ServletTools.java
 *
 * Created on March 6, 2007, 11:16 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.wsitm.server;

import com.sun.labs.aura.music.MusicDatabase;
import java.rmi.RemoteException;
import javax.servlet.ServletConfig;

/**
 *
 * @author plamere
 */
public class ServletTools {

    /** Creates a new instance of ServletTools */
    private ServletTools() {
    }

    public static DataManager getDataManager(ServletConfig sc) {
        DataManager dm = (DataManager) sc.getServletContext().getAttribute("DataManager");
        MusicDatabase mdb = getMusicDatabase(sc);
        String cacheSizeString = (String) sc.getServletContext().getAttribute("cacheSize");

        int cacheSize = 500;
        cacheSizeString = sc.getInitParameter("cacheSize");
        if (cacheSizeString != null) {
            cacheSize = Integer.parseInt(cacheSizeString);
        }
        if (dm == null) {
            dm = new DataManager(mdb, cacheSize);
            sc.getServletContext().setAttribute("DataManager", dm);
        //@todo fix this
        //dm.getLogger().log("_system_", "startup", "Created datamanager with cache " + cacheSize);
        }
        return dm;
    }

    public static MusicDatabase getMusicDatabase(ServletConfig sc) {
        return (MusicDatabase) sc.getServletContext().getAttribute("MusicDatabase");
    }
}
