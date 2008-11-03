/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.music.ArtistTag;
import com.sun.labs.aura.music.Listener;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.webservices.Util.ErrorCode;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.ScoredComparator;
import com.sun.labs.aura.util.Tag;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
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
public class GetListenerTags extends HttpServlet {

    private final static String SERVLET_NAME = "GetListenerTags";

    private enum Type {

        Distinctive, Frequent
    };
    private ParameterChecker pc;

    @Override
    public void init() throws ServletException {
        super.init();
        pc = new ParameterChecker(SERVLET_NAME, "Gets the tags associated with a listener");
        pc.addParam("key", "the key of the listener of interest");
        pc.addParam("max", "100", "the maxiumum number of results to return");
        pc.addParam("type", "distinctive", "the type of tag report - 'distinctive' or 'frequent'");
        pc.addParam("field", Listener.FIELD_SOCIAL_TAGS, "the field of interest.");
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

            String key = pc.getParam(status, request, "key");
            int maxCount = pc.getParamAsInt(status, request, "max", 1, 250);
            String field = pc.getParam(status, request, "field");
            // TBD Field not used yet.
            boolean frequent = ((Type) pc.getParamAsEnum(status, request, "type", Type.values())) == Type.Frequent;

            Listener listener = null;

            if ((listener = mdb.getListener(key)) != null) {
                if (frequent) {
                    List<Tag> tags = listener.getSocialTags();
                    for (Tag tag : tags) {
                        String tagKey = tag.getName();
                        out.println("    <ListenerTag key=\"" + tagKey + "\" " + "score=\"" + tag.getCount() + "\" " + "/>");
                    }
                } else {
                    List<Scored<ArtistTag>> artistTags = mdb.listenerGetDistinctiveTags(key, maxCount);
                    for (Scored<ArtistTag> sartistTag : artistTags) {
                        ArtistTag artistTag = sartistTag.getItem();
                        out.println("    <ListenerTag key=\"" + artistTag.getKey() + "\" " +
                                "score=\"" + sartistTag.getScore() + "\" " + "/>");
                    }
                }
            } else {
                status.addError(ErrorCode.MissingArgument, "Can't find specified listener");
            }
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
        return "Gets the tags that have been applied to an artist";
    }// </editor-fold>
}
