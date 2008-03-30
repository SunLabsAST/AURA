package com.sun.labs.aura.aardvark.dashboard.web;

import com.sun.labs.aura.aardvark.BlogEntry;
import com.sun.labs.aura.aardvark.BlogFeed;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.util.AuraException;

import com.sun.labs.aura.util.Scored;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.*;
import javax.servlet.http.*;

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
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ServletContext context = getServletContext();
        DataStore dataStore = (DataStore) context.getAttribute("dataStore");


        String key = request.getParameter("key");

        int maxCount = 100;
        String maxCountString = request.getParameter("max");
        if (maxCountString != null) {
            maxCount = Integer.parseInt(maxCountString);
        }

        if (key != null) {
            try {
                List<Scored<Item>> scoredItems = dataStore.findSimilar(key, maxCount, null);

                response.setContentType("text/xml;charset=UTF-8");
                PrintWriter out = response.getWriter();
                StoryUtil.dumpScoredStories(out, dataStore, scoredItems);
                out.close();

            } catch (AuraException ex) {
                Shared.forwardToError(context, request, response, ex);
            }
        } else {
            Shared.forwardToError(context, request, response, "missing key");
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
