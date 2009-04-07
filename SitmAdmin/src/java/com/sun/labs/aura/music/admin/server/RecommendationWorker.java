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

import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.music.Listener;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.Recommendation;
import com.sun.labs.aura.music.RecommendationSummary;
import com.sun.labs.aura.music.RecommendationType;
import com.sun.labs.aura.music.admin.client.WorkbenchResult;
import com.sun.labs.aura.util.AuraException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author plamere
 */
public class RecommendationWorker extends Worker {

    public RecommendationWorker(MusicDatabase mdb) {
        super("Get Recommendations", "Gets Recommendations for a user");
        param("User key", "The key for the user", "");
        param("Recommendation Type", "The type of recommendation",
                getRecommendationTypes(mdb), mdb.getDefaultArtistRecommendationType().getName());
        param("count", "the maxiumum number of results to return", 15);
    }

    private String[] getRecommendationTypes(MusicDatabase mdb) {
        List<String> names = new ArrayList<String>();
        for (RecommendationType rtype : mdb.getArtistRecommendationTypes()) {
            names.add(rtype.getName());
        }
        return names.toArray(new String[names.size()]);
    }

    @Override
    void go(MusicDatabase mdb, Map<String, String> params, WorkbenchResult result) throws AuraException, RemoteException {
        String key = getParam(params, "User key");
        String rtypeName = getParam(params, "Recommendation Type");
        int count = getParamAsInt(params, "count");

        Listener listener = mdb.getListener(key);

        if (listener == null) {
            result.fail("Listener doesn't exisit");
            return;
        }

        RecommendationType rtype = mdb.getArtistRecommendationType(rtypeName);

        if (rtype == null) {
            result.fail("Can't find recommendation type " + rtypeName);
        }

        RecommendationSummary rs = rtype.getRecommendations(key, count, null);
        result.output(rs.getExplanation());
        result.output("");
        for (Recommendation r : rs.getRecommendations()) {
            Item item = mdb.getDataStore().getItem(r.getId());
            result.output(String.format("  %.4f %s", r.getScore(), item.getName()));
        }
    }
}
