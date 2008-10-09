/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.music.Listener;
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
public class AddListener extends HttpServlet {

    private final static String SERVLET_NAME = "AddListener";
    private ParameterChecker pc = null;

    public void init() throws ServletException {
        super.init();
        pc = new ParameterChecker(SERVLET_NAME, "adds a new user/listener to the database");
        pc.addParam("appKey", "the application key");
        pc.addParam("userKey", "the key of the user");
        pc.addParam("lastfmName", null, "the lastfm name of the user");
        pc.addParam("pandoraName", null, "the pandora name of the user");
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

            MusicDatabase mdb = (MusicDatabase) context.getAttribute("MusicDatabase");

            if (mdb == null) {
                status.addError(ErrorCode.InternalError, "Can't connect to the music database");
                return;
            }

            String userKey = pc.getParam(status,  request, "userKey");
            String lastfmName = pc.getParam(status, request, "lastfmName");
            String pandoraName = pc.getParam(status, request, "pandoraName");


            if (mdb.getListener(userKey) != null) {
                status.addError(ErrorCode.BadArgument, "userKey already exists");
                return;
            }

            Listener listener = mdb.enrollListener(userKey);

            if (lastfmName != null) {
                listener.setLastFmName(lastfmName);
            }

            if (pandoraName != null) {
                listener.setPandoraName(pandoraName);
            }

            mdb.updateListener(listener);
        } catch (AuraException ex) {
            status.addError(ErrorCode.InternalError, "Problem accessing data");
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
        return "Adds a listener ot the database";
    }// </editor-fold>
}
