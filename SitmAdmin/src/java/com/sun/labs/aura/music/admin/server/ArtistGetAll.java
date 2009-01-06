/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.admin.server;

import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.admin.client.WorkbenchResult;
import com.sun.labs.aura.util.AuraException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author plamere
 */
public class ArtistGetAll extends Worker {


    ArtistGetAll() {
        super("Artist Get All", "Gets All of the artist");
    }

    @Override
    void go( MusicDatabase mdb, Map<String, String> params, WorkbenchResult result) throws AuraException, RemoteException {
        List<Item> artists = mdb.getDataStore().getAll(ItemType.ARTIST);
        int row = 0;
        for (Item item : artists) {
            row++;
            result.output(row + " " + item.getName());
        }
    }
}
