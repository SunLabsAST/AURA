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

package com.sun.labs.aura.attention;

import java.io.*;
import java.net.*;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author plamere
 */
public class addAttention extends HttpServlet {
   
    /** 
    * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
    * @param request servlet request
    * @param response servlet response
    */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            /* TODO output your page here
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet addAttention</title>");  
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet addAttention at " + request.getContextPath () + "</h1>");
            out.println("</body>");
            out.println("</html>");
            */
        } finally { 
            out.close();
        }
    } 


    // track resolution:

    // possible inputs:
    //    mbtid
    //    mbrid
    //    mbaid
    //    tn:  rn:    an:

    // examples:
    //    addAttention?sessionID=12341234&mbtid=123412-1234-123-123&att=play
    //    addAttention?sessionID=12341234&att=play&tn=stairway+to+heaven&an=Led+Zeppelin

    private String getAppKey(HttpServletResponse request) {
        return "";
    }

    private String getUserID(HttpServletResponse request) {
        return "";
    }

    private String getItemID(HttpServletResponse request) {
        return "";
    }

    private String getAttentionType(HttpServletResponse request) {
        return "";
    }

    private String getAttentionArg(HttpServletResponse request) {
        return "";
    }

    private long getTimestamp(HttpServletResponse request) {
        return 0;
    }

    private void addAttentionData(String appKey, String userID, String itemID, String attentionType, String attentionArg, long timestamp) {
    }
    

    private boolean isValidAppKey(String key) {
        return true;
    }

    private boolean isValidUser(String user) {
        return true;
    }

    private boolean isValidItem(String item) {
        return true;
    }

    private boolean isAttention(String attentionType, String attentionArg) {
        return true;
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
