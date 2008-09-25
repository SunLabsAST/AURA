/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.Listener;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.web.apml.APML;
import com.sun.labs.aura.music.web.apml.Concept;
import com.sun.labs.aura.music.web.apml.Profile;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Tag;
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
public class GetApml extends HttpServlet {

    private final static String SERVLET_NAME = "GetApml";

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
                int maxCount = 100;
                String maxCountString = request.getParameter("max");
                if (maxCountString != null) {
                    maxCount = Integer.parseInt(maxCountString);
                }

                String format = request.getParameter("format");
                boolean showArtistNames =  false;
                if (format != null) {
                    if (format.equals("artist")) {
                        showArtistNames = true;
                    } else if (format.equals("mbaid")) {
                        showArtistNames = false;
                    } else {
                        Util.outputStatus(out, SERVLET_NAME, Util.ErrorCode.BadArgument, "format must be 'artist' or 'mbaid'");
                        return;
                    }
                }

                String userID = request.getParameter("userID");
                if (userID != null) {
                    try {
                        Listener listener = mdb.getListener(userID);
                        Concept[] explicitConcepts = getArtistNameConcepts(mdb, showArtistNames, 
                                listener.getFavoriteArtist(), maxCount);
                        Concept[] implicitConcepts = getConcepts(listener.getSocialTags(), maxCount);
                        Profile profile = new Profile("music", implicitConcepts, explicitConcepts);
                        APML apml = new APML("taste data for user " + listener.getKey());
                        apml.addProfile(profile);
                        out.println(apml.toString());
                    } catch (AuraException ex) {
                        Util.outputStatus(out, SERVLET_NAME, Util.ErrorCode.InternalError, "Problem accessing data " + ex);
                    }
                } else {
                    Util.outputStatus(out, SERVLET_NAME, Util.ErrorCode.MissingArgument, "Missing userID");
                }
            }
        } finally {
            timer.report(out);
            out.close();
        }
    }
    
    private Concept[] getConcepts(List<Tag> tags, int maxCount) {
        int size = tags.size() > maxCount ? maxCount : tags.size();
        Concept[] concepts = new Concept[size];

        float max = getMaxFreq(tags);

        for (int i = 0; i < size; i++) {
            Tag tag = tags.get(i);
            concepts[i] = new Concept(tag.getName(), tag.getFreq() / max);
        }
        return concepts;
    }

    private Concept[] getArtistNameConcepts(MusicDatabase mdb, boolean useArtistName, 
                List<Tag> tags, int maxCount) throws AuraException {

        int size = tags.size() > maxCount ? maxCount : tags.size();
        Concept[] concepts = new Concept[size];

        float max = getMaxFreq(tags);

        for (int i = 0; i < size; i++) {
            Tag tag = tags.get(i);
            Artist artist = mdb.artistLookup(tag.getName());
            if (artist != null) {
                String name = useArtistName ? artist.getName() : tag.getName();
                String annotation = "mbaid for " + artist.getName() + " is " + tag.getName();
                concepts[i] = new Concept(name, tag.getFreq() / max, annotation);
            }
        }
        return concepts;
    }

    private float getMaxFreq(List<Tag> tags) {
        float max = -Float.MAX_VALUE;

        for (Tag tag : tags) {
            if (tag.getFreq() > max) {
                max = tag.getFreq();
            }
        }
        return max;
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
