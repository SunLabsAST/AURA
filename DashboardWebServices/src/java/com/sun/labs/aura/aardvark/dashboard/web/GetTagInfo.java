/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.aardvark.dashboard.web;

import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import java.io.*;
import java.net.*;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author plamere
 */
public class GetTagInfo extends HttpServlet {

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ServletContext context = getServletContext();
        DataStore dataStore = (DataStore) context.getAttribute("dataStore");


        String tag = request.getParameter("tag");
        if (tag != null) {
            String[] tags = tag.split(",");
            List<Scored<String>> tagList = new ArrayList<Scored<String>>();

            for (String t : tags) {
                tagList.add(new Scored<String>(t, 1f));
            }

            try {

                response.setContentType("text/xml;charset=UTF-8");
                PrintWriter out = response.getWriter();

                try {
                    StoryUtil.dumpTagInfo(out, dataStore, tagList, null);
                } finally {
                    out.close();
                }
            } catch (AuraException ex) {
                Shared.forwardToError(context, request, response, ex);
            }
        } else {
            Shared.forwardToError(context, request, response, "missing tag parameter");
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