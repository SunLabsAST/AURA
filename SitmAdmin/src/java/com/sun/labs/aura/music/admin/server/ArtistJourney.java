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
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.admin.client.WorkbenchResult;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author plamere
 */
public class ArtistJourney extends Worker {

    ArtistJourney() {
        super("ArtistJourney", "Find shortest similarity path between two artists");
        param("starting artist", "The artist where the journey starts", "");
        param("ending artist", "The artist where the journey ends", "");
        param("similarity depth", "The depth of the similarity", 10);
        param("Popularity",  "The desired popularity of the results", MusicDatabase.Popularity.values(), MusicDatabase.Popularity.ALL);
    }

    @Override
    void go(MusicDatabase mdb, Map<String, String> params, WorkbenchResult result) throws AuraException, RemoteException {
        Artist startingArtist = lookupByNameOrKey(mdb, getParam(params, "starting artist"));

        if (startingArtist == null) {
            result.fail("Can't find starting artist " + getParam(params, "starting artist"));
        }

        Artist endingArtist = lookupByNameOrKey(mdb, getParam(params, "ending artist"));

        if (endingArtist == null) {
            result.fail("Can't find ending artist " + getParam(params, "ending artist"));
        }

        int depth = getParamAsInt(params, "similarity depth");
        MusicDatabase.Popularity pop = (MusicDatabase.Popularity) getParamAsEnum(params, "Popularity");

        int comparisons = 0;

        List<Node> queue = new ArrayList<Node>();
        Set<String> visitedSet = new HashSet<String>();

        queue.add(new Node(startingArtist, null));
        visitedSet.add(startingArtist.getKey());

        Node finalNode = null;

        while (finalNode == null && queue.size() > 0) {
            Node curNode = queue.remove(0);
            System.out.println("FS " + curNode.artist.getName());
            List<Scored<Artist>> simArtists = mdb.artistFindSimilar(curNode.artist.getKey(), depth, pop);
            for (Scored<Artist> sartist : simArtists) {
                Artist artist = sartist.getItem();
                Node newNode = new Node(artist, curNode);
                comparisons++;
                if (artist.getKey().equals(endingArtist.getKey())) {
                    finalNode = newNode;
                    break;
                }
                if (!visitedSet.contains(artist.getKey())) {
                    visitedSet.add(artist.getKey());
                    queue.add(newNode);
                }
            }
        }

        if (finalNode != null) {
            List<Artist> resultList = new ArrayList();
            Node node = finalNode;

            while (node != null) {
                resultList.add(node.artist);
                node = node.prev;
            }

            Collections.reverse(resultList);

            int i = 0;
            for (Artist artist : resultList) {
                result.output(++i + " " + artist.getName());
            }
        } else {
            result.output("Couldn't find a path from " + startingArtist.getName() + " to " + endingArtist.getName());
        }
        result.output("Total comparisons: " + comparisons) ;
    }


    class Node {
        Artist artist;
        Node prev;
        Node(Artist artist, Node prev)  {
            this.artist = artist;
            this.prev = prev;
        }
    }
}
