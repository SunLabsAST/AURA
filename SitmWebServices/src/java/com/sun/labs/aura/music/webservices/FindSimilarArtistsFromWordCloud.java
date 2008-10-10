/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.MusicDatabase.Popularity;
import com.sun.labs.aura.music.webservices.Util.ErrorCode;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.WordCloud;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author plamere
 */
public class FindSimilarArtistsFromWordCloud extends HttpServlet {

    private final static String SERVLET_NAME = "FindSimilarArtistFromWordCloud";
    private ParameterChecker pc;

    @Override
    public void init() throws ServletException {
        super.init();
        pc = new ParameterChecker(SERVLET_NAME, "find artists similar to a wordcloud");
        pc.addParam("wordcloud", "the wordcloud");
        pc.addParam("max", "10", "the maxiumum number of artists to return");
        pc.addParam("popularity", Popularity.ALL.name(), "the popularity filter");
    }

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (pc.processDocumentationRequest(request, response)) {
            return;
        }

        Status status = new Status(request);
        ServletContext context = getServletContext();
        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            Util.tagOpen(out, SERVLET_NAME);
            pc.check(status, request);
            MusicDatabase mdb = DatabaseBroker.getMusicDatabase(context);

            if (mdb == null) {
                status.addError(ErrorCode.InternalError, "Can't connect to the music database");
                return;
            }

            String wc = pc.getParam(status, request, "wordCloud");
            int maxCount = pc.getParamAsInt(status, request, "max", 1, 250);
            Popularity pop = (Popularity) pc.getParamAsEnum(status, request,
                    "popularity", Popularity.values());

            WordCloud cloud = WordCloud.convertStringToWordCloud(wc);
            if (cloud == null) {
                status.addError(ErrorCode.BadArgument, "Bad wordcloud format. Should be:" +
                        "(tag1 name,weight)(tag2 name,weight)");
                return;
            }


            List<Scored<Artist>> scoredArtists = mdb.wordCloudFindSimilarArtists(cloud, maxCount, pop);
            for (Scored<Artist> scoredArtist : scoredArtists) {
                Artist simArtist = scoredArtist.getItem();
                out.println("    <artist key=\"" +
                        simArtist.getKey() + "\" " +
                        "score=\"" + scoredArtist.getScore() + "\" " +
                        "name=\"" + Util.filter(simArtist.getName()) + "\"" +
                        "/>");
            }
        } catch (AuraException ex) {
            status.addError(ErrorCode.InternalError, "Problem accessing data " + ex);
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
        return "Finds artists that are similar to a word cloud ";
    }// </editor-fold>
}
