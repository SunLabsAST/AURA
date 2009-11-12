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
            List<Scored<String>> sids = mdb.getAllArtistsAsIDsWithIterator(key);


            long count = 0L;
            for (Scored<String> sid : sids) {
                Artist artist = mdb.artistLookup(sid.getItem());
                result.output(String.format("%.0f %s", sid.getScore(), artist.getName()));
                count += sid.getScore();
            }
            result.output("Total: " + count + " Uniques:" + sids.size());
        } else {
            /*List<Tag> ltags = listener.getFavoriteArtist();
            long count = 0L;
            for (Tag tag : ltags) {
                Artist artist = mdb.artistLookup(tag.getName());
                result.output(String.format("%d %s", tag.getCount(), artist.getName()));
                count += tag.getCount();
            }
            result.output("Total: " + count + " Uniques:" + ltags.size());
             * */
            result.output("Didn't run");
        }
    }
}
