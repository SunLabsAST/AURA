/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.music.Listener;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.webservices.Util.ErrorCode;
import com.sun.labs.aura.util.AuraException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author plamere
 */
public class AddListener extends StandardService {

    @Override
    public void initParams() {
        addParam("appKey", "the application key");
        addParam("userKey", "the key of the user");
        addParam("lastfmName", null, "the lastfm name of the user");
        addParam("pandoraName", null, "the pandora name of the user");
    }

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void go(HttpServletRequest request, PrintWriter out, MusicDatabase mdb)
            throws AuraException, ParameterException, RemoteException {
        String userKey = getParam(request, "userKey");
        String lastfmName = getParam(request, "lastfmName");
        String pandoraName = getParam(request, "pandoraName");


        if (mdb.getListener(userKey) != null) {
            new ParameterException(ErrorCode.BadArgument, "userKey already exists");
        }

        Listener listener = mdb.enrollListener(userKey);

        if (lastfmName != null) {
            listener.setLastFmName(lastfmName);
        }

        if (pandoraName != null) {
            listener.setPandoraName(pandoraName);
        }

        mdb.updateListener(listener);
    }
    /** 
     * Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo() {
        return "Adds a listener ot the database";
    }
}
