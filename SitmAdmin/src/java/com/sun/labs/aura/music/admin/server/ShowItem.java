/*
 * Copyright 2008-2009 Sun Microsystems, Inc. All Rights Reserved.
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
