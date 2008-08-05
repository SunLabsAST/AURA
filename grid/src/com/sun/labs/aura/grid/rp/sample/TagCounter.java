/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.grid.rp.sample;

import com.sun.caroline.platform.FileSystem;
import com.sun.caroline.platform.GridFactory;
import com.sun.caroline.platform.ProcessContext;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.AttentionConfig;
import com.sun.labs.aura.datastore.DBIterator;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.grid.rp.ReplicantProcessor;
import com.sun.labs.aura.grid.util.GridUtil;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class TagCounter extends ReplicantProcessor  {
    
    public void start() {
        logger.info("Starting: " + getClass().getName());
        
        try {
            FileOutputStream outStream =
                    new FileOutputStream(GridUtil.cacheFSMntPnt + File.separator +
                                         getClass().getSimpleName() + "." +
                                         replicant.getPrefix() + ".dump");
            PrintStream out = new PrintStream(outStream);
            AttentionConfig attentionFilter = new AttentionConfig();
        
            logger.info("Getting artists from replicant");
            List<Item> artists = replicant.getAll(Item.ItemType.ARTIST);
            logger.info("Processing " + artists.size() + " artists");
            for(Item item : artists) {
                logger.info("Processing: " + item.getName());
                attentionFilter.setTargetKey(item.getKey());
                Map<String,Integer> counts = new HashMap();
                DBIterator<Attention> attentionIterator =
                        replicant.getAttentionIterator(attentionFilter);
                while(attentionIterator.hasNext()) {
                    Attention attention = attentionIterator.next();
                    if(!counts.containsKey(attention.getSourceKey())) {
                        counts.put(attention.getSourceKey(), 0);
                    }
                    counts.put(attention.getSourceKey(),
                               counts.get(attention.getSourceKey()) + 1);
                }
                for(String key : counts.keySet()) {
                    out.println(item.getKey() + "\t" +
                                key + "\t" + counts.get(key));
                }
            }
            out.close();
        } catch(Exception ex) {
            logger.severe("Error getting iterator for entries: " + ex);
        }
    }

    public void stop() {
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        logger.info("Configured Logger: " + logger.getName());
    }
}
