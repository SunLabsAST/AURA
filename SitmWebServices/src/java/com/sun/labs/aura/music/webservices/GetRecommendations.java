/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
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
