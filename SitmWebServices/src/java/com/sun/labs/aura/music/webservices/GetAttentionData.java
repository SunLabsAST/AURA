/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.AttentionConfig;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.util.AuraException;
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
public class GetAttentionData extends HttpServlet {

    private final static String SERVLET_NAME = "GetAttentionData";

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
                Util.outputStatus(out, SERVLET_NAME, Util.ErrorCode.DataStore, "Can't connect to the music database");
            } else {
                String src = request.getParameter("src");
                String target = request.getParameter("tgt");
                String stringValue = request.getParameter("sv");
                String numValue = request.getParameter("nv");
                String stype = request.getParameter("type");

                Attention.Type type = null;
                if (stype != null) {
                    try {
                        type = Attention.Type.valueOf(stype);
                    } catch (IllegalArgumentException ex) {
                        Util.outputStatus(out, SERVLET_NAME, Util.ErrorCode.BadArgument,
                                "bad type " + stype);
                        return;
                    }
                }

                int maxCount = 100;
                String maxCountString = request.getParameter("max");
                if (maxCountString != null) {
                    maxCount = Integer.parseInt(maxCountString);
                }

                AttentionConfig ac = new AttentionConfig();
                ac.setSourceKey(src);
                ac.setTargetKey(target);
                ac.setType(type);
                ac.setStringVal(stringValue);
                ac.setStringVal(numValue);

                try {
                    List<Attention> attns = mdb.getDataStore().getLastAttention(ac, maxCount);

                    Util.tagOpen(out, SERVLET_NAME);
                    for (Attention attn : attns) {
                        out.println(Util.toXML(attn));
                    }
                    Util.tagClose(out, SERVLET_NAME);
                } catch (AuraException e) {
                    Util.outputStatus(out, SERVLET_NAME, Util.ErrorCode.DataStore, 
                            "Can't get attention data " + e);
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
