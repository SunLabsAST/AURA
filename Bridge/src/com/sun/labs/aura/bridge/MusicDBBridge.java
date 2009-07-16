/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.bridge;

import com.sun.labs.aura.music.Artist;
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

}
