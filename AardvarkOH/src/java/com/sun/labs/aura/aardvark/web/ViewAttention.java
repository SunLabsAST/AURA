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
import com.sun.labs.aura.aardvark.web.bean.AttentionBean;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.util.AuraException;
import java.io.*;
import java.net.*;

import java.util.Date;
import java.util.List;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author ja151348
 */
public class ViewAttention extends HttpServlet {
   
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
        
        //
        // Get all the attentions, then make an array of beans for the web page
        try {
            Shared.fillPageHeader(request, aardvark);
            List<Attention> attention = aardvark.getAttention(user);
            AttentionBean[] beans = new AttentionBean[attention.size()];
            int i = 0;
            for (Attention a : attention) {
                beans[i] = new AttentionBean(
                        a.getSourceKey(),
                        a.getTargetKey(),
                        a.getType().toString(),
                        new Date(a.getTimeStamp()).toString());
                Item targ = aardvark.getItem(a.getTargetKey());
                beans[i++].setRealName(targ.getName());
            }
            //request.setAttribute("userBean", new UserBean(new BlogUser(user)));
            request.setAttribute("attnBean", beans);
            RequestDispatcher dispatcher = context.getRequestDispatcher("/viewAttention.jsp");
            dispatcher.forward(request, response);
        } catch (AuraException e) {
            Shared.forwardToError(context, request, response, e);
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
