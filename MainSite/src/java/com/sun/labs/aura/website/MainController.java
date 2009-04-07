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

package com.sun.labs.aura.website;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 */
public class MainController extends AuraServlet {
   
    /** 
    * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
    * @param request servlet request
    * @param response servlet response
    */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        //
        // We'll figure out what to do with this as we go along.
        RequestDispatcher dispatcher;

        //
        // Get the servlet path 
        // (eg "/mf", "/search", "/message", "/thread", "/alias", or "/author")
        String servletPath = request.getServletPath();
        prepRequestForForward(request, response);
        
        if (servletPath.equals("/Index")) {
            dispatcher = context.getRequestDispatcher("/index.jsp");
            dispatcher.forward(request, response);
        } else if (servletPath.equals("/content")) {
            String pathInfo = request.getPathInfo();
            if (pathInfo != null) {
                dispatcher = context.getRequestDispatcher(pathInfo);
                dispatcher.forward(request, response);
            } else {
                goHome(request, response);
            }
        } else if (servletPath.equals("/sorry")) {
            dispatcher = context.getRequestDispatcher("/sorry.jsp");
            dispatcher.forward(request, response);
        }
    }
    
    protected void goHome(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        response.sendRedirect(
                response.encodeRedirectURL(request.getContextPath() + "/"));
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
