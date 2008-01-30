/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.attention;

import java.io.*;
import java.net.*;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author plamere
 */
public class addAttention extends HttpServlet {
   
    /** 
    * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
    * @param request servlet request
    * @param response servlet response
    */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            /* TODO output your page here
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet addAttention</title>");  
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet addAttention at " + request.getContextPath () + "</h1>");
            out.println("</body>");
            out.println("</html>");
            */
        } finally { 
            out.close();
        }
    } 


    // track resolution:

    // possible inputs:
    //    mbtid
    //    mbrid
    //    mbaid
    //    tn:  rn:    an:

    // examples:
    //    addAttention?sessionID=12341234&mbtid=123412-1234-123-123&att=play
    //    addAttention?sessionID=12341234&att=play&tn=stairway+to+heaven&an=Led+Zeppelin

    private String getAppKey(HttpServletResponse request) {
        return "";
    }

    private String getUserID(HttpServletResponse request) {
        return "";
    }

    private String getItemID(HttpServletResponse request) {
        return "";
    }

    private String getAttentionType(HttpServletResponse request) {
        return "";
    }

    private String getAttentionArg(HttpServletResponse request) {
        return "";
    }

    private long getTimestamp(HttpServletResponse request) {
        return 0;
    }

    private void addAttentionData(String appKey, String userID, String itemID, String attentionType, String attentionArg, long timestamp) {
    }
    

    private boolean isValidAppKey(String key) {
        return true;
    }

    private boolean isValidUser(String user) {
        return true;
    }

    private boolean isValidItem(String item) {
        return true;
    }

    private boolean isAttention(String attentionType, String attentionArg) {
        return true;
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
