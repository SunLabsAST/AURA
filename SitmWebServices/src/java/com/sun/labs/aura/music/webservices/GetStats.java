/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.AttentionConfig;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.webservices.Util.ErrorCode;
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
public class GetStats extends HttpServlet {

    private final static String SERVLET_NAME = "GetStats";
    private ParameterChecker pc = null;

    public void init() throws ServletException {
        super.init();
        pc = new ParameterChecker(SERVLET_NAME, "get statistics about the database");
    }

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (pc.processDocumentationRequest(request, response)) {
            return;
        }

        Status status = new Status(request);
        ServletContext context = getServletContext();
        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            Util.tagOpen(out, SERVLET_NAME);
            pc.check(status, request);
            MusicDatabase mdb = DatabaseBroker.getMusicDatabase(context);
            if (mdb == null) {
                status.addError(ErrorCode.InternalError, "Can't connect to the music database");
                return;
            }
            try {
                DataStore ds = mdb.getDataStore();
                // show the number of items of each type

                Util.tag(out, "ready", "" + ds.ready());
                Util.tag(out, "replicants", Integer.toString(ds.getPrefixes().size()));
                for (ItemType t : ItemType.values()) {
                    long count = ds.getItemCount(t);
                    if (count > 0L) {
                        Util.tag(out, t.toString(), Long.toString(count));
                    }
                }
                for (Attention.Type t : Attention.Type.values()) {
                    AttentionConfig ac = new AttentionConfig();
                    ac.setType(t);
                    long count = ds.getAttentionCount(ac);
                    if (count > 0L) {
                        Util.tag(out, t.toString(), Long.toString(count));
                    }
                }
            } catch (AuraException ex) {
                status.addError(ErrorCode.InternalError, "Can't connect to the music database " + ex, ex);
            }
        } catch (ParameterException ex) {
        } finally {
            status.toXML(out);
            Util.tagClose(out, SERVLET_NAME);
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
