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

import com.sun.labs.aura.datastore.AttentionConfig;
import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.Listener;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.admin.client.WorkbenchResult;
import com.sun.labs.aura.util.AuraException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *
 * @author plamere
 */
public class ListenerSummary extends Worker {

    private final static long millisPerDay = 1000L * 60 * 60 * 24;

    ListenerSummary() {
        super("Listener Summary", "Shows summary information for the listeners");
        param("count", "Number of listeners to show", 100);
    }

    @Override
    void go( MusicDatabase mdb, Map<String, String> params, WorkbenchResult result) throws AuraException, RemoteException {
        int count = getParamAsInt(params, "count");

        List<String> ids = getListenerIDs(mdb);

        String header = String.format("%4s %32s %4s %4s %4s %6s %8s %s",
                "#", "key", "age", "pull", "yob", "attn", "sex", "fav");
        int row = 1;
        for (int i = 0; i < count && i < ids.size(); i++) {
            Listener listener = mdb.getListener(ids.get(i));
            if (listener != null) {
                int ageInDays = (int) ((System.currentTimeMillis() - listener.getLastCrawl()) / millisPerDay);
                AttentionConfig ac = new AttentionConfig();
                ac.setSourceKey(listener.getKey());
                long attns = mdb.getDataStore().getAttentionCount(ac);
                Collection<Artist> favs = mdb.getFavoriteArtists(listener.getKey(), 1);
                String favArtistName = "";
                if (favs.size() > 0) {
                    favArtistName = favs.toArray(new Artist[favs.size()])[0].getName();
                }


                if (row % 24 == 0) {
                    result.output(header);
                }

                String s = String.format("%4d %32s %4d %4d %4d %6d %8s %s",
                        row++, listener.getKey(),
                        ageInDays, listener.getUpdateCount(), listener.getYearOfBirth(), attns, listener.getGender().name(), favArtistName);
                result.output(s);
            }
        }
    }
}
