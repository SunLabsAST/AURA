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

package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.MusicDatabase.Popularity;
import com.sun.labs.aura.music.webservices.ItemFormatter.OutputType;
import com.sun.labs.aura.music.webservices.Util.ErrorCode;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import java.io.*;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author plamere
 */
public class FindSimilarArtists extends StandardService {

    @Override
    public void initParams() {
        addParam("key", null, "the key of the item of interest");
        addParam("name", null, "the name of the item of interest");
        addParam("max", "10", "the maxiumum number of artists to return");
        addParam("popularity", Popularity.ALL.name(), "the popularity filter");
        addParam("field", Artist.FIELD_SOCIAL_TAGS, "the field to use for similarity");
        addParam("outputType", OutputType.Tiny.name(), "the type of output");
    }

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void go(HttpServletRequest request, PrintWriter out, MusicDatabase mdb)
            throws AuraException, ParameterException, RemoteException {

        Set<Artist> artists = getArtistsFromRequest(mdb, request);
        List<String> keys = getKeyList(artists);
        int maxCount = getParamAsInt(request, "max", 1, 250);
        Popularity pop = (Popularity) getParamAsEnum(request, "popularity", Popularity.values());
        String field = getParam(request, "field");
        OutputType outputType = (OutputType) getParamAsEnum(request, "outputType", OutputType.values());
        ItemFormatterManager formatter = getItemFormatterManager();
        List<Scored<Artist>> scoredArtists;

        if ("all".equals(field)) {
            scoredArtists = mdb.artistFindSimilar(keys, maxCount + keys.size(), pop);
        } else {
            scoredArtists = mdb.artistFindSimilar(keys, field, maxCount + keys.size(), pop);
        }

        for (Artist artist : artists) {
            out.println("    <seed key=\"" + artist.getKey() + "\" name=\"" + Util.filter(artist.getName()) + "\"/>");
        }

        for (Scored<Artist> scoredArtist : scoredArtists) {
            if (keys.contains(scoredArtist.getItem().getKey())) {
                continue;
            }
            Artist simArtist = scoredArtist.getItem();
            out.println(formatter.toXML(simArtist.getItem(), outputType, scoredArtist.getScore()));
        }
    }

    Set<Artist> getArtistsFromRequest(MusicDatabase mdb, HttpServletRequest request) throws AuraException, ParameterException {
        Set<Artist> artists = new HashSet<Artist>();
        StringBuilder errorMessage = new StringBuilder();

        String[] pkeys = request.getParameterValues("key");

        if (pkeys != null) {
            for (String key : pkeys) {
                Artist artist = mdb.artistLookup(key);
                if (artist != null) {
                    artists.add(artist);
                } else {
                    addMessage(errorMessage, "bad key: " + key);
                }
            }
        }
        String[] names = request.getParameterValues("name");
        if (names != null) {
            for (String name : names) {
                Artist artist = mdb.artistFindBestMatch(name);
                if (artist != null) {
                    artists.add(artist);
                } else {
                    addMessage(errorMessage, "bad name: " + name);
                }
            }
        }

        if (errorMessage.length() > 0) {
            throw new ParameterException(ErrorCode.BadArgument, errorMessage.toString());
        }

        if (artists.size() == 0) {
            throw new ParameterException(ErrorCode.MissingArgument, "Need at least one key or name");
        }
        return artists;
    }

    List<String> getKeyList(Collection<Artist> artists) {
        List<String> keys = new ArrayList<String>();
        for (Artist artist : artists) {
            keys.add(artist.getKey());
        }
        return keys;
    }

    private void addMessage(StringBuilder sb, String msg) {
        if (sb.length() > 0) {
            sb.append(", ");
        }
        sb.append(msg);
    }

    /** 
     * Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo() {
        return "Finds artists that are similar to a seed artist";
    }
    // 
}
