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

package com.sun.labs.aura.aardvark.dashboard.web;

import com.sun.labs.aura.aardvark.Aardvark;

import com.sun.labs.aura.aardvark.Stats;
import com.sun.labs.aura.util.AuraException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Generates a feed for a user
 */
public class GetStatus extends HttpServlet {

    protected Logger logger = Logger.getLogger("");

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */

//  getStories?topics=news,music,business,technology,all&time=epoch&delta=epoch&max=count
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ServletContext context = getServletContext();
        Aardvark aardvark = (Aardvark) context.getAttribute("aardvark");
        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            Stats stats = aardvark.getStats();
            out.println("<status>");
            out.println("     <entries>" + stats.getNumEntries() + "</entries>");
            out.println("     <feeds>" + stats.getNumFeeds() + "</feeds>");
            out.println("     <pulls>" + stats.getFeedPullCount() + "</pulls>");
            out.println("     <users>" + stats.getNumUsers() + "</users>");
            out.println("     <taste>" + stats.getNumAttentionData() + "</taste>");
            out.println("     <entriesPerMinute>" + stats.getEntriesPerMin() + "</entriesPerMinute>");
            out.println("</status>");
        } catch (IOException ex) {
            response.setStatus(response.SC_NOT_FOUND);
        } catch(AuraException ex) {
            response.setStatus(response.SC_NOT_FOUND);
        } finally {
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
