/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.MusicDatabase.DBOperation;
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
public class AddAttentionData extends HttpServlet {

    private final static String SERVLET_NAME = "AddAttentionData";
    private ParameterChecker pc = null;

    public void init() throws ServletException {
        super.init();
        pc = new ParameterChecker(SERVLET_NAME, "Adds attention data to the database");
        pc.addParam("appKey", "the application key");
        pc.addParam("srcKey", "the key of the attention source");
        pc.addParam("tgtKey", "the key of the attentin target");
        pc.addParam("type", "the type of the attention");
        pc.addParam("value", null, "the optional value associated with the attention type");
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

        ServletContext context = getServletContext();

        Status status = new Status(request);
        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            Util.tagOpen(out, SERVLET_NAME);
            pc.check(status, request);

            MusicDatabase mdb = DatabaseBroker.getMusicDatabase(context);

            if (mdb == null) {
                status.addError(ErrorCode.InternalError, "Can't connect to the music database");
                throw new ParameterException();
            }

            String appKey = pc.getParam(status, request, "appKey");
            String srcKey = pc.getParam(status, request, "srcKey");
            String destKey = pc.getParam(status, request, "tgtKey");
            Attention.Type type = (Attention.Type) pc.getParamAsEnum(status, request, "type", Attention.Type.values());
            String value = pc.getParam(status, request, "value");

            if (!mdb.isValidApplication(appKey)) {
                status.addError(ErrorCode.BadArgument, "not a valid application");
                return;
            }

            if (!mdb.hasAuthorization(appKey, DBOperation.AddAttention)) {
                status.addError(ErrorCode.NotAuthorized, "application not authorized to add attention data");
                return;
            }
            mdb.addAttention(srcKey, destKey, type, value);
        } catch (AuraException ex) {
            status.addError(ErrorCode.InternalError, "Problem adding attention data " + ex.getMessage());
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
        return "Adds attention to the datastore";
    }// </editor-fold>
}
