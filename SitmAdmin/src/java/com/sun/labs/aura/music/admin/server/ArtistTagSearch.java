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

import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.ArtistTag;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.admin.client.TestStatus;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import java.rmi.RemoteException;
import java.util.List;

/**
 *
 * @author plamere
 */
class ArtistTagSearch extends Test {

    private int numTests;

    ArtistTagSearch(int numTests) {
        super("Artist Tag Search-" + numTests);
        this.numTests = numTests;
    }

    @Override
    protected void go(MusicDatabase mdb, TestStatus ts) throws AuraException, RemoteException {

        for (int i = 0; i < numTests; i++) {
            ArtistTag queryArtistTag = selectRandomArtistTag(mdb);
            List<Scored<ArtistTag>> results = mdb.artistTagSearch(queryArtistTag.getName(), 10);
            for (int j = 0; j < results.size(); j++) {
                if (results.get(j).getScore() > .97f && results.get(j).getItem().getKey().equals(queryArtistTag.getKey())) {
                    return;
                }
            }
            ts.fail("Search Fail for " + queryArtistTag.getName());
        }
    }
}
