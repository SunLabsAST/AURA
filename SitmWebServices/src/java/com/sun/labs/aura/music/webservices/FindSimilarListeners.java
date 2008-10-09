/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.music.Listener;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.webservices.Util.ErrorCode;
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
public class FindSimilarListeners extends HttpServlet {

    private final static String SERVLET_NAME = "FindSimilarListeners";

    private ParameterChecker pc;

    @Override
    public void init() throws ServletException {
        super.init();
        pc = new ParameterChecker(SERVLET_NAME, "find listeners similar to a seed listener");
        pc.addParam("key", "the key of the item of interest");
        pc.addParam("max", "10", "the maxiumum number of artists to return");
        pc.addParam("field", Listener.FIELD_SOCIAL_TAGS, "the field to use for similarity");
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

            int maxCount = pc.getParamAsInt(status, request, "max", 1, 250);
            String key = pc.getParam(status, request, "key");
            String field = pc.getParam(status, request, "field"); //TBD field is not used yet.
            Listener listener = mdb.getListener(key);
            if (listener != null) {
                List<Scored<Listener>> similarListeners = mdb.listenerFindSimilar(key, maxCount);
                for (Scored<Listener> scoredListener : similarListeners) {

                    if (scoredListener.getItem().getKey().equals(key)) {
                        continue;
                    }

                    Listener simListener = scoredListener.getItem();
                    out.println("    <listener key=\"" +
                            simListener.getKey() + "\" " +
                            "score=\"" + scoredListener.getScore() + "\" " +
                            "name=\"" + Util.filter(simListener.getName()) + "\"" +
                            "/>");
                }
            } else {
                status.addError(ErrorCode.BadArgument, "Can't find user with key " + key);
            }
        } catch (AuraException ex) {
            status.addError(Util.ErrorCode.InternalError, "Problem accessing data " + ex);
        } catch (ParameterException e) {
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
        return "Finds listeners that are similar to a seed listener. ";
    }// </editor-fold>
}
