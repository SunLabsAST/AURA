/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.admin.server;

import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.admin.client.WorkbenchResult;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author plamere
 */
public class GeneralSearch extends Worker {

    GeneralSearch() {
        super("General Search", "Searches the datastore for any type of item");
        param("Query", "The query", "");
        param("Sort", "How to sort the results", "-score");
        param("maxResults", "The maximum results returned", 25);
    }

    @Override
    void go( MusicDatabase mdb, Map<String, String> params, WorkbenchResult result) throws AuraException, RemoteException {
        String query = getParam(params, "Query");
        String sort = getParam(params, "Sort");
        int max = getParamAsInt(params, "maxResults");

        List<Scored<Item>> items = mdb.getDataStore().query(query, sort, max, null);
        int i = 0;
        for (Scored<Item> sitem : items) {
            Item item = sitem.getItem();
            if (item != null) {
                result.output(String.format("%3d %6.4f %s %s", ++i, sitem.getScore(), item.getKey(), item.getName()));
            } else {
                result.output("Unexpected null item in result");
            }
        }
    }
}
