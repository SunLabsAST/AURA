/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.web;

import com.sun.labs.aura.aardvark.Aardvark;
import com.sun.labs.aura.aardvark.BlogUser;
import com.sun.labs.aura.aardvark.web.bean.UserBean;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.util.AuraException;
import java.io.*;
import java.net.*;

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
        User user = (User)session.getAttribute("loggedInUser");
        
        //
        // Figure out who the user is and make a bean
        UserBean ub = new UserBean(new BlogUser(user),
                                   "http://aardvark.tastekeeper.com/randomstring");
        Aardvark aardvark = (Aardvark)context.getAttribute("aardvark");
        try {
            ub.setNumFeeds(aardvark.getStats().getNumFeeds());
        } catch (AuraException e) {
            logger.log(Level.WARNING, "Failed to use aardvark", e);
        }
        request.setAttribute("userBean", ub);

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
