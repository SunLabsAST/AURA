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

package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.Listener;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.web.apml.APML;
import com.sun.labs.aura.music.web.apml.Concept;
import com.sun.labs.aura.music.web.apml.Profile;
import com.sun.labs.aura.music.webservices.Util.ErrorCode;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Tag;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author plamere
 */
public class GetApml extends StandardService {

    private enum Format {

        Artist, MBaid
    };

    @Override
    public void initParams() {
        addParam("userKey", "the key the user of interest");
        addParam("max", "10", "the maximum number of concepts returned");
        addParam("format", "artist", "the format of the output");
        setDocType(DocType.RawXML);
    }

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void go(HttpServletRequest request, PrintWriter out, MusicDatabase mdb)
            throws AuraException, ParameterException, RemoteException {
        String key = getParam(request, "userKey");
        int maxCount = getParamAsInt(request, "max", 1, 250);
        boolean showArtistNames = getParamAsEnum(request, "format", Format.values()) == Format.Artist;

        Listener listener = mdb.getListener(key);
        if (listener != null) {
            Concept[] explicitConcepts = getArtistNameConcepts(mdb, showArtistNames,
                    listener.getAggregatedPlayCount(), maxCount);
            Concept[] implicitConcepts = getConcepts(listener.getSocialTags(), maxCount);
            Profile profile = new Profile("music", implicitConcepts, explicitConcepts);
            APML apml = new APML("taste data for user " + listener.getKey());
            apml.addProfile(profile);
            out.println(apml.toString());
        } else {
            throw new ParameterException(ErrorCode.InvalidKey, "Can't find listener with key " + key);
        }
    }

    private Concept[] getConcepts(List<Tag> tags, int maxCount) {
        int size = tags.size() > maxCount ? maxCount : tags.size();
        Concept[] concepts = new Concept[size];

        float max = getMaxFreq(tags);

        for (int i = 0; i < size; i++) {
            Tag tag = tags.get(i);
            concepts[i] = new Concept(tag.getName(), tag.getFreq() / max);
        }
        return concepts;
    }

    private Concept[] getArtistNameConcepts(MusicDatabase mdb, boolean useArtistName,
            List<Tag> tags, int maxCount) throws AuraException {

        int size = tags.size() > maxCount ? maxCount : tags.size();
        Concept[] concepts = new Concept[size];

        float max = getMaxFreq(tags);

        for (int i = 0; i < size; i++) {
            Tag tag = tags.get(i);
            Artist artist = mdb.artistLookup(tag.getName());
            if (artist != null) {
                String name = useArtistName ? artist.getName() : tag.getName();
                String annotation = "mbaid for " + artist.getName() + " is " + tag.getName();
                concepts[i] = new Concept(name, tag.getFreq() / max, annotation);
            }
        }
        return concepts;
    }

    private float getMaxFreq(List<Tag> tags) {
        float max = -Float.MAX_VALUE;

        for (Tag tag : tags) {
            if (tag.getFreq() > max) {
                max = tag.getFreq();
            }
        }
        return max;
    }
    /** 
     * Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo() {
        return "Gets the APML markup for a listener";
    }
}
