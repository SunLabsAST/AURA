/*
 * Copyright 2007-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package com.sun.labs.aura.grid.util;

import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.SimilarityConfig;
import com.sun.labs.aura.grid.ServiceAdapter;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.WordCloud;
import com.sun.labs.util.props.ConfigComponent;
import com.sun.labs.util.props.ConfigStringList;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.rmi.RemoteException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class QueryDataStore extends ServiceAdapter {

    @ConfigComponent(type=com.sun.labs.aura.datastore.DataStore.class)
    public static final String PROP_DATA_STORE = "dataStore";

    private DataStore dataStore;

    @ConfigStringList(defaultList={"冨田勲"})
    public static final String PROP_QUERIES = "queries";

    private List<String> queries;

    @ConfigStringList(defaultList={"e86ab653-bec8-46f3-b4b6-a1a866919ef6"})
    public static final String PROP_KEYS = "keys";

    private List<String> keys;
    
    @Override
    public String serviceName() {
        return getClass().getName();
    }

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        dataStore = (DataStore) ps.getComponent(PROP_DATA_STORE);
        logger.info(String.format("ds: %s", dataStore));
        queries = ps.getStringList(PROP_QUERIES);
        keys = ps.getStringList(PROP_KEYS);
    }

    @Override
    public void start() {
        try {
            for (String q : queries) {
                String query = String.format("aura-name <matches> \"*%s*\"",
                        q);
                logger.info("query: " + query);
                List<Scored<Item>> r = dataStore.query(query, 10, null);
                for (Scored<Item> i : r) {
                    logger.info(String.format("%.2f %s %s", i.getScore(), i.getItem().
                            getKey(), i.getItem().getName()));
                }
            }

            for(String k : keys) {
                logger.info("key: " + k);
                SimilarityConfig config = new SimilarityConfig("socialTags");
                config.setSkimPercent(1);
                config.setReportPercent(1);
                List<Scored<Item>> r = dataStore.findSimilar(k, config);
                for (Scored<Item> i : r) {
                    logger.info(String.format("%6.3f %s %s", i.getScore(), i.getItem().
                            getKey(), i.getItem().getName()));
                }
            }
        } catch (AuraException ex) {
            logger.log(Level.SEVERE, "Aura exception", ex);
        } catch (RemoteException ex) {
            logger.log(Level.SEVERE, "Aura exception", ex);
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Throwable?", t);
        }
    }

    @Override
    public void stop() {
    }

}
