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
import com.sun.labs.aura.aardvark.BlogFeed;
import com.sun.labs.aura.aardvark.BlogUser;
import com.sun.labs.aura.aardvark.web.bean.UserBean;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.util.AuraException;
import java.io.*;
import java.net.*;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author ja151348
 */
public class Home extends HttpServlet {
    protected Logger logger = Logger.getLogger("");
    
    /** 
    * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
    * @param request servlet request
    * @param response servlet response
    */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        ServletContext context = getServletContext();
        HttpSession session = request.getSession();
        Aardvark aardvark = (Aardvark)context.getAttribute("aardvark");
        User user = (User)session.getAttribute("loggedInUser");
        if (user == null) {
            response.sendRedirect(response.encodeRedirectURL(request.getContextPath() + "/Welcome"));
            return;
        }
        logger.log(Level.INFO, "Getting home for user " + user);
        
        try {
            Shared.fillPageHeader(request, aardvark);
            //
            // Figure out who the user is, get their feed, and make a bean
            Set<BlogFeed> feeds = aardvark.getFeeds(user, Attention.Type.STARRED_FEED);
            String tasteFeed = "no feed found!";
            if (!feeds.isEmpty()) {
                tasteFeed = feeds.iterator().next().getKey();
            }
            UserBean ub = new UserBean(new BlogUser(user),
                                       tasteFeed);
            String[] basisFeeds = new String[feeds.size()];
            int i = 0;
            for (BlogFeed f : feeds) {
                basisFeeds[i++] = f.getKey();
            }
            ub.setBasisFeeds(basisFeeds);
            ub.setRecommendedFeedURL("/feed/" + user.getUserRandString());
            session.setAttribute("userBean", ub);
        } catch (AuraException e) {
            logger.log(Level.WARNING, "Failed to use aardvark", e);
        }


        RequestDispatcher dispatcher =
                context.getRequestDispatcher("/home.jsp");
        dispatcher.forward(request, response);
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
