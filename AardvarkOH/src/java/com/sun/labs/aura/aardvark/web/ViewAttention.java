/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.web;

import com.sun.labs.aura.aardvark.Aardvark;
import com.sun.labs.aura.aardvark.BlogUser;
import com.sun.labs.aura.aardvark.web.bean.AttentionBean;
import com.sun.labs.aura.aardvark.web.bean.UserBean;
import com.sun.labs.aura.datastore.Attention;
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
                beans[i++] = new AttentionBean(
                        a.getSourceKey(),
                        a.getTargetKey(),
                        a.getType().toString(),
                        new Date(a.getTimeStamp()).toString());
            }
            request.setAttribute("userBean", new UserBean(new BlogUser(user)));
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
