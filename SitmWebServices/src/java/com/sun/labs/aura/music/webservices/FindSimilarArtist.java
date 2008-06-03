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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author plamere
 */
public class FindSimilarArtist extends HttpServlet {
    private final static String SERVLET_NAME = "FindSimilarArtist";

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
                String key = request.getParameter("key");

                int maxCount = 10;
                String maxCountString = request.getParameter("max");
                if (maxCountString != null) {
                    maxCount = Integer.parseInt(maxCountString);
                }

                try {
                    Artist artist = null;
                    if (key == null) {
                        String name = request.getParameter("name");
                        if (name != null) {
                            artist = mdb.artistFindBestMatch(name);
                            if (artist != null) {
                                key = artist.getKey();
                            }
                        }
                    }
                    if (key != null && ((artist = mdb.artistLookup(key)) != null)) {
                        List<Scored<Artist>> scoredArtists = mdb.artistFindSimilar(key, maxCount);

                        out.println("<FindSimilarArtist key=\"" + key + "\" name=\"" + Util.filter(artist.getName()) + "\">");
                        for (Scored<Artist> scoredArtist : scoredArtists) {

                            if (scoredArtist.getItem().getKey().equals(key)) {
                                continue;
                            }

                            Artist simArtist = scoredArtist.getItem();
                            out.println("    <artist key=\"" +
                                    simArtist.getKey() + "\" " +
                                    "score=\"" + scoredArtist.getScore() + "\" " +
                                    "name=\"" + Util.filter(simArtist.getName()) + "\"" +
                                    "/>");
                        }
                        Util.outputOKStatus(out);
                        Util.tagClose(out, SERVLET_NAME);
                    } else {
                        Util.outputStatus(out, SERVLET_NAME, Util.ErrorCode.MissingArgument, "need a name or a key");
                    }
                } catch (AuraException ex) {
                    Util.outputStatus(out, SERVLET_NAME, Util.ErrorCode.DataStore, "Problem accessing data");
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
    }
    // </editor-fold>
}
