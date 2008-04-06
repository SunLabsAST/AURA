/*
 * DBServiceImpl.java
 *
 * Created on February 27, 2008, 1:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.dbbrowser.server;
import com.sun.labs.aura.dbbrowser.client.AttnDesc;
import com.sun.labs.aura.dbbrowser.client.ItemDesc;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sun.labs.aura.aardvark.Aardvark;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.dbbrowser.client.DBService;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import java.rmi.RemoteException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import ngnova.util.StopWatch;

/**
 *
 */
public class DBServiceImpl extends RemoteServiceServlet implements
        DBService {
    
    protected static Aardvark aardvark;
    protected static DataStore store;
    protected static Logger logger = Logger.getLogger("");
    
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ServletContext context = getServletContext();
        aardvark = (Aardvark) context.getAttribute("aardvark");
        store = (DataStore)context.getAttribute("dataStore");
    }

    public ItemDesc[] searchItemByKey(String key) {
        try {
            String q = "aura-key <substring> " + key;
            StopWatch sw = new StopWatch();
            sw.start();
            List<Scored<Item>> res = store.query(q, 10, null);
            sw.stop();
            ItemDesc[] results = new ItemDesc[res.size() + 1];
            results[0] = new ItemDesc(sw.getTime());
            int i = 1;
            for (Scored<Item> si : res) {
                results[i++] = Factory.itemDesc(si.getItem());
            }
            return results;
        } catch (AuraException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public ItemDesc[] searchItemByName(String key) {
        try {
            String q = "aura-name <substring> " + key;
            StopWatch sw = new StopWatch();
            sw.start();
            List<Scored<Item>> res = store.query(q, 10, null);
            sw.stop();
            ItemDesc[] results = new ItemDesc[res.size() + 1];
            results[0] = new ItemDesc(sw.getTime());
            int i = 1;
            for (Scored<Item> si : res) {
                results[i++] = Factory.itemDesc(si.getItem());
            }
            return results;
        } catch (AuraException ex) {
            Logger.getLogger(DBServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(DBServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public ItemDesc[] searchItemByGen(String query) {
        try {
            StopWatch sw = new StopWatch();
            sw.start();
            List<Scored<Item>> res = store.query(query, 10, null);
            sw.stop();
            ItemDesc[] results = new ItemDesc[res.size() + 1];
            results[0] = new ItemDesc(sw.getTime());
            int i = 1;
            for (Scored<Item> si : res) {
                results[i++] = Factory.itemDesc(si.getItem());
            }
            return results;
        } catch (AuraException ex) {
            Logger.getLogger(DBServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(DBServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public ItemDesc[] findSimilar(String key) {
        try {
            StopWatch sw = new StopWatch();
            sw.start();
            List<Scored<Item>> res = store.findSimilar(key, "content", 10, null);
            sw.stop();
            ItemDesc[] results = new ItemDesc[res.size() + 1];
            results[0] = new ItemDesc(sw.getTime());
            int i = 1;
            for (Scored<Item> si : res) {
                results[i++] = Factory.itemDesc(si.getItem());
            }
            return results;
        } catch (AuraException ex) {
            Logger.getLogger(DBServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(DBServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    
    public AttnDesc[] getAttentionForSource(String key) {
        try {
            StopWatch sw = new StopWatch();
            sw.start();
            List<Attention> attn = store.getAttentionForSource(key);
            sw.stop();
            int numResults = Math.min(attn.size(), 100);
            AttnDesc[] results = new AttnDesc[numResults + 1];
            results[0] = new AttnDesc(sw.getTime(), attn.size());
            int i = 1;
            for (Attention a : attn) {
                if (i > numResults) {
                    break;
                }
                results[i++] = Factory.attnDesc(a);
            }
            return results;
        } catch (AuraException ex) {
            Logger.getLogger(DBServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(DBServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public AttnDesc[] getAttentionForTarget(String key) {
        try {
            StopWatch sw = new StopWatch();
            sw.start();
            List<Attention> attn = store.getAttentionForTarget(key);
            sw.stop();
            int numResults = Math.min(attn.size(), 100);
            AttnDesc[] results = new AttnDesc[numResults + 1];
            results[0] = new AttnDesc(sw.getTime(), attn.size());
            int i = 1;
            for (Attention a : attn) {
                if (i > numResults) {
                    break;
                }
                results[i++] = Factory.attnDesc(a);
            }
            return results;
        } catch (AuraException ex) {
            Logger.getLogger(DBServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(DBServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
