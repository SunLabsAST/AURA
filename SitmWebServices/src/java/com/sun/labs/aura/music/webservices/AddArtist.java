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

import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.MusicDatabase.DBOperation;
import com.sun.labs.aura.music.webservices.Util.ErrorCode;
import com.sun.labs.aura.util.AuraException;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author plamere
 */
public class AddArtist extends StandardService {

    @Override
    public void initParams() {
        addParam("appKey", "the application key");
        addParam("mbaid", "the musicbrainz ID of the new artist");
    }

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void go(HttpServletRequest request, PrintWriter out, MusicDatabase mdb) throws AuraException,
            ParameterException, RemoteException {
        String appKey = getParam(request, "appKey");
        String mbaid = getParam(request, "mbaid");

        if (!mdb.isValidApplication(appKey)) {
            throw new ParameterException(ErrorCode.BadArgument, "not a valid application");
        }

        if (!mdb.hasAuthorization(appKey, DBOperation.AddItem)) {
            throw new ParameterException(ErrorCode.BadArgument, "application not authorized to add artists");
        }

        if (mdb.artistLookup(mbaid) != null) {
            throw new ParameterException(ErrorCode.BadArgument, "artist already exists");
        }

        mdb.addArtist(mbaid);
    }

    /** 
     * Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Adds an artist to the database";
    }
}
