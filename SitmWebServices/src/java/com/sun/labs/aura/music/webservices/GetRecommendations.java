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
import com.sun.labs.aura.music.MusicDatabase.Popularity;
import com.sun.labs.aura.music.Recommendation;
import com.sun.labs.aura.music.RecommendationSummary;
import com.sun.labs.aura.music.RecommendationType;
import com.sun.labs.aura.music.webservices.Util.ErrorCode;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author plamere
 */
public class GetRecommendations extends StandardService {

    @Override
    public void initParams() {
        addParam("userKey", "the key of the item of interest");
        addParam("max", "10", "the maxiumum number of artists to return");
        addParam("popularity", Popularity.ALL.name(), "the popularity filter");
        addParam("alg", null, "the recommendatin algorithm to use");
    }

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void go(HttpServletRequest request, PrintWriter out, MusicDatabase mdb)
            throws AuraException, ParameterException, RemoteException {
        String userID = getParam(request, "userKey");
        int maxCount = getParamAsInt(request, "max", 1, 250);
        Popularity pop = (Popularity) getParamAsEnum(request,
                "popularity", Popularity.values());
        String alg = getParam(request, "alg");
        if (alg == null) {
            alg = mdb.getDefaultArtistRecommendationType().getName();
        }

        Listener listener = mdb.getListener(userID);
        if (listener != null) {
            RecommendationType rtype = mdb.getArtistRecommendationType(alg);
            if (rtype != null) {
                RecommendationSummary rs = rtype.getRecommendations(listener.getKey(), maxCount, null);
                out.println("    <explanation>");
                out.println("        " + Util.filter(rs.getExplanation()));
                out.println("    </explanation>");
                out.println("    <recommendations>");
                for (Recommendation r : rs.getRecommendations()) {
                    Artist simArtist = mdb.artistLookup(r.getId());
                    out.printf("         <recommendation artist=\"%s\" score=\"%.3f\" name=\"%s\">\n",
                            simArtist.getKey(), r.getScore(), Util.filter(simArtist.getName()));
                    for (Scored<String> ss : r.getExplanation()) {
                        out.printf("            <reason tag=\"%s\" score=\"%.3f\"/>\n",
                                Util.filter(ss.getItem()), ss.getScore());
                    }
                    out.printf("         </recommendation>\n");
                }
                out.println("    </recommendations>");
            } else {
                throw new ParameterException(ErrorCode.NotFound, "can't find specified recommendation algorithm");
            }
        } else {
            throw new ParameterException(ErrorCode.NotFound, "can't find userID ");
        }
    }
    /** 
     * Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
