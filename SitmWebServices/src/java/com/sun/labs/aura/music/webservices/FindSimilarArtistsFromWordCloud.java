/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.MusicDatabase.Popularity;
import com.sun.labs.aura.music.webservices.ItemFormatter.OutputType;
import com.sun.labs.aura.music.webservices.Util.ErrorCode;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.WordCloud;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author plamere
 */
public class FindSimilarArtistsFromWordCloud extends StandardService {

    @Override
    public void initParams() {
        addParam("wordCloud", "the wordcloud");
        addParam("max", "10", "the maxiumum number of artists to return");
        addParam("popularity", Popularity.ALL.name(), "the popularity filter");
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
        String wc = getParam(request, "wordCloud");
        int maxCount = getParamAsInt(request, "max", 1, 250);
        Popularity pop = (Popularity) getParamAsEnum(request, "popularity", Popularity.values());
        WordCloud cloud = WordCloud.convertStringToWordCloud(wc);
        if (cloud == null) {
            throw new ParameterException(ErrorCode.BadArgument, "Bad wordcloud format. Should be:" +
                    "(tag1 name,weight)(tag2 name,weight)");
        }
        OutputType outputType = (OutputType) getParamAsEnum(request, "outputType", OutputType.values());

        List<Scored<Artist>> scoredArtists = mdb.wordCloudFindSimilarArtists(cloud, maxCount, pop);
        for (Scored<Artist> scoredArtist : scoredArtists) {
            out.println(formatter.toXML(scoredArtist.getItem().getItem(), outputType, scoredArtist.getScore()));
        }
    }

    /**
     * Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo() {
        return "Finds artists that are similar to a word cloud ";
    }
}
