/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.WordCloud;
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
public class FindSimilarArtistFromWordCloud extends HttpServlet {

    private final static String SERVLET_NAME = "FindSimilarArtistFromWordCloud";

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
                String wc = request.getParameter("wordCloud");

                int maxCount = 10;
                String maxCountString = request.getParameter("max");
                if (maxCountString != null) {
                    maxCount = Integer.parseInt(maxCountString);
                }

                if (wc == null) {
                    Util.outputStatus(out, SERVLET_NAME, Util.ErrorCode.MissingArgument, "Missing paramenter wordCloud");
                    return;
                }

                WordCloud cloud = WordCloud.convertStringToWordCloud(wc);
                if (cloud == null) {
                    Util.outputStatus(out, SERVLET_NAME, Util.ErrorCode.BadArgument, "Bad wordcloud format. Should be:" +
                            "(tag1 name,weight)(tag2 name,weight)");
                    return;
                }

                List<Scored<Artist>> scoredArtists = mdb.wordCloudFindSimilarArtists(cloud, maxCount);
                out.println("<FindSimilarArtistFromWordCloud>");

                Util.tagClose(out, SERVLET_NAME);
                for (Scored<Artist> scoredArtist : scoredArtists) {
                    Artist simArtist = scoredArtist.getItem();
                    out.println("    <artist key=\"" +
                            simArtist.getKey() + "\" " +
                            "score=\"" + scoredArtist.getScore() + "\" " +
                            "name=\"" + Util.filter(simArtist.getName()) + "\"" +
                            "/>");
                }
                Util.outputOKStatus(out);
                Util.tagClose(out, SERVLET_NAME);
            }
        } catch (AuraException ex) {
            Util.outputStatus(out, SERVLET_NAME, Util.ErrorCode.DataStore, "Problem accessing data " + ex);
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
