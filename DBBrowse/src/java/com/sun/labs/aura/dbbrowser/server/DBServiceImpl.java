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
import com.sun.labs.aura.dbbrowser.client.DBService;

/**
 *
 */
public class DBServiceImpl extends RemoteServiceServlet implements
        DBService {
    

    public ItemDesc[] searchItemByKey(String key) {
        ItemDesc[] results = new ItemDesc[3];
        results[0] = new ItemDesc("Jeff", "jalex", "User");
        results[1] = new ItemDesc("Steve", "stgreen", "User");
        results[2] = new ItemDesc("Search Guy", "http://steves.blog/feed.xml", "Blog Feed");
        return results;
    }

    public ItemDesc[] searchItemByName(String key) {
        ItemDesc[] results = new ItemDesc[3];
        results[0] = new ItemDesc("Jeff", "jalex", "User");
        results[1] = new ItemDesc("Steve", "stgreen", "User");
        results[2] = new ItemDesc("Search Guy", "http://steves.blog/feed.xml", "Blog Feed");
        return results;
    }

    public AttnDesc[] getAttentionForSource(String key) {
        AttnDesc[] results = new AttnDesc[4];
        results[0] = new AttnDesc("key1", "key2", "STARRED", "Jan 1");
        results[1] = new AttnDesc("key1", "key3", "STARRED", "Jan 2");
        results[2] = new AttnDesc("key1", "key4", "VIEWED", "Jan 3");
        results[3] = new AttnDesc("key1", "key5", "SUBSCRIBED_FEED", "Jan 4");
        return results;
    }

    public AttnDesc[] getAttentionForTarget(String key) {
        AttnDesc[] results = new AttnDesc[4];
        results[0] = new AttnDesc("key1", "key2", "STARRED", "Jan 1");
        results[1] = new AttnDesc("key3", "key2", "VIEWED", "Jan 2");
        results[2] = new AttnDesc("key4", "key2", "STARRED", "Jan 3");
        results[3] = new AttnDesc("key5", "key2", "SUBSCRIBED_FEED", "Jan 4");
        return results;
    }

}
