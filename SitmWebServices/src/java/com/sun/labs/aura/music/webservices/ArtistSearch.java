/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.MusicDatabase;
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
public class ArtistSearch extends HttpServlet {

    private final static String SERVLET_NAME = "ArtistSearch";
    private ParameterChecker pc = null;

    public void init() throws ServletException {
        super.init();
        pc = new ParameterChecker(SERVLET_NAME, "searches the database for an artist");
        pc.addParam("name", "the name of the artist to search for");
        pc.addParam("max", "20", "the maximum number of matches to return");
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

        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();
        ServletContext context = getServletContext();


        try {
            Util.tagOpen(out, SERVLET_NAME);
            pc.check(status, request);

            String name = pc.getParam(status, request, "name");
            int maxCount = pc.getParamAsInt(status, request, "max", 1, 250);

            MusicDatabase mdb = DatabaseBroker.getMusicDatabase(context);
            if (mdb == null) {
                status.addError(ErrorCode.InternalError, "Can't find the datastore");
                return;
            }

            List<Scored<Artist>> scoredArtists = mdb.artistSearch(name, maxCount);
            for (Scored<Artist> scoredArtist : scoredArtists) {
                Artist artist = scoredArtist.getItem();
                out.println("    <artist key=\"" + artist.getKey() +
                        "\" " + "score=\"" + scoredArtist.getScore() + "\" " + "popularity=\"" + mdb.artistGetNormalizedPopularity(artist) + "\" " + "name=\"" + Util.filter(artist.getName()) + "\"" + "/>");
            }
        } catch (AuraException ex) {
            status.addError(ErrorCode.InternalError, "problem accessing data, " + ex.getMessage());
        } catch (ParameterException ex) {
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
        return "Searches the database for an artist with a particular name ";
    }
// </editor-fold>
}
