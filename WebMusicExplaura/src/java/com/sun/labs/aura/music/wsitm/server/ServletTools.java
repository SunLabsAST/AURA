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
import java.io.IOException;
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
        //DataStore ds = (DataStore) sc.getServletContext().getAttribute("dataStore");
        MusicDatabase mdb = (MusicDatabase) sc.getServletContext().getAttribute("MusicDatabase");
        String cacheSizeString = (String) sc.getServletContext().getAttribute("cacheSize");
        
        int cacheSize;
        cacheSizeString = sc.getInitParameter("cacheSize");
        if (cacheSizeString != null) {
            cacheSize = Integer.parseInt(cacheSizeString);
        }
        if (dm == null) {
            try {                
                dm = new DataManager(mdb,500);
                sc.getServletContext().setAttribute("DataManager", dm);
                //@todo fix this
                //dm.getLogger().log("_system_", "startup", "Created datamanager with cache " + cacheSize);
            } catch (IOException ex) {
                System.out.println("Can't create datamanager " + ex);
                return null;
            }
        }
        return dm;
    }
}
