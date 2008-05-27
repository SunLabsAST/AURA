/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.aardvark.dashboard.web;

import com.sun.labs.aura.aardvark.BlogEntry;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.ScoredComparator;
import java.io.*;

import java.util.Collections;
import java.util.List;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author plamere
 */
public class GetDocumentTags extends HttpServlet {

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ServletContext context = getServletContext();
        DataStore dataStore = (DataStore) context.getAttribute("dataStore");


        String key = request.getParameter("key");

        int maxCount = 8;
        String maxCountString = request.getParameter("max");
        if (maxCountString != null) {
            maxCount = Integer.parseInt(maxCountString);
        }

        Item item = null;
        try {
            if (key != null && ((item = dataStore.getItem(key)) != null)) {
                BlogEntry entry = new BlogEntry(item);
                List<Scored<String>> autotags = entry.getAutoTags();
                Collections.sort(autotags, ScoredComparator.COMPARATOR);
                Collections.reverse(autotags);
                if (autotags.size() > maxCount) {
                    autotags = autotags.subList(0, maxCount);
                }

                response.setContentType("text/xml;charset=UTF-8");
                PrintWriter out = response.getWriter();
                StoryUtil.dumpTagInfo(out, dataStore, autotags, key);
                out.close();
            } else {
                Shared.forwardToError(context, request, response, "missing key");
            }
        } catch (AuraException ex) {
            Shared.forwardToError(context, request, response, ex);
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
