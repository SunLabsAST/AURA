package com.sun.labs.aura.aardvark.web;

import com.sun.labs.aura.aardvark.Aardvark;
import com.sun.labs.aura.datastore.User;
import com.sun.labs.aura.util.AuraException;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;
import java.io.*;
import java.net.*;

import java.util.logging.Logger;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Generates a feed for a user
 */
public class Feed extends HttpServlet {
    protected Logger logger = Logger.getLogger("");
    
    /** 
    * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
    * @param request servlet request
    * @param response servlet response
    */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        ServletContext context = getServletContext();
        Aardvark aardvark = (Aardvark)context.getAttribute("aardvark");

        //
        // Get the feed based on the URL string that was provided.  This should
        // in theory be after the "/feed" and start with a /
        String pathInfo = request.getPathInfo();
        String[] pathParts = pathInfo.split("/");
        String randStr = pathParts[1];
       
        try {
            User u = aardvark.getUserByRandomString(randStr);
            SyndFeed feed = aardvark.getRecommendedFeed(u);
            SyndFeedOutput output = new SyndFeedOutput();
            feed.setFeedType("rss_2.0");
            feed.setLink(request.getRequestURL().toString());
            String feedXML = output.outputString(feed);
            response.setContentType("application/atom+xml");
            PrintWriter out = response.getWriter();
            out.println(feedXML);
            out.close();
        } catch (AuraException e) {
            Shared.forwardToError(context, request, response, e);
        } catch (FeedException fe) {
            Shared.forwardToError(context, request, response, fe);
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
