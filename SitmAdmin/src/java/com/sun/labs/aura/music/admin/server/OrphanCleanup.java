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

import com.sun.labs.aura.datastore.DBIterator;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.admin.client.Constants.Bool;
import com.sun.labs.aura.music.admin.client.Constants.Items;
import com.sun.labs.aura.music.admin.client.WorkbenchResult;
import com.sun.labs.aura.util.AuraException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author plamere
 */
public class OrphanCleanup extends Worker {

    OrphanCleanup() {
        super("Orphan Cleanup", "Cleans and removes orphaned items");
        param("Item Type", "The type of item to cleanup", Items.values(), Items.Video);
        param("Max to remove", "The maximum number of items to remove (0 for all)", 100);
        param("Dry run", "If true, perform a dry run (without deleting items)", Bool.values(), Bool.TRUE);
    }

    @Override
    void go( MusicDatabase mdb, Map<String, String> params, WorkbenchResult result) throws AuraException, RemoteException {
        Items type = (Items) getParamAsEnum(params, "Item Type");
        boolean dryRun = getParamAsEnum(params, "Dry run") == Bool.TRUE;
        int maxToRemove = getParamAsInt(params, "Max to remove");

        Set<String> usedItems = getUsedItems(mdb, type);

        int allCount = (int) mdb.getDataStore().getItemCount(convertType(type));

        result.output(String.format("%d     used items of type %s", usedItems.size(), type.name()));
        result.output(String.format("%d    found items of type %s", allCount, type.name()));
        result.output(String.format("%d orphaned items of type %s", allCount - usedItems.size(), type.name()));

        long cutoffTime = System.currentTimeMillis() - 60 * 60 * 1000;   // don't remove items added in the last hour

        List<String> keysToRemove = new ArrayList<String>();
        int tooFresh = 0;

        DBIterator<Item> iter = mdb.getDataStore().getAllIterator(convertType(type));
        try {
            int removed = 0;
            while (iter.hasNext()) {
                Item item = iter.next();
                if (!usedItems.contains(item.getKey())) {
                    if (item.getTimeAdded() < cutoffTime) {
                        keysToRemove.add(item.getKey());
                        removed++;
                    } else {
                        tooFresh++;
                    }
                }

                if (maxToRemove > 0 && removed >= maxToRemove) {
                    break;
                }
            }

        } finally {
            iter.close();
        }
        if (dryRun) {
            result.output("Dry Run - would remove " + keysToRemove.size() + " " + type.name() + " items.");
        } else {
            for (String key : keysToRemove) {
                mdb.getDataStore().deleteItem(key);
            }
            result.output("Removed " + keysToRemove.size() + " " + type.name() + " items.");
        }
        if (tooFresh > 0) {
            result.output(tooFresh + " " + type.name() + " not removed because they were just added");
        }
        result.output("Done");
    }

    Set<String> getAllItems(MusicDatabase mdb, Items type) throws AuraException, RemoteException {
        Set<String> results = new HashSet<String>();
        ItemType itemType = convertType(type);
        DBIterator<Item> iter = mdb.getDataStore().getAllIterator(itemType);

        try {
            while (iter.hasNext()) {
                Item item = iter.next();
                results.add(item.getKey());
            }

        } finally {
            iter.close();
        }

        return results;
    }

    Set<String> getUsedItems(MusicDatabase mdb, Items type) throws AuraException, RemoteException {
        Set<String> results = new HashSet<String>();
        DBIterator<Item> iter = mdb.getDataStore().getAllIterator(ItemType.ARTIST);

        try {
            while (iter.hasNext()) {
                Item item = iter.next();
                Artist artist = new Artist(item);
                switch (type) {
                    case Album:
                        results.addAll(artist.getAlbums());
                        break;

                    case Event:
                        results.addAll(artist.getEvents());
                        break;

                    case Photo:
                        results.addAll(artist.getPhotos());
                        break;

                    case Video:
                        results.addAll(artist.getVideos());
                        break;

                    default:

                        throw new AuraException("Unexpected type " + type);
                }

            }

        } finally {
            iter.close();
        }

        return results;
    }

    ItemType convertType(Items type) throws AuraException {
        if (type == Items.Album) {
            return ItemType.ALBUM;
        } else if (type == Items.Event) {
            return ItemType.EVENT;
        } else if (type == Items.Photo) {
            return ItemType.PHOTO;
        } else if (type == Items.Video) {
            return ItemType.VIDEO;
        } else {
            throw new AuraException("Unexpectd item type");
        }
    }
}
