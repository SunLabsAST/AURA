/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.webservices.ItemFormatter.OutputType;
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
public class GetItems extends HttpServlet {
    private enum Format { FULL, COMPACT};
    private final static String SERVLET_NAME = "GetItems";
    private ParameterChecker pc;

    public void init() throws ServletException {
        super.init();
        pc = new ParameterChecker(SERVLET_NAME, "get items from the database");
        pc.addParam("key", "the key to the item of interest");
        pc.addParam("outputType", OutputType.Tiny.name(), "the type of output");
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
            ItemFormatterManager formatter = DatabaseBroker.getItemFormatterManager(context);

            if (mdb == null || formatter == null) {
                status.addError(ErrorCode.InternalError, "Can't connect to the music database");
                return;
            }

            String itemID = pc.getParam(status, request, "key");
            OutputType outputType = (OutputType) pc.getParamAsEnum(status, request, "outputType", OutputType.values());

            String[] keys = itemID.split(",");
            try {
                for (String key : keys) {
                    key = key.trim();
                    long fetchStart = System.currentTimeMillis();
                    Item item = mdb.getDataStore().getItem(key);
                    long delta = System.currentTimeMillis() - fetchStart;
                    if (item != null) {
                        out.println(formatter.toXML(item, outputType));
                        out.println("<!-- item fetch in " + delta + " ms -->");
                    } else {
                        status.addError(ErrorCode.NotFound, key);
                    }
                }
            } catch (AuraException ex) {
                status.addError(ErrorCode.InternalError, "Problem accessing the data", ex);
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
        return "Get items from the database";
    }// </editor-fold>
}
