/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.datastore.AttentionConfig;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.webservices.Util.ErrorCode;
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
    private ParameterChecker pc = null;

    public void init() throws ServletException {
        super.init();
        pc = new ParameterChecker(SERVLET_NAME, "get attention data from the database");
        pc.addParam("max", "100", "the maximum number of attention elements to return");
        pc.addParam("srcKey", null, "the source key");
        pc.addParam("tgtKey", null, "the destination key");
        pc.addParam("type", null, "the type of the attention");
        pc.addParam("svalue", null, "the optional string value associated with the attention type");
        pc.addParam("nvalue", null, "the optional integer value associated with the attention type");
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

            String src = pc.getParam(status,  request, "srcKey");
            String target = pc.getParam(status,  request, "tgtKey");
            Attention.Type type = (Attention.Type) pc.getParamAsEnum(status,  request, "type", Attention.Type.values());
            String svalue = pc.getParam(status,  request, "svalue");
            String nvalue = pc.getParam(status,  request, "nvalue");
            int maxCount = pc.getParamAsInt(status, request, "max", 1, 1000);

            AttentionConfig ac = new AttentionConfig();
            ac.setSourceKey(src);
            ac.setTargetKey(target);
            ac.setType(type);

            if (svalue != null) {
                ac.setStringVal(svalue);
            }

            if (nvalue != null) {
                try {
                    Long lval = Long.parseLong(nvalue);
                    ac.setNumberVal(lval);
                } catch (NumberFormatException nfe) {
                    status.addError(ErrorCode.BadArgument, "bad nvalue format");
                }
            }

            try {
                List<Attention> attns = mdb.getDataStore().getLastAttention(ac, maxCount);
                for (Attention attn : attns) {
                    out.println(Util.toXML(attn));
                }
            } catch (AuraException e) {
                status.addError(Util.ErrorCode.InternalError,
                        "Can't get attention data " + e);
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
        return "Get attention data from the database";
    }// </editor-fold>
}
