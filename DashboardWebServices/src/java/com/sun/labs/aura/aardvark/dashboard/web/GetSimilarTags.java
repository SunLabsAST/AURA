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

import com.sun.labs.aura.aardvark.BlogEntry;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.ScoredComparator;
import java.io.*;
import java.net.*;

import java.util.Collections;
import java.util.List;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author plamere
 */
public class GetSimilarTags extends HttpServlet {
   
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

                try {
                    out.println("<TagInfos>");
                    //out.println("    <key>" + key + "</key>");
                    for (Scored<String> tag : autotags) {
                        out.printf("    <TagInfo name='%s' score='%f'>\n", tag.getItem(), tag.getScore());
                        out.println("        <DocTerms>");
                        for (Scored<String> explanation : dataStore.getExplanation(key, tag.getItem(), 20)) {
                            out.printf("            <DocTerm name=\'%s\' score=\'%f\'/>\n", explanation.getItem(), explanation.getScore());
                        }
                        out.println("        </DocTerms>");

                        // TBD - change this from getExplanation to getTopTerms when it is written
                        out.println("        <TopTerms>");
                        for (Scored<String> term : dataStore.getTopAutotagTerms(tag.getItem(), 20)) {
                            out.printf("            <TopTerm name=\'%s\' score=\'%f\'/>\n", term.getItem(), term.getScore());
                        }
                        out.println("        </TopTerms>");
                        out.println("    </TagInfo>");
                    }
                    out.println("</TagInfos>");
                } finally {
                    out.close();
                }
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
