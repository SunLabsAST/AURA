/*
 * Copyright 2007-2009 Sun Microsystems, Inc. All Rights Reserved.
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

import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.webservices.ItemFormatter.OutputType;
import com.sun.labs.aura.music.webservices.Util.ErrorCode;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import java.io.*;

import java.rmi.RemoteException;
import java.util.List;
import javax.servlet.http.*;

/**
 *
 * @author plamere
 */
public class ArtistSearch extends StandardService {

    @Override
    public void initParams() {
        addParam("name", "the name of the artist to search for");
        addParam("max", "20", "the maximum number of matches to return");
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
        String name = getParam(request, "name");
        int maxCount = getParamAsInt(request, "max", 1, 250);
        OutputType outputType = (OutputType) getParamAsEnum(request, "outputType", OutputType.values());
        ItemFormatterManager formatter = getItemFormatterManager();
        List<Scored<Artist>> scoredArtists = mdb.artistSearch(name, maxCount);
        for (Scored<Artist> scoredArtist : scoredArtists) {
            out.println(formatter.toXML(scoredArtist.getItem().getItem(), outputType, scoredArtist.getScore()));
        }
    }

    /** 
     * Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo() {
        return "Searches the database for an artist with a particular name ";
    }
}
