/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.ArtistTag;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.webservices.Util.ErrorCode;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.Tag;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author plamere
 */
public class GetAllTagData extends HttpServlet {

    private final static String SERVLET_NAME = "GetAllTagData";

    private enum Type {

        Distinctive, Frequent
    };
    private ParameterChecker pc;

    @Override
    public void init() throws ServletException {
        super.init();
        pc = new ParameterChecker(SERVLET_NAME, "Gets bulk tag data for a batch of artists");
        pc.addParam("artistMax", "1000", "the maximum number of artists to return");
        pc.addParam("tagMax", "500", "the maximum number of tags to return");
        pc.addParam("type", "distinctive", "the type of tag report - 'distinctive' or 'frequent'");
    }

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ServletContext context = getServletContext();

        if (pc.processDocumentationRequest(request, response)) {
            return;
        }

        Status status = new Status(request);

        response.setContentType("text/plain;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            pc.check(status, request);
            MusicDatabase mdb = DatabaseBroker.getMusicDatabase(context);

            if (mdb == null) {
                status.addError(ErrorCode.InternalError, "Can't connect to the music database");
                return;
            }

            int artistMax = pc.getParamAsInt(status, request, "artistMax", 1, 100000);
            int tagMax = pc.getParamAsInt(status, request, "tagMax", 1, 10000);
            boolean frequent = ((Type) pc.getParamAsEnum(status, request, "type", Type.values())) == Type.Frequent;

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
        } catch (AuraException ex) {
            status.addError(ErrorCode.InternalError, "Problem accessing data:" + ex, ex);
        } catch (ParameterException e) {
        } finally {
            status.toXML(out);
            Util.tagClose(out, SERVLET_NAME);
            out.close();
        }
    }
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
