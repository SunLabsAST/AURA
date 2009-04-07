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

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Some shared code across the servlets
 */
public class Shared {

    
    public static void forwardToError(ServletContext context,
                                      HttpServletRequest req,
                                      HttpServletResponse resp,
                                      Exception e) 
            throws ServletException, IOException {
        forwardToError(context, req, resp, e.getMessage());
    }

    public static void forwardToError(ServletContext context,
                                      HttpServletRequest req,
                                      HttpServletResponse response,
                                      String msg) 
            throws ServletException, IOException {
            req.setAttribute("errorMsg", msg);
            PrintWriter out = response.getWriter();
            response.setContentType("text/html;charset=UTF-8");
            out.println("<html>");
            out.println("<head>");
            out.println("<title> Error" + msg + "</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h2>" + "Error " + msg + "</h2>");
            out.println("</body>");
            out.println("</html>");
            out.close();
    }
}
