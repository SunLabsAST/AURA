/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.MusicDatabase.Popularity;
import com.sun.labs.aura.music.webservices.Util.ErrorCode;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import java.io.*;

import java.util.List;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author plamere
 */
public class FindSimilarArtists extends HttpServlet {

    private final static String SERVLET_NAME = "FindSimilarArtists";
    private ParameterChecker pc;

    @Override
    public void init() throws ServletException {
        super.init();
        pc = new ParameterChecker(SERVLET_NAME, "find artists similar to a seed artist");
        pc.addParam("key", null, "the key of the item of interest");
        pc.addParam("name", null, "the name of the item of interest");
        pc.addParam("max", "10", "the maxiumum number of artists to return");
        pc.addParam("popularity", Popularity.ALL.name(), "the popularity filter");
        pc.addParam("field", Artist.FIELD_SOCIAL_TAGS, "the field to use for similarity");
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

            MusicDatabase mdb = (MusicDatabase) context.getAttribute("MusicDatabase");

            if (mdb == null) {
                status.addError(ErrorCode.InternalError, "Can't connecto to the music database");
                return;
            }

            String key = pc.getParam(status, request, "key");
            int maxCount = pc.getParamAsInt(status, request, "max", 1, 250);
            Popularity pop = (Popularity) pc.getParamAsEnum(status, request,
                    "popularity", Popularity.values());
            String field = pc.getParam(status, request, "field");

            Artist artist = null;
            if (key == null) {
                String name = pc.getParam(status, request, "name");
                if (name != null) {
                    artist = mdb.artistFindBestMatch(name);
                    if (artist != null) {
                        key = artist.getKey();
                    }
                }
            }
            if (key == null) {
                status.addError(ErrorCode.NotFound, "Can't find artist");
                return;
            }

            if ((artist = mdb.artistLookup(key)) != null) {
                List<Scored<Artist>> scoredArtists;
                if ("all".equals(field)) {
                    scoredArtists = mdb.artistFindSimilar(key, maxCount + 1, pop);
                } else {
                    scoredArtists = mdb.artistFindSimilar(key, field, maxCount + 1, pop);
                }

                out.println("    <seed key=\"" + key + "\" name=\"" + Util.filter(artist.getName()) + "\"/>");
                for (Scored<Artist> scoredArtist : scoredArtists) {

                    if (scoredArtist.getItem().getKey().equals(key)) {
                        continue;
                    }

                    Artist simArtist = scoredArtist.getItem();
                    out.println("    <artist key=\"" +
                            simArtist.getKey() + "\" " +
                            "score=\"" + scoredArtist.getScore() + "\" " +
                            "name=\"" + Util.filter(simArtist.getName()) + "\"" +
                            "/>");
                }
            } else {
                status.addError(Util.ErrorCode.NotFound, "Can't find specified artist");
            }
        } catch (AuraException ex) {
            status.addError(Util.ErrorCode.InternalError, "Problem accessing data");
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
        return "Finds artists that are similar to a seed artist";
    }
    // </editor-fold>
}
