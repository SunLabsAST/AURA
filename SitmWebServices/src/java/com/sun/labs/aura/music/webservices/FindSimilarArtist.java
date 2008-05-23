/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.recommender.TypeFilter;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import java.io.*;

import java.rmi.RemoteException;
import java.util.List;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author plamere
 */
public class FindSimilarArtist extends HttpServlet {

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ServletContext context = getServletContext();
        DataStore dataStore = (DataStore) context.getAttribute("dataStore");

        if (dataStore == null)  {
            Shared.forwardToError(context, request, response, "Can't find the datastore");
        }

        String key = request.getParameter("key");

        int maxCount = 10;
        String maxCountString = request.getParameter("max");
        if (maxCountString != null) {
            maxCount = Integer.parseInt(maxCountString);
        }

        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();

        Item item = null;
        try {
            if (key == null) {
                String name = request.getParameter("name");
                if (name != null) {
                    Artist artist = findArtist(dataStore, name);
                    if (artist != null) {
                        key = artist.getKey();
                    }
                }
            }
            if (key != null && ((item = dataStore.getItem(key)) != null)) {
                List<Scored<Item>> scoredItems = dataStore.findSimilar(key, 
                        Artist.FIELD_SOCIAL_TAGS, maxCount, new TypeFilter(ItemType.ARTIST));

                out.println("<FindSimilarArtist key=\"" + key + "\" name=\"" + filter(item.getName()) + "\">");
                for (Scored<Item> si : scoredItems) {

                    if (si.getItem().getKey().equals(key)) {
                        continue;
                    }

                    Artist simArtist = new Artist(si.getItem());
                    out.println("    <artist key=\"" +
                            simArtist.getKey() + "\" " +
                            "score=\"" + si.getScore() + "\" " +
                            "name=\"" + filter(simArtist.getName()) + "\"" +
                            "/>");
                }
                out.println("</FindSimilarArtist>");
                out.close();
            } else {
                Shared.forwardToError(context, request, response, "need an artist key or an artist name");
            }
        } catch (AuraException ex) {
            Shared.forwardToError(context, request, response, ex);
        }
    }

    static String filter(String s) {
        if (s != null) {
            s = s.replaceAll("[^\\p{ASCII}]", "");
            s = s.replaceAll("\\&", "&amp;");
            s = s.replaceAll("\\<", "&lt;");
            s = s.replaceAll("\\>", "&gt;");
            s = s.replaceAll("[^\\p{Graph}\\p{Blank}]", "");
        }
        return s;
    }

    private Artist findArtist(DataStore dataStore, String qname) throws AuraException, RemoteException {
        String query = "(aura-type = artist) <AND> (aura-name <matches> \"*" + qname + "*\")";
        List<Scored<Item>> items = dataStore.query(query, "-score", 1, null);
        if (items.size() > 0) {
            return new Artist(items.get(0).getItem());
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
    }
    // </editor-fold>
}
