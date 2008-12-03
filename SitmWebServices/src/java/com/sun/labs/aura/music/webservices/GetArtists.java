/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.webservices.ItemFormatter.OutputType;
import com.sun.labs.aura.util.AuraException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author plamere
 */
public class GetArtists extends StandardService {

    @Override
    public void initParams() {
        addParam("max", "100", "the maximum number of results returned");
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
        int maxCount = getParamAsInt(request, "max", 1, 10000);
        OutputType outputType = (OutputType) getParamAsEnum(request, "outputType", OutputType.values());

        List<Artist> artists = mdb.artistGetMostPopular(maxCount);

        for (Artist artist : artists) {
            out.println(formatter.toXML(artist.getItem(), outputType, (double) mdb.artistGetNormalizedPopularity(artist)));
        }
    }

    /**
     * Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
