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

package com.sun.labs.aura.bridge;

import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.ArtistTagRaw;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.util.props.ConfigurationManager;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 *
 * @author mailletf
 */
public class MusicDBBridge {

    private MusicDatabase mdb;

    public MusicDBBridge(ConfigurationManager cm) throws AuraException {
        mdb = new MusicDatabase(cm);
    }

    public Collection<Artist> getFavoriteArtists(String listenerID, int max)
            throws AuraException, RemoteException {
        return mdb.getFavoriteArtists(listenerID, max);
    }

    public Set<String> getFavoriteArtistKeys(String listenerID, int max)
            throws AuraException, RemoteException {
        return mdb.getFavoriteArtistKeys(listenerID, max);
    }

    public List<Scored<Artist>> artistSearch(String artistName, int returnCount) throws AuraException {
        return mdb.artistSearch(artistName, returnCount);
    }

    public List<Scored<ArtistTagRaw>> findSimilar(String key, int count) throws AuraException, RemoteException {
        return mdb.artistTagRawFindSimilar(key, count);
    }

}
