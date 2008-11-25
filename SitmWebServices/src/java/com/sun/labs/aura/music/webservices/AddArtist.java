/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
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
