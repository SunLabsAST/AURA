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

import com.sun.labs.aura.music.Listener;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.webservices.ItemFormatter.OutputType;
import com.sun.labs.aura.music.webservices.Util.ErrorCode;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author plamere
 */
public class FindSimilarListeners extends StandardService {

    @Override
    public void initParams() {
        addParam("key", "the key of the item of interest");
        addParam("max", "10", "the maxiumum number of artists to return");
        addParam("field", Listener.FIELD_SOCIAL_TAGS, "the field to use for similarity");
        addParam("outputType", OutputType.Tiny.name(), "the type of output");
    }

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void go(HttpServletRequest request, PrintWriter out, MusicDatabase mdb)
            throws AuraException, ParameterException, RemoteException {
        ItemFormatterManager formatter = getItemFormatterManager();
        int maxCount = getParamAsInt(request, "max", 1, 250);
        String key = getParam(request, "key");
        String field = getParam(request, "field"); //TBD field is not used yet.
        OutputType outputType = (OutputType) getParamAsEnum(request, "outputType", OutputType.values());
        Listener listener = mdb.getListener(key);
        if (listener != null) {
            List<Scored<Listener>> similarListeners = mdb.listenerFindSimilar(key, maxCount);
            for (Scored<Listener> scoredListener : similarListeners) {

                if (scoredListener.getItem().getKey().equals(key)) {
                    continue;
                }

                out.println(formatter.toXML(scoredListener.getItem().getItem(), outputType, scoredListener.getScore()));
            }
        } else {
            throw new ParameterException(ErrorCode.BadArgument, "Can't find user with key " + key);
        }
    }

    /** 
     * Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo() {
        return "Finds listeners that are similar to a seed listener. ";
    }
}
