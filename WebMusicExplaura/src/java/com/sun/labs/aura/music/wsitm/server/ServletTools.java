/*
 * ServletTools.java
 *
 * Created on March 6, 2007, 11:16 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.server;

import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.util.AuraException;
import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

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
        String cacheSizeString = (String) sc.getServletContext().getAttribute("cacheSize");
        int cacheSize = 100;
        if (cacheSizeString != null) {
            cacheSize = Integer.parseInt(cacheSizeString);
        }
        if (dm == null) {
            try {
                /*
                String path =sc.getInitParameter("databasePath");
                if (path == null) {
                   path = "/home/fm223201/NetBeansProjects/MiniDatabase/minidatabase.db";
                }
                
                dm = new DataManager(path, cacheSize);
                sc.getServletContext().setAttribute("DataManager", dm);
                dm.getLogger().log("_system_", "startup", "Created datamanager at " + path + " cache " + cacheSize);
                */
                
                dm = DataManager.getDefault();
                sc.getServletContext().setAttribute("DataManager", dm);
                //@todo fix this
                //dm.getLogger().log("_system_", "startup", "Created datamanager with cache " + cacheSize);
            } catch (AuraException ex) {
                System.out.println("Can't create datamanager " + ex);
                return null;
            }
        }
        return dm;
    }
}
