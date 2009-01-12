
package com.sun.labs.aura.website;

import com.sun.labs.aura.datastore.DataStore;
import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A servlet supertype that handles pulling out bits of our context.
 */
public class AuraServlet extends HttpServlet {
    protected ServletContext context;
    protected DataStore dataStore;

    /** 
    * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
    * @param request servlet request
    * @param response servlet response
    */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
    } 

    public void init(ServletConfig config) 
        throws javax.servlet.ServletException {
        super.init(config);
        //
        // get datastore handle from context
        context = config.getServletContext();
        dataStore = (DataStore)context.getAttribute("dataStore");
    }    
    
    protected void prepRequestForForward(HttpServletRequest request,
                                         HttpServletResponse response) {
        //
        // Everybody gets a stat bean to show in the header
        request.setAttribute("statBean", new StatBean(dataStore));
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
