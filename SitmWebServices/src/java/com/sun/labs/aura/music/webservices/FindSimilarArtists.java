/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.MusicDatabase.Popularity;
import com.sun.labs.aura.music.webservices.ItemFormatter.OutputType;
import com.sun.labs.aura.music.webservices.Util.ErrorCode;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import java.io.*;

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
        pc.addParam("outputType", OutputType.Tiny.name(), "the type of output");
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
            ItemFormatterManager formatter = DatabaseBroker.getItemFormatterManager(context);

            if (mdb == null || formatter == null) {
                status.addError(ErrorCode.InternalError, "Can't connect to the music database");
                return;
            }

            Set<Artist> artists = getArtistsFromRequest(mdb, status, request);
            List<String> keys = getKeyList(artists);
            int maxCount = pc.getParamAsInt(status, request, "max", 1, 250);
            Popularity pop = (Popularity) pc.getParamAsEnum(status, request,
                    "popularity", Popularity.values());
            String field = pc.getParam(status, request, "field");
            OutputType outputType = (OutputType) pc.getParamAsEnum(status, request, "outputType", OutputType.values());
                    

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
        } catch (AuraException ex) {
            status.addError(Util.ErrorCode.InternalError, "Problem accessing data", ex);
        } catch (ParameterException e) {
        } finally {
            status.toXML(out);
            Util.tagClose(out, SERVLET_NAME);
            out.close();
        }
    }

    Set<Artist> getArtistsFromRequest(MusicDatabase mdb, Status status, HttpServletRequest request) throws AuraException, ParameterException {
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
            status.addError(ErrorCode.BadArgument, errorMessage.toString());
            throw new ParameterException();
        }
        if (artists.size() == 0) {
            status.addError(ErrorCode.MissingArgument, "Need at least one key or name");
            throw new ParameterException();
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
