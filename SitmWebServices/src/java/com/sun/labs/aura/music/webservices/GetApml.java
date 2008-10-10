/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.Listener;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.web.apml.APML;
import com.sun.labs.aura.music.web.apml.Concept;
import com.sun.labs.aura.music.web.apml.Profile;
import com.sun.labs.aura.music.webservices.Util.ErrorCode;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Tag;
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
public class GetApml extends HttpServlet {

    private final static String SERVLET_NAME = "GetApml";
    private ParameterChecker pc;

    private enum Format {

        Artist, MBaid
    };

    @Override
    public void init() throws ServletException {
        super.init();
        pc = new ParameterChecker(SERVLET_NAME, "gets the APML for a listener");
        pc.addParam("userKey", "the key the user of interest");
        pc.addParam("max", "10", "the maximum number of concepts returned");
        pc.addParam("format", "artist", "the format of the output");
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
            pc.check(status, request);
            MusicDatabase mdb = DatabaseBroker.getMusicDatabase(context);

            if (mdb == null) {
                status.addError(ErrorCode.InternalError, "Can't connect to the music database");
                return;
            }

            String key = pc.getParam(status, request, "userKey");
            int maxCount = pc.getParamAsInt(status, request, "max", 1, 250);
            boolean showArtistNames = pc.getParamAsEnum(status, request, "format", Format.values()) == Format.Artist;

            Listener listener = mdb.getListener(key);
            if (listener != null) {
                Concept[] explicitConcepts = getArtistNameConcepts(mdb, showArtistNames,
                        listener.getFavoriteArtist(), maxCount);
                Concept[] implicitConcepts = getConcepts(listener.getSocialTags(), maxCount);
                Profile profile = new Profile("music", implicitConcepts, explicitConcepts);
                APML apml = new APML("taste data for user " + listener.getKey());
                apml.addProfile(profile);
                out.println(apml.toString());
            } else {
                status.addError(ErrorCode.InvalidKey, "Can't find listener with key " + key);
            }
        } catch (AuraException ex) {
            status.addError(ErrorCode.InternalError, "Problem accessing data " + ex);
        } catch (ParameterException ex) {
        } finally {
            if (!status.isOK()) {
                Util.tagOpen(out, SERVLET_NAME);
                status.toXML(out);
                Util.tagClose(out, SERVLET_NAME);
            }
            out.close();
        }
    }

    private Concept[] getConcepts(List<Tag> tags, int maxCount) {
        int size = tags.size() > maxCount ? maxCount : tags.size();
        Concept[] concepts = new Concept[size];

        float max = getMaxFreq(tags);

        for (int i = 0; i < size; i++) {
            Tag tag = tags.get(i);
            concepts[i] = new Concept(tag.getName(), tag.getFreq() / max);
        }
        return concepts;
    }

    private Concept[] getArtistNameConcepts(MusicDatabase mdb, boolean useArtistName,
            List<Tag> tags, int maxCount) throws AuraException {

        int size = tags.size() > maxCount ? maxCount : tags.size();
        Concept[] concepts = new Concept[size];

        float max = getMaxFreq(tags);

        for (int i = 0; i < size; i++) {
            Tag tag = tags.get(i);
            Artist artist = mdb.artistLookup(tag.getName());
            if (artist != null) {
                String name = useArtistName ? artist.getName() : tag.getName();
                String annotation = "mbaid for " + artist.getName() + " is " + tag.getName();
                concepts[i] = new Concept(name, tag.getFreq() / max, annotation);
            }
        }
        return concepts;
    }

    private float getMaxFreq(List<Tag> tags) {
        float max = -Float.MAX_VALUE;

        for (Tag tag : tags) {
            if (tag.getFreq() > max) {
                max = tag.getFreq();
            }
        }
        return max;
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
    @Override
    public String getServletInfo() {
        return "Gets the APML markup for a listener";
    }// </editor-fold>
}
