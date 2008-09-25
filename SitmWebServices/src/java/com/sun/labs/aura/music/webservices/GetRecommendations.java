/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.Listener;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.Recommendation;
import com.sun.labs.aura.music.RecommendationSummary;
import com.sun.labs.aura.music.RecommendationType;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author plamere
 */
public class GetRecommendations extends HttpServlet {

    private final static String SERVLET_NAME = "GetRecommendations";

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ServletContext context = getServletContext();

        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Timer timer = Util.getTimer();

        try {
            MusicDatabase mdb = (MusicDatabase) context.getAttribute("MusicDatabase");

            if (mdb == null) {
                Util.outputStatus(out, SERVLET_NAME, Util.ErrorCode.InternalError, "Can't connect to the music database");
            } else {
                String userID = request.getParameter("userID");

                int maxCount = 10;
                String maxCountString = request.getParameter("max");
                if (maxCountString != null) {
                    maxCount = Integer.parseInt(maxCountString);
                }

                String alg = request.getParameter("alg");
                if (alg == null) {
                    alg = MusicDatabase.DEFAULT_RECOMMENDER;
                }

                if (userID != null) {
                    try {
                        Listener listener = mdb.getListener(userID);
                        if (listener != null) {
                            RecommendationType rtype = mdb.getArtistRecommendationType(alg);
                            if (rtype != null) {
                                RecommendationSummary rs = rtype.getRecommendations(listener.getKey(), maxCount, null);

                                out.println("<GetRecommendations userID=\"" + userID + "\" alg=\"" + rtype.getName() + "\">");
                                out.println("    <explanation>");
                                out.println("        " + Util.filter(rs.getExplanation()));
                                out.println("    </explanation>");
                                out.println("    <recommendations>");
                                for (Recommendation r : rs.getRecommendations()) {
                                    Artist simArtist = mdb.artistLookup(r.getId());
                                    out.printf("         <recommendation artist=\"%s\" score=\"%.3f\" name=\"%s\">\n",
                                        simArtist.getKey(), r.getScore(),  Util.filter(simArtist.getName()));
                                    for (Scored<String> ss : r.getExplanation()) {
                                        out.printf("            <reason tag=\"%s\" score=\"%.3f\"/>\n", 
                                                Util.filter(ss.getItem()), ss.getScore());
                                    }
                                    out.printf("         </recommendation>\n");
                                }
                                out.println("    </recommendations>");
                                out.println("</GetRecommendations>");
                            } else {
                                Util.outputStatus(out, SERVLET_NAME, Util.ErrorCode.NotFound,
                                        "can't find specified recommendation algorithm");
                            }
                        } else {
                            Util.outputStatus(out, SERVLET_NAME, Util.ErrorCode.NotFound,
                                    "can't find userID ");
                        }
                    } catch (AuraException ex) {
                        Util.outputStatus(out, SERVLET_NAME, Util.ErrorCode.InternalError, "Problem accessing data");
                    }
                } else {
                    Util.outputStatus(out, SERVLET_NAME, Util.ErrorCode.NotFound,
                            "must specify a userID ");
                }
            }
        } finally {
            timer.report(out);
            out.close();
        }
    }
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
