/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.grid.rp.sample;

import com.sun.labs.aura.aardvark.BlogEntry;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.grid.rp.ReplicantProcessor;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
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
            Map<String,Integer> counts = new HashMap();
            logger.info("Getting artists from replicant");
            List<Item> artists = replicant.getAll(Item.ItemType.ARTIST);
            logger.info("Processing " + artists.size() + " artists");
            for(Item item : artists) {
                logger.info("Processing: " + item.getName());
                item.getField(BlogEntry.FIELD_TAG);
            }
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
