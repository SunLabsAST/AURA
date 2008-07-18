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
import java.util.Map;

/**
 *
 */
public class TagCounter extends ReplicantProcessor  {

    public void start() {
        try {
            Map<String,Integer> counts = new HashMap();
            for(Item item : replicant.getAll(Item.ItemType.BLOGENTRY)) {
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
    }

}
