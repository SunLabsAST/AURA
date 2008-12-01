/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.MusicDatabase.Popularity;
import com.sun.labs.aura.music.webservices.ItemFormatter.OutputType;
import com.sun.labs.aura.music.webservices.Util.ErrorCode;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import java.io.*;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author plamere
 */
public class FindSimilarArtists extends StandardService {

    @Override
    public void initParams() {
        addParam("key", null, "the key of the item of interest");
        addParam("name", null, "the name of the item of interest");
        addParam("max", "10", "the maxiumum number of artists to return");
        addParam("popularity", Popularity.ALL.name(), "the popularity filter");
        addParam("field", Artist.FIELD_SOCIAL_TAGS, "the field to use for similarity");
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

        Set<Artist> artists = getArtistsFromRequest(mdb, request);
        List<String> keys = getKeyList(artists);
        int maxCount = getParamAsInt(request, "max", 1, 250);
        Popularity pop = (Popularity) getParamAsEnum(request, "popularity", Popularity.values());
        String field = getParam(request, "field");
        OutputType outputType = (OutputType) getParamAsEnum(request, "outputType", OutputType.values());
        ItemFormatterManager formatter = getItemFormatterManager();
        List<Scored<Artist>> scoredArtists;

        if ("all".equals(field)) {
            scoredArtists = mdb.artistFindSimilar(keys, maxCount + keys.size(), pop);
        } else {
            scoredArtists = mdb.artistFindSimilar(keys, field, maxCount + keys.size(), pop);
        }

        for (Artist artist : artists) {
            out.println("    <seed key=\"" + artist.getKey() + "\" name=\"" + Util.filter(artist.getName()) + "\"/>");
        }

        for (Scored<Artist> scoredArtist : scoredArtists) {
            if (keys.contains(scoredArtist.getItem().getKey())) {
                continue;
            }
            Artist simArtist = scoredArtist.getItem();
            out.println(formatter.toXML(simArtist.getItem(), outputType, scoredArtist.getScore()));
        }
    }

    Set<Artist> getArtistsFromRequest(MusicDatabase mdb, HttpServletRequest request) throws AuraException, ParameterException {
        Set<Artist> artists = new HashSet<Artist>();
        StringBuilder errorMessage = new StringBuilder();

        String[] pkeys = request.getParameterValues("key");

        if (pkeys != null) {
            for (String key : pkeys) {
                Artist artist = mdb.artistLookup(key);
                if (artist != null) {
                    artists.add(artist);
                } else {
                    addMessage(errorMessage, "bad key: " + key);
                }
            }
        }
        String[] names = request.getParameterValues("name");
        if (names != null) {
            for (String name : names) {
                Artist artist = mdb.artistFindBestMatch(name);
                if (artist != null) {
                    artists.add(artist);
                } else {
                    addMessage(errorMessage, "bad name: " + name);
                }
            }
        }

        if (errorMessage.length() > 0) {
            throw new ParameterException(ErrorCode.BadArgument, errorMessage.toString());
        }

        if (artists.size() == 0) {
            throw new ParameterException(ErrorCode.MissingArgument, "Need at least one key or name");
        }
        return artists;
    }

    List<String> getKeyList(Collection<Artist> artists) {
        List<String> keys = new ArrayList<String>();
        for (Artist artist : artists) {
            keys.add(artist.getKey());
        }
        return keys;
    }

    private void addMessage(StringBuilder sb, String msg) {
        if (sb.length() > 0) {
            sb.append(", ");
        }
        sb.append(msg);
    }

    /** 
     * Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo() {
        return "Finds artists that are similar to a seed artist";
    }
    // 
}
