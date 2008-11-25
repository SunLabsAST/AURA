/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
