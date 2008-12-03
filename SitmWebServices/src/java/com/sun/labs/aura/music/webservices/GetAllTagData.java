/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.ArtistTag;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.Tag;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author plamere
 */
public class GetAllTagData extends StandardService {

    private enum Type {
        Distinctive, Frequent
    };

    @Override
    public String getServletInfo() {
        return "Gets bulk tag data for a batch of artists";
    }

    @Override
    public void initParams() {
        addParam("artistMax", "1000", "the maximum number of artists to return");
        addParam("tagMax", "500", "the maximum number of tags to return");
        addParam("type", "distinctive", "the type of tag report - 'distinctive' or 'frequent'");
        setDocType(DocType.RawText);
    }


    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void go(HttpServletRequest request, PrintWriter out, MusicDatabase mdb) throws AuraException, ParameterException {
        out.println("# " + getServletName());
        int artistMax = getParamAsInt(request, "artistMax", 1, 100000);
        int tagMax = getParamAsInt(request, "tagMax", 1, 10000);
        boolean frequent = ((Type) getParamAsEnum(request, "type", Type.values())) == Type.Frequent;

        List<Artist> artists = mdb.artistGetMostPopular(artistMax);
        List<ArtistTag> tags = mdb.artistTagGetMostPopular(tagMax);
        Map<String, Integer> tagIndexMap = new HashMap<String, Integer>();

        for (int i = 0; i < tags.size(); i++) {
            ArtistTag artistTag = tags.get(i);
            float popularity = mdb.artistTagGetNormalizedPopularity(artistTag);
            tagIndexMap.put(artistTag.getName(), i);
            out.printf("tag %d %.4f %s\n", i + 1, popularity, artistTag.getName());
        }

        for (int i = 0; i < artists.size(); i++) {
            Artist artist = artists.get(i);
            float popularity = mdb.artistGetNormalizedPopularity(artist);
            out.printf("artist %d %.4f %s %s\n", i + 1, popularity, artist.getKey(), artist.getName());
        }

        int missedTags = 0;
        for (int i = 0; i < artists.size(); i++) {
            double[] scores = new double[tags.size()];
            Artist artist = artists.get(i);
            if (frequent) {
                List<Tag> tagList = artist.getSocialTags();
                for (Tag tag : tagList) {
                    Integer index = tagIndexMap.get(tag.getName());
                    if (index == null) {
                        missedTags++;
                    } else {
                        scores[index] = tag.getCount();
                    }
                }
            } else {
                List<Scored<ArtistTag>> artistTags = mdb.artistGetDistinctiveTags(artist.getKey(), tags.size());
                for (Scored<ArtistTag> sartistTag : artistTags) {
                    Integer index = tagIndexMap.get(sartistTag.getItem().getName());
                    if (index == null) {
                        missedTags++;
                    } else {
                        scores[index] = sartistTag.getScore();
                    }
                }
            }

            for (int j = 0; j < scores.length; j++) {
                out.printf("%.3f ", scores[j]);
            }
            out.println();
        }
        out.println("# Missed tags: " + missedTags);
        out.flush();
    }
}