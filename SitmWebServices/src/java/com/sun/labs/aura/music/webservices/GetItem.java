/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.util.AuraException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author plamere
 */
public class GetItem extends HttpServlet {

    private final static String SERVLET_NAME = "GetItem";

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
                boolean compact = false;
                String itemID = request.getParameter("key");
                String format = request.getParameter("format");
                if (format != null) {
                    compact = format.equalsIgnoreCase("compact");
                }
                if (itemID != null) {
                    String[] keys = itemID.split(",");
                    try {
                        Util.tagOpen(out, SERVLET_NAME);
                        for (String key : keys) {
                            key = key.trim();
                            long fetchStart = System.currentTimeMillis();
                            Item item = mdb.getDataStore().getItem(key);
                            long delta = System.currentTimeMillis() - fetchStart;
                            if (item != null) {
                                if (compact) {
                                    out.println(toCompactXML(mdb, item));
                                } else {
                                    out.println(Util.toXML(item));
                                }
                                out.println("<!-- item fetch in " + delta + " ms -->");
                            } else {
                                out.println("<item key=\"" + key + "\" status=\"NotFound\"/>");
                            }
                        }
                        Util.outputOKStatus(out);
                        Util.tagClose(out, SERVLET_NAME);
                    } catch (AuraException ex) {
                        Util.outputStatus(out, SERVLET_NAME, Util.ErrorCode.InternalError, "Problem accessing data " + ex);
                    }
                } else {
                    Util.outputStatus(out, SERVLET_NAME, Util.ErrorCode.MissingArgument, "Missing itemID");
                }
            }
        } finally {
            timer.report(out);
            out.close();
        }
    }

    private String toCompactXML(MusicDatabase mdb, Item item) throws AuraException {
        if (item.getType() == ItemType.ARTIST) {
            Artist artist = new Artist(item);
            StringBuilder sb = new StringBuilder();
            // TBD finish this
            sb.append(" <item key=\"" + artist.getKey() + "\">");
            sb.append("<name>" + artist.getName() + "</name>");
            sb.append("<popularity>" + mdb.artistGetNormalizedPopularity(artist) + "</popularity>");
            {
                String photo = selectFromSet(artist.getPhotos());
                if (photo != null) {
                    sb.append("<image>" + photo + "</image>");
                }
            }
            {
                String audio = selectFromSet(artist.getAudio());
                if (audio != null) {
                    sb.append("<audio>" + audio + "</audio>");
                }
            }
            {
                String spotify = artist.getSpotifyID();
                if (spotify != null) {
                    sb.append("<spotify>" + spotify + "</spotify>");
                }
            }
            sb.append("</item>");
            return sb.toString();
        } else {
            return Util.toXML(item);
        }
    }

    private String selectFromSet(Set<String> set) {
        if (set.size() > 0) {
            return (String) set.toArray()[0];
        }
        return null;
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
