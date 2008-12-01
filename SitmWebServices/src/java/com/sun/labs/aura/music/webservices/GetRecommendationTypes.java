/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.RecommendationType;
import com.sun.labs.aura.util.AuraException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author plamere
 */
public class GetRecommendationTypes extends StandardService {

    @Override
    public void initParams() {
    }

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void go(HttpServletRequest request, PrintWriter out, MusicDatabase mdb)
            throws AuraException, ParameterException, RemoteException {
        List<RecommendationType> rtypes = mdb.getArtistRecommendationTypes();
        Util.tag(out, "default", MusicDatabase.DEFAULT_RECOMMENDER);
        for (RecommendationType rtype : rtypes) {
            out.println("    <RecommendationType>");
            out.println("        <name>" + rtype.getName() + "</name>");
            out.println("        <description>" + rtype.getDescription() + "</description>");
            out.println("        <type>" + rtype.getType().name() + "</type>");
            out.println("    </RecommendationType>");
        }
    }
    /** 
     * Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo() {
        return "Gets the list of supported recommendation types";
    }
}
