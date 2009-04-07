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

import com.sun.labs.aura.music.ArtistTag;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.webservices.ItemFormatter.OutputType;
import com.sun.labs.aura.music.webservices.Util.ErrorCode;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author plamere
 */
public class FindSimilarArtistTags extends StandardService {

    @Override
    public void initParams() {
        addParam("key", null, "the key of the item of interest");
        addParam("name", null, "the name of the item of interest");
        addParam("max", "10", "the maxiumum number of artists to return");
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

        ItemFormatterManager formatter = getItemFormatterManager();
        String key = getParam(request, "key");
        int maxCount = getParamAsInt(request, "max", 1, 250);
        OutputType outputType = (OutputType) getParamAsEnum(request, "outputType", OutputType.values());

        ArtistTag artistTag = null;
        if (key == null) {
            String name = getParam(request, "name");
            if (name != null) {
                artistTag = mdb.artistTagFindBestMatch(name);
                if (artistTag != null) {
                    key = artistTag.getKey();
                }
            }
        }
        if (key != null) {
            if ((artistTag = mdb.artistTagLookup(key)) != null) {
                List<Scored<ArtistTag>> scoredArtistTags = mdb.artistTagFindSimilar(key, maxCount);

                out.println("   <seed key=\"" + key + "\" name=\"" + Util.filter(artistTag.getName()) + "\"/>");
                for (Scored<ArtistTag> scoredArtistTag : scoredArtistTags) {

                    if (scoredArtistTag.getItem().getKey().equals(key)) {
                        continue;
                    }

                    out.println(formatter.toXML(scoredArtistTag.getItem().getItem(), outputType, scoredArtistTag.getScore()));
                }
            } else {
                throw new ParameterException(ErrorCode.NotFound, "can't find specified artist");
            }
        } else {
            throw new ParameterException(ErrorCode.MissingArgument, "need a name or a key");
        }
    }

    /**
     * Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo() {
        return "Finds artist tags that are similar to a seed tag ";
    }
}
