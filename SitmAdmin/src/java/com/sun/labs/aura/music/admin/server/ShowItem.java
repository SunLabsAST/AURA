/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */

package com.sun.labs.aura.music.admin.server;

import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.admin.client.WorkbenchResult;
import com.sun.labs.aura.util.AuraException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author plamere
 */
public class ShowItem extends Worker {

    ShowItem() {
        super("Show Item", "Shows an Item");
        param("key", "the item key", "");
    }

    @Override
    void go(MusicDatabase mdb, Map<String, String> params, WorkbenchResult result) throws AuraException, RemoteException {
        String key = getParam(params, "key");

        Item item = mdb.getDataStore().getItem(key);
        if (item == null) {
            result.fail("Can't find item with key " + key);
        }

        result.output("Key: " + item.getKey());
        result.output("Name: " + item.getName());
        result.output("Name: " + item.getType().name());
        result.output("Added: " + new Date(item.getTimeAdded()).toString());
        for (Entry<String, Serializable> entry : item) {
            Object o = entry.getValue();
            if (o instanceof Collection) {
                result.output(entry.getKey() + ":");
                Collection c = (Collection) o;
                for (Object i : c) {
                    result.output("    " + i);
                }
            } else {
                result.output(entry.getKey() + ":" + o);
            }
        }
    }
}
