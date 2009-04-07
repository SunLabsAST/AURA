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

import com.sun.labs.aura.music.ArtistTag;
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
public class ArtistTagFindSimilar extends Worker {

    ArtistTagFindSimilar() {
        super("ArtistTag Find Similar", "Finds similar artist tags to a seed artist tag");
        param("ArtistTag", "The artist tag", "");
        param("count", "The number of similar artist tags to return", 20);
    }

    @Override
    void go(MusicDatabase mdb, Map<String, String> params, WorkbenchResult result) throws AuraException, RemoteException {
        String artistTagName = getParam(params, "ArtistTag");
        int count = getParamAsInt(params, "count");
        ArtistTag artistTag = lookupArtistTag(mdb, artistTagName);
        List<Scored<ArtistTag>> sartistTags = mdb.artistTagFindSimilar(artistTag.getKey(), count);
        dumpArtistTags(mdb, result, sartistTags);
    }

}

