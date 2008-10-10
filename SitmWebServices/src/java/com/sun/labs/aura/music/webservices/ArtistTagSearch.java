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
public class ArtistTagSearch extends HttpServlet {

    private final static String SERVLET_NAME = "ArtistTagSearch";
    private ParameterChecker pc = null;

    @Override
    public void init() throws ServletException {
        super.init();
        pc = new ParameterChecker(SERVLET_NAME, "searches the database for an artist tag");
        pc.addParam("name", "the name of the artist tag to search for");
        pc.addParam("max", "10", "the maximum number of matches to return");
    }

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (pc.processDocumentationRequest(request, response)) {
            return;
        }

        Status status = new Status(request);
        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();

        ServletContext context = getServletContext();
        MusicDatabase mdb = (MusicDatabase) context.getAttribute("MusicDatabase");

        try {
            Util.tagOpen(out, SERVLET_NAME);
            pc.check(status, request);

            String name = pc.getParam(status, request, "name");
            int maxCount = pc.getParamAsInt(status, request, "max", 1, 250);


            if (mdb == null) {
                status.addError(ErrorCode.InternalError, "Can't find the datastore");
                return;
            }
            List<Scored<ArtistTag>> scoredArtistTags = mdb.artistTagSearch(name, maxCount);
            for (Scored<ArtistTag> scoredArtistTag : scoredArtistTags) {
                ArtistTag artistTag = scoredArtistTag.getItem();
                out.println("    <artistTag key=\"" + artistTag.getKey() + "\" " 
                        + "score=\"" + scoredArtistTag.getScore() + "\" " 
                        + "popularity=\"" + mdb.artistTagGetNormalizedPopularity(artistTag) + "\" "
                        + "name=\"" + Util.filter(artistTag.getName()) + "\"" + "/>");
            }
        } catch (AuraException ex) {
            status.addError(ErrorCode.InternalError, "problem accessing data, " + ex.getMessage());
        } catch (ParameterException ex) {
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
        return "Searches the database for an artist tag with a particular name ";
    }// </editor-fold>
}
