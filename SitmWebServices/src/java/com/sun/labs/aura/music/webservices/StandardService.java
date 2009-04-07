/*
 * Copyright 2008-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.webservices.Util.ErrorCode;
import com.sun.labs.aura.util.AuraException;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author plamere
 */
public abstract class StandardService extends HttpServlet {
    public enum DocType { RawText, RawXML, StandardXML, HTML };

    private ParameterChecker pc;
    private DocType docType = DocType.StandardXML;

    @Override
    public void init() throws ServletException {
        super.init();
        pc = new ParameterChecker(getServletName(), getServletInfo());
        initParams();
    }

    abstract void initParams();

    abstract void go(HttpServletRequest request, PrintWriter out, MusicDatabase mdb)
            throws ParameterException, AuraException, RemoteException;


    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ServletContext context = getServletContext();

        if (pc.processDocumentationRequest(request, response)) {
            return;
        }

        long startTime = System.currentTimeMillis();

        Status status = new Status(request);

        if (docType == DocType.RawText) {
            response.setContentType("text/plain;charset=UTF-8");
        } else if (docType == DocType.HTML) {
            response.setContentType("text/html;charset=UTF-8");
        } else {
            response.setContentType("text/xml;charset=UTF-8");
        }

        PrintWriter out = response.getWriter();

        try {
            if (docType == DocType.StandardXML) {
                Util.tagOpen(out, getServletName());
            }

            pc.check(request);

            status.setDebug(getParamAsBoolean(request, "debug"));

            MusicDatabase mdb = DatabaseBroker.getMusicDatabase(context);

            if (mdb == null) {
                status.addError(ErrorCode.InternalError, "Can't connect to the music database");
                return;
            }
            go(request, out, mdb);

        } catch (AuraException ex) {
            status.addError(ErrorCode.InternalError, "Problem accessing data:" + ex, ex);
        } catch (RemoteException ex) {
            status.addError(ErrorCode.InternalError, "Problem contacting remote components:" + ex, ex);
        } catch (ParameterException e) {
            status.addErrors(e.getErrors());
        } finally {
            if (docType == DocType.StandardXML) {
                status.toXML(out);
                Util.tagClose(out, getServletName());
            } else if (docType == DocType.RawText) {
                if (!status.isOK()) {
                    out.println("# Error: " + status.toString());
                }
            }
            out.close();
        }

        long delta = System.currentTimeMillis() - startTime;
        DatabaseBroker.getStatsManager(context).addStats(request, getServletName(), status.isOK(), delta);
    }

    protected DocType getDocType() {
        return docType;
    }

    protected void setDocType(DocType docType) {
        this.docType = docType;
    }


    boolean isRaw() {
        return docType == DocType.RawText || docType == DocType.RawXML;
    }


    ItemFormatter getItemFormatter(ItemType type) {
        return DatabaseBroker.getItemFormatterManager(getServletContext()).getItemFormatter(type);
    }

    ItemFormatterManager getItemFormatterManager() {
        return DatabaseBroker.getItemFormatterManager(getServletContext());
    }

    protected void addParam(String name, String defaultValue, String description) {
        pc.addParam(name, defaultValue, description);
    }

    protected void addParam(String name, String description) {
        pc.addParam(name, description);
    }

    protected void addParam(String name, boolean required, String defaultValue, String description) {
        pc.addParam(name, required, defaultValue, description);
    }

    protected String getParam(ServletRequest request, String name) throws ParameterException {
        return pc.getParam(request, name);
    }

    protected int getParamAsInt(ServletRequest request, String name) throws ParameterException {
        return pc.getParamAsInt(request, name);
    }

    protected int getParamAsInt(ServletRequest request, String name, int min, int max) throws ParameterException {
        return pc.getParamAsInt(request, name, min, max);
    }

    protected Enum getParamAsEnum(ServletRequest request, String name, Enum[] vals) throws ParameterException {
        return pc.getParamAsEnum(request, name, vals);
    }

    protected boolean getParamAsBoolean(ServletRequest request, String name) throws ParameterException {
        return pc.getParamAsBoolean(request, name);
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
