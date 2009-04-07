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

import com.sun.labs.aura.datastore.Attention;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.MusicDatabase.DBOperation;
import com.sun.labs.aura.music.webservices.Util.ErrorCode;
import com.sun.labs.aura.util.AuraException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author plamere
 */
public class AddAttentionData extends StandardService {

    @Override
    public void initParams() {
        addParam("appKey", "the application key");
        addParam("srcKey", "the key of the attention source");
        addParam("tgtKey", "the key of the attentin target");
        addParam("type", "the type of the attention");
        addParam("value", null, "the optional value associated with the attention type");
    }

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void go(HttpServletRequest request, PrintWriter out, MusicDatabase mdb)
            throws AuraException, ParameterException, RemoteException {

        String appKey = getParam(request, "appKey");
        String srcKey = getParam(request, "srcKey");
        String destKey = getParam(request, "tgtKey");
        Attention.Type type = (Attention.Type) getParamAsEnum(request, "type", Attention.Type.values());
        String value = getParam(request, "value");

        if (!mdb.isValidApplication(appKey)) {
            throw new ParameterException(ErrorCode.BadArgument, "not a valid application");
        }

        if (!mdb.hasAuthorization(appKey, DBOperation.AddAttention)) {
            throw new ParameterException(ErrorCode.NotAuthorized, "application not authorized to add attention data");
        }
        mdb.addAttention(srcKey, destKey, type, value);
    }

    /**
     * Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo() {
        return "Adds attention to the datastore";
    }
}
