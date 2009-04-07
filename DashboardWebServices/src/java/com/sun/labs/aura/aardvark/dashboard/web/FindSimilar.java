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

import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.recommender.TypeFilter;
import com.sun.labs.aura.util.AuraException;

import com.sun.labs.aura.util.Scored;
import com.sun.labs.minion.WeightedField;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Generates a feed for a user
 */
public class FindSimilar extends HttpServlet {

    protected Logger logger = Logger.getLogger("");
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */

//  findSimilar?max=10&key=http://asdsdasd/f
    private final static WeightedField[] simFields = {
        new WeightedField("content", 1f),
        new WeightedField("aura-name", 1f),
        new WeightedField("tag", 1f),
        new WeightedField("autotag", 1f),
    };

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ServletContext context = getServletContext();
        DataStore dataStore = (DataStore) context.getAttribute("dataStore");


        String key = request.getParameter("key");

        int maxCount = 10;
        String maxCountString = request.getParameter("max");
        if (maxCountString != null) {
            maxCount = Integer.parseInt(maxCountString);
        }

        Item item = null;
        try {
            if (key != null && ((item = dataStore.getItem(key)) != null)) {
                Set<String> titleSet = new HashSet<String>();
                //List<Scored<Item>> scoredItems = dataStore.findSimilar(key, simFields, maxCount * 4, new TypeFilter(ItemType.BLOGENTRY));
                List<Scored<Item>> scoredItems = dataStore.findSimilar(key, maxCount * 4, new TypeFilter(ItemType.BLOGENTRY));
                List<Scored<Item>> filteredItems = new ArrayList<Scored<Item>>();

                titleSet.add(item.getName());
                
                for (Scored<Item> si : scoredItems) {

                    if (si.getItem().getKey().equals(key)) {
                        continue;
                    }

                    String normTitle = normalizeTitle(si.getItem().getName());

                    if (!titleSet.contains(normTitle)) {
                        titleSet.add(normTitle);
                        filteredItems.add(si);
                        if (filteredItems.size() >= maxCount) {
                            break;
                        }
                    }
                }
                response.setContentType("text/xml;charset=UTF-8");
                PrintWriter out = response.getWriter();
                StoryUtil.dumpScoredStories(out, dataStore, filteredItems);
                out.close();
            } else {
                Shared.forwardToError(context, request, response, "missing key");
            }
        } catch (AuraException ex) {
            Shared.forwardToError(context, request, response, ex);
        }
    }


    private String normalizeTitle(String title) {
        if (title == null) {
            return "";
        } else {
            return title.replaceAll("[^\\p{Alnum}]", "").toLowerCase();
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
