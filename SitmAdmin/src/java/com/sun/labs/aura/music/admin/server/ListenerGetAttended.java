/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.admin.server;

import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.AttentionConfig;
import com.sun.labs.aura.datastore.DBIterator;
import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.Listener;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.admin.client.Constants.Bool;
import com.sun.labs.aura.music.admin.client.WorkbenchResult;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.Tag;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author plamere
 */
public class ListenerGetAttended extends Worker {

    public ListenerGetAttended() {
        super("Listener Attended", "Gets the set of items that this listener has been attended to");
        param("User key", "The key for the user", "");
        param("From Attention", "If true, collect the artists from the attention data, otherwise, " +
                "from the listener", Bool.values(), Bool.TRUE);
    }

    @Override
    void go(MusicDatabase mdb, Map<String, String> params, WorkbenchResult result) throws AuraException, RemoteException {
        String key = getParam(params, "User key");
        boolean fromAttention = getParamAsEnum(params, "From Attention") == Bool.TRUE;
        Listener listener = mdb.getListener(key);

        if (listener == null) {
            result.fail("Listener doesn't exisit");
            return;
        }

        if (fromAttention) {
            List<Scored<String>> sids = mdb.getAllArtistsAsIDs(key);


            long count = 0L;
            for (Scored<String> sid : sids) {
                Artist artist = mdb.artistLookup(sid.getItem());
                result.output(String.format("%.0f %s", sid.getScore(), artist.getName()));
                count += sid.getScore();
            }
            result.output("Total: " + count + " Uniques:" + sids.size());
        } else {
            List<Tag> ltags = listener.getFavoriteArtist();
            long count = 0L;
            for (Tag tag : ltags) {
                Artist artist = mdb.artistLookup(tag.getName());
                result.output(String.format("%d %s", tag.getCount(), artist.getName()));
                count += tag.getCount();
            }
            result.output("Total: " + count + " Uniques:" + ltags.size());
        }
    }
}
