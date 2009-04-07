/*
 * Copyright 2007-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package com.sun.labs.aura.aardvark.web;

import com.sun.labs.aura.aardvark.Aardvark;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.util.AuraException;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;
import java.io.*;
import java.net.*;

import java.util.logging.Logger;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Generates a feed for a user
 */
public class Feed extends HttpServlet {

    protected Logger logger = Logger.getLogger("");

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ServletContext context = getServletContext();
        Aardvark aardvark = (Aardvark) context.getAttribute("aardvark");

        //
        // Get the feed based on the URL string that was provided.  This should
        // in theory be after the "/feed" and start with a /
        String pathInfo = request.getPathInfo();
        String[] pathParts = pathInfo.split("/");
        String randStr = pathParts[1];
        String feedName = pathParts[2];

        try {
            User u = aardvark.getUserByRandomString(randStr);
            if (u != null) {
                SyndFeed feed = null;
                if (feedName.equals("default")) {
                    feed = aardvark.getRecommendedFeed(u, 1);
                } else if (feedName.equals("flood")) {
                    feed = aardvark.getRecommendedFeed(u, 10);
                }
                SyndFeedOutput output = new SyndFeedOutput();
                feed.setFeedType("rss_2.0");
                feed.setLink(request.getRequestURL().toString());
                String feedXML = output.outputString(feed);
                response.setContentType("application/atom+xml");
                PrintWriter out = response.getWriter();
                out.println(feedXML);
                out.close();
            } else {
                // couldn't find user;
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown user");
            }
        } catch (AuraException e) {
            Shared.forwardToError(context, request, response, e);
        } catch (FeedException fe) {
            Shared.forwardToError(context, request, response, fe);
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
