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
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author jalex
 */
public class Settings extends HttpServlet {
   
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
        // See if we were supposed to modify anything, then print the page again.
        String op = request.getParameter("op");
        if (op != null) {
            if (op.equals("AddFeed")) {
                String feed = request.getParameter("newFeed");
                feed = feed.trim();
                //
                // See if we got a valid URL
                try {
                    URL uri = new URL(feed);
                    
                    //
                    // Feed is a valid URI at least, so add it where appropriate
                    aardvark.addUserFeed(user, feed, Attention.Type.STARRED_FEED);
                    //
                    // Update the bean too
                    UserBean ub = (UserBean)session.getAttribute("userBean");
                    String[] newBasisFeeds = new String[ub.getBasisFeeds().length + 1];
                    System.arraycopy(ub.getBasisFeeds(), 0, newBasisFeeds, 0, ub.getBasisFeeds().length);
                    newBasisFeeds[newBasisFeeds.length - 1] = feed;
                    ub.setBasisFeeds(newBasisFeeds);
                } catch (MalformedURLException e) {
                    //
                    // Send them back to the page with a status message
                    request.setAttribute("prevNewFeed", feed);
                    request.setAttribute("feedStatus", "Sorry, that URL could not be understood.  Please re-enter it.");
                } catch (AuraException ae) {
                    Shared.forwardToError(context, request, response, ae);
                    return;
                }
            } else if (op.equals("RemoveFeed")) {
                String removeIdx = request.getParameter("toRemove");
                int idx = Integer.parseInt(removeIdx);
                UserBean ub = (UserBean)session.getAttribute("userBean");
                String[] basisFeeds = ub.getBasisFeeds();
                String rfeed = basisFeeds[idx];
                try {
                    //
                    // Remove internally
                    aardvark.removeUserFeed(user, rfeed, Attention.Type.STARRED_FEED);
                    
                    //
                    // Now update the user feeds in the bean
                    Set<BlogFeed> feeds = aardvark.getFeeds(user, Attention.Type.STARRED_FEED);
                    String tasteFeed = "no feed found!";
                    if (!feeds.isEmpty()) {
                        tasteFeed = feeds.iterator().next().getKey();
                    }
                    basisFeeds = new String[feeds.size()];
                    int i = 0;
                    for (BlogFeed f : feeds) {
                        basisFeeds[i++] = f.getKey();
                    }
                    ub.setBasisFeeds(basisFeeds);
                } catch (AuraException ae) {
                    Shared.forwardToError(context, request, response, ae);
                }
            } else if (op.equals("UpdateSettings")) {
                String nickname = request.getParameter("nickname");
                String fullname = request.getParameter("fullname");
                String email = request.getParameter("email");
                BlogUser bu = new BlogUser(user);
                bu.setNickname(nickname);
                bu.setFullname(fullname);
                bu.setEmailAddress(email);
                try {
                    user = aardvark.updateUser(user);
                    UserBean ub = (UserBean)session.getAttribute("userBean");
                    ub.setNickname(nickname);
                    ub.setFullname(fullname);
                    ub.setEmailAddress(email);
                } catch (AuraException e) {
                    Shared.forwardToError(context, request, response, e);
                }
            }
        }
        
        //
        // Get all the attentions, then make an array of beans for the web page
        try {
            Shared.fillPageHeader(request, aardvark);

            RequestDispatcher dispatcher = context.getRequestDispatcher("/settings.jsp");
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
