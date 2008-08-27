/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */

package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.util.AuraException;
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
public class GetArtists extends HttpServlet {

    private final static String SERVLET_NAME = "GetArtists";

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
                Util.outputStatus(out, SERVLET_NAME, Util.ErrorCode.DataStore, "Can't connect to the music database");
            } else {
                int maxCount = 100;
                String maxCountString = request.getParameter("max");
                if (maxCountString != null) {
                    maxCount = Integer.parseInt(maxCountString);
                }

                List<Artist> artists = mdb.artistGetMostPopular(maxCount);

                Util.tagOpen(out, SERVLET_NAME);
                for (Artist artist : artists) {
                    out.println("    <artist key=\"" + artist.getKey() + "\" name=\"" 
                            + Util.filter(artist.getName()) + "\"" 
                            + " popularity=\"" + mdb.artistGetNormalizedPopularity(artist) + "\"" + "/>");
                }

                Util.outputOKStatus(out);
                Util.tagClose(out, SERVLET_NAME);
            }
        } catch (AuraException ex) {
            Util.outputStatus(out, SERVLET_NAME, Util.ErrorCode.DataStore, "Problem accessing data");
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
