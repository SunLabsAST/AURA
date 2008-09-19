/*
 * DBServiceImpl.java
 *
 * Created on February 27, 2008, 1:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.dbbrowser.server;
import com.sun.labs.aura.dbbrowser.client.query.AttnDesc;
import com.sun.labs.aura.dbbrowser.client.query.ItemDesc;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.AttentionConfig;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.SimilarityConfig;
import com.sun.labs.aura.dbbrowser.client.query.DBService;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.minion.util.StopWatch;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 */
public class DBServiceImpl extends RemoteServiceServlet implements
        DBService {
    
    protected static DataStore store;
    protected static Logger logger = Logger.getLogger("");
    
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ServletContext context = getServletContext();
        store = (DataStore)context.getAttribute("dataStore");
    }
    
    /**
     * Log the user and host when new sessions start
     * 
     * @param request the request
     * @param response the response
     */
    @Override
    public void service(HttpServletRequest request,
                        HttpServletResponse response)
            throws ServletException, IOException
    {
        HttpSession s = request.getSession();
        if (s.isNew()) {
            logger.info("New session started for "
                    + request.getRemoteUser()
                    + " from " + request.getRemoteHost());
        }
        super.service(request, response);
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
            SimilarityConfig fsc = new SimilarityConfig("content", 10);
            List<Scored<Item>> res = store.findSimilar(key, fsc);
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
    
    public HashMap getItemInfo(String key) {
        try {
            HashMap<String,String> result = new HashMap<String,String>();
            Item i = store.getItem(key);
            for (Entry<String,Serializable> ent : i) {
                String name = ent.getKey();
                Serializable val = ent.getValue();
                
                String display = val.toString();
                result.put(name, display);
            }
            return result;
        } catch (AuraException ex) {
            Logger.getLogger(DBServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(DBServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public AttnDesc[] doTest() {
        try {
            StopWatch sw = new StopWatch();
            sw.start();
            AttentionConfig ac = new AttentionConfig();
            ac.setTargetKey("854a1807-025b-42a8-ba8c-2a39717f1d25");
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MMM-yyyy hh:mm");
            Date date = null;
            try {
                date = sdf.parse("07-JUL-2008 10:39");
            }catch(java.text.ParseException p) {
                System.out.println(p.toString());
            }
            List<Attention> attn = store.getLastAttention(ac, 35);
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
