/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */

package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.util.AuraException;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author plamere
 */
public class GetItem extends HttpServlet {
    private final static String SERVLET_NAME = "GetItem";
   
    /** 
    * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
    * @param request servlet request
    * @param response servlet response
    */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
                ServletContext context = getServletContext();

        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Timer timer = Util.getTimer();

        try {
            MusicDatabase mdb = (MusicDatabase) context.getAttribute("MusicDatabase");

            if (mdb == null) {
                Util.outputStatus(out, SERVLET_NAME, Util.ErrorCode.InternalError, "Can't connect to the music database");
            } else {
                String itemID = request.getParameter("itemID");
                if (itemID != null) {
                    try {
                        long fetchStart = System.currentTimeMillis();
                        Item item = mdb.getDataStore().getItem(itemID);
                        long delta = System.currentTimeMillis() - fetchStart;
                        if (item != null) {
                            Util.tagOpen(out, SERVLET_NAME);
                            out.println(Util.toXML(item));
                            Util.outputOKStatus(out);
                            out.println("<!-- item fetch in " + delta + " ms -->" );
                            Util.tagClose(out, SERVLET_NAME);
                        } else {
                            Util.outputStatus(out, SERVLET_NAME, Util.ErrorCode.BadArgument, "Can't find item with id " + itemID);
                        }
                    } catch (AuraException ex) {
                        Util.outputStatus(out, SERVLET_NAME, Util.ErrorCode.InternalError, "Problem accessing data " + ex);
                    }
                } else {
                    Util.outputStatus(out, SERVLET_NAME, Util.ErrorCode.MissingArgument, "Missing itemID");
                }
            }
        } finally {
            timer.report(out);
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
    }// </editor-fold>

}
