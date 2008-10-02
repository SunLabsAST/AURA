/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */

package com.sun.labs.aura.music;

import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import java.util.List;

/**
 * Represents a type of similarity
 * @author plamere
 */
public interface SimType {
    /**
     * Gets a descriptio of this SimType
     * @return
     */
    public String getDescription();
    /**
     * Gets the name of the SimType
     * @return
     */
    public String getName();

    /**
     * Finds similar artists
     * @param artistID the seed artist
     * @param count the maximum number of artists to return
     * @return a list of scored artists.
     * @throws com.sun.labs.aura.util.AuraException if an error occurs while talking to the datastore
     */
    public List<Scored<Artist>> findSimilarArtists(String artistID, int count) throws AuraException;

    /**
     * Finds similar artists
     * @param artistID the seed artist
     * @param count the maximum number of artists to return
     * @param count popularity the desired popularity range of the results
     * @return a list of scored artists.
     * @throws com.sun.labs.aura.util.AuraException if an error occurs while talking to the datastore
     */
    public List<Scored<Artist>> findSimilarArtists(String artistID, 
            int count, MusicDatabase.Popularity popularity) throws AuraException;

    /**
     * Explains the similarity between two artists
     * @param artistID1 the id of the first artist
     * @param artistID2 the id of th second artist
     * @param count the maximum number of terms to return in the explanation
     * @return a list of scored terms that show the overlap
     * @throws com.sun.labs.aura.util.AuraException if an error occurs while talking to the datastore
     */
    public List<Scored<String>> explainSimilarity(String artistID1, String artistID2, int count) throws AuraException;
}
