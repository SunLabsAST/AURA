/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.admin.server;

import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.ArtistTag;
import com.sun.labs.aura.music.Listener;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.admin.client.WorkbenchResult;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Random;

/**
 *
 * @author plamere
 */
public class MDBHelper {

    static List<String> artistIDs;
    static List<String> listenerIDs;
    static List<String> artistTagIDs;
    static Random rng = new Random();
    static Object lock = new Object();
    // some test helper methods
    String selectRandomArtistKey(MusicDatabase mdb) throws AuraException, RemoteException {
        return getArtistIDs(mdb).get(rng.nextInt(artistIDs.size()));
    }

    Artist selectRandomArtist(MusicDatabase mdb) throws AuraException, RemoteException {
        return mdb.artistLookup(selectRandomArtistKey(mdb));
    }

    String selectRandomArtistTagKey(MusicDatabase mdb) throws AuraException, RemoteException {
        return getArtistTagIDs(mdb).get(rng.nextInt(artistTagIDs.size()));
    }

    ArtistTag selectRandomArtistTag(MusicDatabase mdb) throws AuraException, RemoteException {
        return mdb.artistTagLookup(selectRandomArtistTagKey(mdb));
    }

    String selectRandomListenerKey(MusicDatabase mdb) throws AuraException, RemoteException {
        return getListenerIDs(mdb).get(rng.nextInt(listenerIDs.size()));
    }

    Listener selectRandomListener(MusicDatabase mdb) throws AuraException, RemoteException {
        return mdb.getListener(selectRandomListenerKey(mdb));
    }

    Artist lookupByNameOrKey(MusicDatabase mdb, String nameOrKey) throws AuraException, RemoteException {
        if (isKey(nameOrKey)) {
            return mdb.artistLookup(nameOrKey);
        } else {
            List<Scored<Artist>> results = mdb.artistSearch(nameOrKey, 1);
            if (results.size() != 1) {
                throw new AuraException("Can't find artist " + nameOrKey);
            }
            return results.get(0).getItem();
        }
    }

    ArtistTag lookupArtistTag(MusicDatabase mdb, String name) throws AuraException, RemoteException {
        List<Scored<ArtistTag>> results = mdb.artistTagSearch(name, 1);
        if (results.size() != 1) {
            throw new AuraException("Can't find artist tag" + name);
        }
        return results.get(0).getItem();
    }

    boolean isKey(String nameOrKey) {
        String[] fields = nameOrKey.split("-");
        return fields.length == 5 && fields[0].length() == 8 && fields[4].length() == 12;
    }

    List<String> getArtistIDs(MusicDatabase mdb) throws AuraException, RemoteException {
        synchronized (lock) {
            if (artistIDs == null) {
                artistIDs = mdb.getAllItemKeys(ItemType.ARTIST);
            }
        }
        return artistIDs;
    }

    List<String> getListenerIDs(MusicDatabase mdb) throws AuraException, RemoteException {
        synchronized (lock) {
            if (listenerIDs == null) {
                listenerIDs = mdb.getAllItemKeys(ItemType.USER);
            }
        }
        return listenerIDs;
    }

    List<String> getArtistTagIDs(MusicDatabase mdb) throws AuraException, RemoteException {
        synchronized (lock) {
            if (artistTagIDs == null) {
                artistTagIDs = mdb.getAllItemKeys(ItemType.ARTIST_TAG);
            }
        }
        return artistTagIDs;
    }

    boolean hasTag(List<Scored<ArtistTag>> tags, String tag) {
        for (Scored<ArtistTag> at : tags) {
            if (at.getItem().getName().equalsIgnoreCase(tag)) {
                return true;
            }
        }
        return false;
    }

    boolean hasArtist(List<Scored<Artist>> artists, String artist) {
        for (Scored<Artist> a : artists) {
            if (a.getItem().getName().equalsIgnoreCase(artist)) {
                return true;
            }
        }
        return false;
    }

    void dump(MusicDatabase mdb, WorkbenchResult result, List<Scored<Artist>> sartists) throws AuraException, RemoteException {
        result.output(String.format("%5s %5s %s", "Score", "Pop", "Name"));
        for (Scored<Artist> sartist : sartists) {
            result.output(String.format("%5.3f %5.3f %s",
                    sartist.getScore(),
                    mdb.artistGetNormalizedPopularity(sartist.getItem()),
                    sartist.getItem().getName()));
        }
    }

    void dumpArtistTags(MusicDatabase mdb, WorkbenchResult result, List<Scored<ArtistTag>> sartistTags) throws AuraException, RemoteException {
        result.output(String.format("%5s %5s %s", "Score", "Pop", "Name"));
        for (Scored<ArtistTag> sartistTag : sartistTags) {
            result.output(String.format("%5.3f %5.3f %s",
                    sartistTag.getScore(),
                    mdb.artistTagGetNormalizedPopularity(sartistTag.getItem()),
                    sartistTag.getItem().getName()));
        }
    }
}
