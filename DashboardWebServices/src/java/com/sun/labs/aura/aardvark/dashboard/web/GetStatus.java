package com.sun.labs.aura.aardvark.dashboard.web;

import com.sun.labs.aura.aardvark.Aardvark;

import com.sun.labs.aura.aardvark.Stats;
import com.sun.labs.aura.util.AuraException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Generates a feed for a user
 */
public class GetStatus extends HttpServlet {

    protected Logger logger = Logger.getLogger("");

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */

//  getStories?topics=news,music,business,technology,all&time=epoch&delta=epoch&max=count
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ServletContext context = getServletContext();
        Aardvark aardvark = (Aardvark) context.getAttribute("aardvark");
        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            Stats stats = aardvark.getStats();
            out.println("<status>");
            out.println("     <entries>" + stats.getNumEntries() + "</entries>");
            out.println("     <feeds>" + stats.getNumFeeds() + "</feeds>");
            out.println("     <users>" + stats.getNumUsers() + "</users>");
            out.println("     <taste>" + stats.getNumAttentionData() + "</taste>");
            out.println("     <entriesPerMinute>" + stats.getEntriesPerMin() + "</entriesPerMinute>");
            out.println("</status>");
        } catch (IOException ex) {
            response.setStatus(response.SC_NOT_FOUND);
        } catch(AuraException ex) {
            response.setStatus(response.SC_NOT_FOUND);
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
