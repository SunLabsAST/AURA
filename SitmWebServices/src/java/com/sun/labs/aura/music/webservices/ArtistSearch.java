/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import java.io.*;

import java.util.List;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author plamere
 */
public class ArtistSearch extends HttpServlet {
    private final static String SERVLET_NAME = "ArtistSearch";

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Timer timer = Util.getTimer();

        ServletContext context = getServletContext();
        MusicDatabase mdb = (MusicDatabase) context.getAttribute("MusicDatabase");

        try {
            String name = request.getParameter("name");
            int maxCount = 10;
            String maxCountString = request.getParameter("max");
            if (maxCountString != null) {
                maxCount = Integer.parseInt(maxCountString);
            }


            if (mdb == null) {
                Util.outputStatus(out, SERVLET_NAME, Util.ErrorCode.InternalError, "Can't find the datastore");
            } else {
                Util.tagOpen(out, SERVLET_NAME);
                if (name != null) {
                    try {
                        List<Scored<Artist>> scoredArtists = mdb.artistSearch(name, maxCount);
                        for (Scored<Artist> scoredArtist : scoredArtists) {
                            Artist artist = scoredArtist.getItem();
                            out.println("    <artist key=\"" + artist.getKey() + "\" " + "score=\"" + scoredArtist.getScore() + "\" " + "popularity=\"" + artist.getPopularity() + "\" " + "name=\"" + Util.filter(artist.getName()) + "\"" + "/>");
                        }
                        Util.outputOKStatus(out);
                    } catch (AuraException ex) {
                        Util.outputStatus(out, Util.ErrorCode.InternalError, "problem accessing data, " + ex.getMessage());
                    }
                } else {
                    Util.outputStatus(out, Util.ErrorCode.MissingArgument, "artist name");
                }
                Util.tagClose(out, SERVLET_NAME);
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
    }
// </editor-fold>
}
