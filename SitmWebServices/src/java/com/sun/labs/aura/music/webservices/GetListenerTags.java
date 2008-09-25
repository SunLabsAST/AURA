/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.music.Listener;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author plamere
 */
public class GetListenerTags extends HttpServlet {

    private final static String SERVLET_NAME = "GetListenerTags";

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
                String userID = request.getParameter("userID");
                String itemID = request.getParameter("itemID");


                if (userID == null) {
                    Util.outputStatus(out, SERVLET_NAME, Util.ErrorCode.MissingArgument, "missing userID");
                    return;
                }

                Listener listener = mdb.getListener(userID);

                if (itemID == null) {
                    List<Scored<String>> stags = mdb.getAllTags(listener.getKey());
                    if (stags != null) {
                        Util.tagOpen(out, SERVLET_NAME);
                        for (Scored<String> stag : stags) {
                            out.println("    <tag key=\"" + stag.getItem() + " score=\"" + stag.getScore() + "\"/>");
                        }
                        Util.outputOKStatus(out);
                        Util.tagClose(out, SERVLET_NAME);
                    } else {
                        Util.outputStatus(out, SERVLET_NAME, Util.ErrorCode.NotFound, "can't find tags");
                    }
                } else {
                    List<String> tags = mdb.getTags(listener.getKey(), itemID);
                    if (tags != null) {
                        Util.tagOpen(out, SERVLET_NAME);
                        for (String tag : tags) {
                            out.println("    <tag key=\"" + tag + "\"/>");
                        }
                        Util.outputOKStatus(out);
                        Util.tagClose(out, SERVLET_NAME);
                    } else {
                        Util.outputStatus(out, SERVLET_NAME, Util.ErrorCode.NotFound, "can't find tags");
                    }
                }
            }
        } catch (AuraException ex) {
            Util.outputStatus(out, SERVLET_NAME, Util.ErrorCode.InternalError, "Problem accessing data");
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
