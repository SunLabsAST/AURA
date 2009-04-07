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
import com.sun.labs.aura.music.RecommendationType;
import com.sun.labs.aura.util.AuraException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author plamere
 */
public class GetRecommendationTypes extends StandardService {

    @Override
    public void initParams() {
    }

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void go(HttpServletRequest request, PrintWriter out, MusicDatabase mdb)
            throws AuraException, ParameterException, RemoteException {
        List<RecommendationType> rtypes = mdb.getArtistRecommendationTypes();
        Util.tag(out, "default", mdb.getDefaultArtistRecommendationType().getName());
        for (RecommendationType rtype : rtypes) {
            out.println("    <RecommendationType>");
            out.println("        <name>" + rtype.getName() + "</name>");
            out.println("        <description>" + rtype.getDescription() + "</description>");
            out.println("        <type>" + rtype.getType().name() + "</type>");
            out.println("    </RecommendationType>");
        }
    }
    /** 
     * Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo() {
        return "Gets the list of supported recommendation types";
    }
}
