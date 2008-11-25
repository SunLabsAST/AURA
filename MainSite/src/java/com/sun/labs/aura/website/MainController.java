
package com.sun.labs.aura.website;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 */
public class MainController extends AuraServlet {
   
    /** 
    * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
    * @param request servlet request
    * @param response servlet response
    */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        //
        // We'll figure out what to do with this as we go along.
        RequestDispatcher dispatcher;

        //
        // Get the servlet path 
        // (eg "/mf", "/search", "/message", "/thread", "/alias", or "/author")
        String servletPath = request.getServletPath();
        prepRequestForForward(request, response);
        
        if (servletPath.equals("/Index")) {
            dispatcher = context.getRequestDispatcher("/index.jsp");
            dispatcher.forward(request, response);
        } else if (servletPath.equals("/content")) {
            String pathInfo = request.getPathInfo();
            if (pathInfo != null) {
                dispatcher = context.getRequestDispatcher(pathInfo);
                dispatcher.forward(request, response);
            } else {
                goHome(request, response);
            }
        }
    }
    
    protected void goHome(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        response.sendRedirect(
                response.encodeRedirectURL(request.getContextPath() + "/"));
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
    }// </editor-fold>

}
