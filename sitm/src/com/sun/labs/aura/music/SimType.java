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
