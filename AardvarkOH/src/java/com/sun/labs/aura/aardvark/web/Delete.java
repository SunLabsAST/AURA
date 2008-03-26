/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.web;

import com.sun.labs.aura.aardvark.Aardvark;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.util.AuraException;
import java.io.*;
import java.net.*;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author ja151348
 */
public class Delete extends HttpServlet {

    protected Aardvark aardvark;
    
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        aardvark = (Aardvark)config.getServletContext()
                .getAttribute("aardvark");
    }
   
    /** 
    * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
    * @param request servlet request
    * @param response servlet response
    */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        String userKey = request.getParameter("deleteUser");
        if (userKey != null) {
            try {
                User u = aardvark.getUser(userKey);
                if (u != null) {
                    out.println("Deleting user: " + userKey);
                    aardvark.deleteUser(u);
                } else {
                    out.println("User was null, ignoring");
                }
            } catch (AuraException e) {
                out.println("Failed to delete user: " + e.getMessage());
            }
        }
        try {
            out.println("<h1>Done!</h1>");
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
