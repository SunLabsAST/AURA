/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.web;

import com.sun.labs.aura.aardvark.Aardvark;
import com.sun.labs.aura.aardvark.BlogFeed;
import com.sun.labs.aura.aardvark.BlogUser;
import com.sun.labs.aura.aardvark.web.bean.UserBean;
import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.util.AuraException;
import java.io.*;
import java.net.*;

import java.util.Set;
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
        Aardvark aardvark = (Aardvark)context.getAttribute("aardvark");
        User user = (User)session.getAttribute("loggedInUser");
        if (user == null) {
            response.sendRedirect(response.encodeRedirectURL("/Welcome"));
        }
        logger.log(Level.INFO, "Getting home for user " + user);
        
        try {
            Shared.fillPageHeader(request, aardvark);
            //
            // Figure out who the user is, get their feed, and make a bean
            Set<BlogFeed> feeds = aardvark.getFeeds(user, Attention.Type.STARRED_FEED);
            String defaultFeed = "some feed here...";
            if (!feeds.isEmpty()) {
                defaultFeed = feeds.iterator().next().getKey();
            }
            UserBean ub = new UserBean(new BlogUser(user),
                                       defaultFeed);
            ub.setRecommendedFeedURL("/feed/" + user.getUserRandString() + "/default");
            request.setAttribute("userBean", ub);
        } catch (AuraException e) {
            logger.log(Level.WARNING, "Failed to use aardvark", e);
        }


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
