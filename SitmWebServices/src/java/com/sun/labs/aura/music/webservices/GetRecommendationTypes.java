/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.RecommendationType;
import com.sun.labs.aura.music.webservices.Util.ErrorCode;
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
public class GetRecommendationTypes extends HttpServlet {

    private final static String SERVLET_NAME = "GetRecommendationTypes";
    private ParameterChecker pc = null;

    public void init() throws ServletException {
        super.init();
        pc = new ParameterChecker(SERVLET_NAME, "get the supported recomendation types from the database");
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

            List<RecommendationType> rtypes = mdb.getArtistRecommendationTypes();
            Util.tag(out, "default", MusicDatabase.DEFAULT_RECOMMENDER);
            for (RecommendationType rtype : rtypes) {
                out.println("    <RecommendationType>");
                out.println("        <name>" + rtype.getName() + "</name>");
                out.println("        <description>" + rtype.getDescription() + "</description>");
                out.println("        <type>" + rtype.getType().name() + "</type>");
                out.println("    </RecommendationType>");
            }
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
        return "Gets the list of supported recommendation types";
    }// </editor-fold>
}
