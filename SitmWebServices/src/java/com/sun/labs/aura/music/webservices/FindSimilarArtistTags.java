/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.music.ArtistTag;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.webservices.Util.ErrorCode;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author plamere
 */
public class FindSimilarArtistTags extends HttpServlet {

    private final static String SERVLET_NAME = "FindSimilarArtistTags";
    private ParameterChecker pc;

    @Override
    public void init() throws ServletException {
        super.init();
        pc = new ParameterChecker();
        pc.addParam("key", null, "the key of the item of interest");
        pc.addParam("name", null, "the name of the item of interest");
        pc.addParam("max", "10", "the maxiumum number of artists to return");
    }

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Status status = new Status();
        ServletContext context = getServletContext();

        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            Util.tagOpen(out, SERVLET_NAME);
            pc.check(status, request);
            MusicDatabase mdb = (MusicDatabase) context.getAttribute("MusicDatabase");

            if (mdb == null) {
                status.addError(ErrorCode.InternalError, "Can't connect to the music database");
                return;
            }
            String key = pc.getParam(status, request, "key");
            int maxCount = pc.getParamAsInt(status, request, "max", 1, 250);

            try {
                ArtistTag artistTag = null;
                if (key == null) {
                    String name = pc.getParam(status, request, "name");
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

                        out.println("<FindSimilarArtistTags key=\"" + key + "\" name=\"" + Util.filter(artistTag.getName()) + "\">");
                        for (Scored<ArtistTag> scoredArtistTag : scoredArtistTags) {

                            if (scoredArtistTag.getItem().getKey().equals(key)) {
                                continue;
                            }

                            ArtistTag simArtistTag = scoredArtistTag.getItem();
                            out.println("    <artistTag key=\"" +
                                    simArtistTag.getKey() + "\" " +
                                    "score=\"" + scoredArtistTag.getScore() + "\" " +
                                    "name=\"" + Util.filter(simArtistTag.getName()) + "\"" +
                                    "/>");
                        }
                    } else {
                        status.addError(ErrorCode.NotFound, "can't find specified artist");
                    }
                } else {
                    status.addError(ErrorCode.MissingArgument, "need a name or a key");
                }
            } catch (AuraException ex) {
                status.addError(ErrorCode.InternalError, "Problem accessing data");
            }
        } catch (ParameterException e) {
        } finally {
            status.toXML(out);
            Util.tagClose(out, SERVLET_NAME);
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
