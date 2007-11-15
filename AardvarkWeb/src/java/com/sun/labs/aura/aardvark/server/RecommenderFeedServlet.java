/*
 *  Copyright 2007 Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES. 
 */
package com.sun.labs.aura.aardvark.server;

import com.sun.labs.aura.aardvark.Aardvark;
import com.sun.labs.aura.aardvark.store.item.User;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;
import java.io.*;
import java.net.*;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author plamere
 */
public class RecommenderFeedServlet extends HttpServlet {

    /** 
    * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
    * @param request servlet request
    * @param response servlet response
    */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        String[] restfulParams = request.getPathInfo().split("/");

        if (restfulParams.length != 2) {
            response.sendError(response.SC_BAD_REQUEST, "missing user in  request");
        } else {
            try {
                String userID = restfulParams[1];
                Aardvark aardvark = (Aardvark) getServletContext().getAttribute("aardvark");

                if (aardvark == null) {
                    response.sendError(response.SC_INTERNAL_SERVER_ERROR, "Can't find aardvark");
                    return;
                }
                
                User user = aardvark.getUser(userID);
                if (user == null) {
                    response.sendError(response.SC_NOT_FOUND, "Can't find user " + userID);
                    return;
                }

                SyndFeed feed = aardvark.getRecommendedFeed(user);
                SyndFeedOutput output = new SyndFeedOutput();
                feed.setFeedType("atom_1.0");
                // feed.setLink();
                String feedXML = output.outputString(feed);
                response.setContentType("application/atom+xml");
                PrintWriter out = response.getWriter();
                out.println(feedXML);
                out.close();
            } catch (FeedException ex) {
                Logger.getLogger(RecommenderFeedServlet.class.getName()).log(Level.SEVERE, null, ex);
                response.sendError(response.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
            }
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
