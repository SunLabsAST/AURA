/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.attention;

import com.sun.labs.search.music.minidatabase.Artist;
import com.sun.labs.search.music.minidatabase.MusicDatabase;
import com.sun.labs.search.music.minidatabase.Scored;
import com.sun.labs.search.music.web.apml.APML;
import com.sun.labs.search.music.web.apml.APMLLoader;
import com.sun.labs.search.music.web.apml.Concept;
import com.sun.labs.search.music.web.apml.Profile;
import java.io.*;
import java.net.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 *
 * @author plamere
 */
public class RecommenderService extends HttpServlet {

    private MusicDatabase mdb;
    private APMLLoader loader;
    private Set algSet = new HashSet<String>();
    private int requestCount = 0;
    private int totalRecommendations = 0;

    @Override
    public void init(ServletConfig sc) throws ServletException {
        try {
            super.init(sc);

            String path = sc.getServletContext().getInitParameter("dbPath");
            mdb = new MusicDatabase(path);
            loader = new APMLLoader();

            // supported algorithms
            algSet.add("exp1");
            algSet.add("exp2");
            algSet.add("imp1");
            algSet.add("default");

        } catch (IOException ex) {
            throw new ServletException("Can't find database", ex);
        }
    }

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     */
    // request is of form:
    //     RecommenderService?apmlURL=url&profile=music&outputFormat=apml&num=30&alg=ex1
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        URL apmlURL = null;
        int num = 30;

        requestCount++;

        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            String apmlSURL = request.getParameter("apmlURL");

            if (apmlSURL == null) {
                response.sendError(response.SC_BAD_REQUEST, "missing required parameter apmlURL");
                return;
            }

            try {
                apmlURL = new URL(apmlSURL);
            } catch (MalformedURLException ex) {
                response.sendError(response.SC_BAD_REQUEST, "bad apmlURL format " + apmlSURL);
                return;
            }

            String outputFormat = request.getParameter("outputFormat");
            if (outputFormat == null) {
                outputFormat = "apml";
            }

            if (!isSupportedOutputFormat(outputFormat)) {
                response.sendError(response.SC_BAD_REQUEST, "unknown outputFormat " + outputFormat);
                return;
            }

            String algorithm = request.getParameter("alg");
            if (algorithm == null) {
                algorithm = "default";
            }

            if (!isSupportedAlgorithm(algorithm)) {
                response.sendError(response.SC_BAD_REQUEST, "unknown algorithm " + algorithm);
                return;
            }

            String type = request.getParameter("type");
            if (type == null) {
                type = "artist";
            }

            if (!isSupportedType(type)) {
                response.sendError(response.SC_BAD_REQUEST, "unknown recommendation type " + type);
                return;
            }

            String snum = request.getParameter("num");
            if (snum == null) {
                snum = "30";
            }

            try {
                num = Integer.parseInt(snum);
            } catch (NumberFormatException ex) {
                response.sendError(response.SC_BAD_REQUEST, "Bad format for 'num' parameter " + snum);
                return;
            }

            if (num < 1 || num > 250) {
                response.sendError(response.SC_BAD_REQUEST, "'num' parameter out of range 1 - 250 ");
                return;
            }

            APML apml = loader.loadAPML(apmlURL);

            String profileName = request.getParameter("profile");
            if (profileName == null) {
                profileName = apml.getDefaultProfile();
            }

            if (profileName == null) {
                response.sendError(response.SC_BAD_REQUEST, "No profile name specified and APML has no defaultprofile");
                return;
            }


            Profile profile = apml.getProfile(profileName);
            if (profile == null) {
                response.sendError(response.SC_BAD_REQUEST, "Can't find profile " + profileName);
                return;
            }

            // TODO: Support other types besides 'artists'
            List<Scored<Artist>> recommendedArtists = mdb.recommendArtists(profile, algorithm, num);

            Profile recommendationProfile = new Profile("Music-Recommendations", getConcepts(recommendedArtists), Profile.EMPTY_CONCEPTS);

            apml.addProfile(recommendationProfile);
            out.println(apml.toString());
            totalRecommendations += recommendedArtists.size();

            out.println("<!-- recommendations generated by tastebroker.org -->");
            out.println("<!-- tastebroker stats: requests: " + requestCount + " recommendations: " + totalRecommendations + " -->");
        } catch (IOException ex) {
            response.sendError(response.SC_BAD_REQUEST, "Can't load APML " + apmlURL);
            return;
        } finally {
            out.close();
        }
    }

    private Concept[] getConcepts(List<Scored<Artist>> artists) {
        double highScore = 0;
        for (Scored<Artist> scoredArtist : artists) {
            if (scoredArtist.getScore() > highScore) {
                highScore = scoredArtist.getScore();
            }
        }


        Concept[] concepts = new Concept[artists.size()];
        int index = 0;
        for (Scored<Artist> scoredArtist : artists) {
            concepts[index++] = new Concept(scoredArtist.getItem().getName(), (float) (scoredArtist.getScore() / highScore));
        }
        return concepts;
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     */
    public String getServletInfo() {
        return "Short description";
    }

    private boolean isSupportedOutputFormat(String outputFormat) {
        return outputFormat.equals("apml");
    }

    private boolean isSupportedType(String type) {
        return type.equals("artist");
    }

    private boolean isSupportedAlgorithm(String alg) {
        return algSet.contains(alg);
    }
    // </editor-fold>
}
