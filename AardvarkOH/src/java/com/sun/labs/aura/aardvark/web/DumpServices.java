/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.web;

import com.sun.labs.util.props.ComponentRegistry;
import com.sun.labs.util.props.ConfigurationManager;
import java.io.*;
import java.net.*;

import java.util.List;
import java.util.Map;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author ja151348
 */
public class DumpServices extends HttpServlet {
   
    /** 
    * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
    * @param request servlet request
    * @param response servlet response
    */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        ServletContext context = getServletContext();
        ConfigurationManager cm = (ConfigurationManager)context.getAttribute("configManager");
        
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            out.println("<html><head><title>Servlet DumpServices</title></head>");
            out.println("<body>");
            ComponentRegistry cr = cm.getComponentRegistry();
            if (cr == null) {
                out.println("<h1>ComponentRegistry was null!</h1>");
                return;
            }
            Map<String,List<String>> reggies = cr.dumpJiniServices();
            out.println("<h1>Discovered Registrars:</h1>");
            out.println("<ul>");
            for (String reg : reggies.keySet()) {
                out.println("<li>" + reg + "</li>");
                out.println("<ol>");
                List<String> svcs = reggies.get(reg);
                for (String svc : svcs) {
                    out.println("<li>" + svc + "</li>");
                }
                out.println("</ol>");
            }
            out.println("</ul>");
            out.println("</body>");
            out.println("</html>");
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
