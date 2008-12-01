/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.ArtistTag;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.webservices.ItemFormatter.OutputType;
import com.sun.labs.aura.music.webservices.Util.ErrorCode;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.ScoredComparator;
import com.sun.labs.aura.util.Tag;
import com.sun.labs.aura.util.WordCloud;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author plamere
 */
public class GetArtistTags extends StandardService {
    private enum Type {
        Distinctive, Frequent
    };

    @Override
    public void initParams() {
        addParam("name", null, "the name of the item of interest");
        addParam("key", null, "the key of the item of interest");
        addParam("max", "100", "the maxiumum number of results to return");
        addParam("type", "distinctive", "the type of tag report - 'distinctive' or 'frequent'");
        addParam("field", Artist.FIELD_SOCIAL_TAGS, "the field of interest.");
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
            String key = getParam(request, "key");
            int maxCount = getParamAsInt(request, "max", 1, 250);
            String field = getParam(request, "field");
            // TBD Field not used yet.
            boolean frequent = ((Type) getParamAsEnum(request, "type", Type.values())) == Type.Frequent;
            OutputType outputType = (OutputType) getParamAsEnum(request, "outputType", OutputType.values());
            ItemFormatterManager formatter = getItemFormatterManager();

            Artist artist = null;
            if (key == null) {
                String name = getParam(request, "name");
                if (name != null) {
                    artist = mdb.artistFindBestMatch(name);
                    if (artist != null) {
                        key = artist.getKey();
                    }
                }
            }

            if (key != null) {
                if ((artist = mdb.artistLookup(key)) != null) {
                    if (frequent) {
                        List<Tag> tags = artist.getSocialTags();
                        for (Tag tag : tags) {
                            String tagName = tag.getName();
                            ArtistTag artistTag = mdb.artistTagLookup(ArtistTag.nameToKey(tagName));
                            if (artistTag != null) {
                                out.println(formatter.toXML(artistTag.getItem(), outputType, (double) tag.getFreq()));
                            }
                        }
                    } else {
                        WordCloud tags = mdb.artistGetDistinctiveTagNames(key, maxCount);
                        Map<String, Scored<String>> words = tags.getWords();
                        List<Scored<String>> vals = new ArrayList<Scored<String>>(words.values());
                        Collections.sort(vals, ScoredComparator.COMPARATOR);
                        Collections.reverse(vals);

                        for (Scored<String> scoredTag : vals) {
                            String tagName = scoredTag.getItem();
                            ArtistTag artistTag = mdb.artistTagLookup(ArtistTag.nameToKey(tagName));
                            if (artistTag != null) {
                                out.println(formatter.toXML(artistTag.getItem(), outputType, (double) scoredTag.getScore()));
                            }
                        }
                    }
                } else {
                    throw new ParameterException(ErrorCode.MissingArgument, "Can't find specified artist");
                }
            } else {
                throw new ParameterException(ErrorCode.MissingArgument, "need an artist  name or a key");
            }
    }
    /** 
     * Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo() {
        return "Gets the tags that have been applied to an artist";
    }
}
