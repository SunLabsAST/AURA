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
import java.io.PrintWriter;
import java.util.Date;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Handles survey submission
 * @author ja151348
 */
public class SurveyServlet extends HttpServlet {


    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        String servletPath = request.getServletPath();
        HttpSession session = request.getSession();
        RequestDispatcher rd = request.getRequestDispatcher("/wmesurvey.jsp");
        Boolean alreadyTaken = (Boolean)session.getAttribute("alreadyTaken");
        if (alreadyTaken != null && alreadyTaken == true) {
            request.setAttribute("alreadyTaken", true);
            request.setAttribute("submitted", false);
        } else {
            request.setAttribute("alreadyTaken", false);

            if (servletPath.equals("/wmesurveysubmit")) {
                session.setAttribute("alreadyTaken", true);
                request.setAttribute("submitted", true);
                request.setAttribute("alreadyTaken", true);
                //
                // Get all the values and store them to a file
                PrintWriter pw = (PrintWriter) session.getServletContext().getAttribute("surveyWriter");
                synchronized(pw) {
                    pw.print(request.getParameter("question1") + "\t");
                    pw.print(request.getParameter("question2") + "\t");
                    pw.print(request.getParameter("question3") + "\t");
                    pw.print(request.getParameter("question4") + "\t");
                    pw.print(request.getParameter("question5") + "\t");
                    pw.print(request.getParameter("question6") + "\t");
                    pw.print(request.getParameter("question7") + "\t");
                    pw.print(request.getParameter("question8") + "\t");
                    pw.print(request.getParameter("question9").replaceAll("[\\r\\n\\t]", " ") + "\t");
                    pw.print(request.getParameter("question10").replaceAll("[\\r\\n\\t]", " ") + "\t");
                    pw.print(request.getHeader("User-Agent").replaceAll("\\t", " ") + "\t");
                    pw.println(new Date());
                    pw.flush();
                }
            }
        }
        rd.forward(request, response);
    } 

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Survey Servlet";
    }// </editor-fold>

}
