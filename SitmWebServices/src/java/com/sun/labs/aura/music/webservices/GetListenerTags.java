/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.music.ArtistTag;
import com.sun.labs.aura.music.Listener;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.webservices.ItemFormatter.OutputType;
import com.sun.labs.aura.music.webservices.Util.ErrorCode;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Scored;
import com.sun.labs.aura.util.Tag;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author plamere
 */
public class GetListenerTags extends StandardService {

    private enum Type {

        Distinctive, Frequent
    };

    @Override
    public void initParams() {
        addParam("key", "the key of the listener of interest");
        addParam("max", "100", "the maxiumum number of results to return");
        addParam("type", "distinctive", "the type of tag report - 'distinctive' or 'frequent'");
        addParam("field", Listener.FIELD_SOCIAL_TAGS, "the field of interest.");
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
        ItemFormatterManager formatter = getItemFormatterManager();

        String key = getParam(request, "key");
        int maxCount = getParamAsInt(request, "max", 1, 250);
        String field = getParam(request, "field");
        // TBD Field not used yet.
        boolean frequent = ((Type) getParamAsEnum(request, "type", Type.values())) == Type.Frequent;
        OutputType outputType = (OutputType) getParamAsEnum(request, "outputType", OutputType.values());

        Listener listener = null;

        if ((listener = mdb.getListener(key)) != null) {
            if (frequent) {
                List<Tag> tags = listener.getSocialTags();
                for (Tag tag : tags) {
                    String tagName = tag.getName();
                    ArtistTag artistTag = mdb.artistTagLookup(ArtistTag.nameToKey(tagName));
                    if (artistTag != null) {
                        out.println(formatter.toXML(artistTag.getItem(), outputType, (double) tag.getFreq()));
                    }
                }
            } else {
                List<Scored<ArtistTag>> artistTags = mdb.listenerGetDistinctiveTags(key, maxCount);
                for (Scored<ArtistTag> sartistTag : artistTags) {
                    ArtistTag artistTag = sartistTag.getItem();
                    out.println("    <ListenerTag key=\"" + artistTag.getKey() + "\" " +
                            "score=\"" + sartistTag.getScore() + "\" " + "/>");
                    out.println(formatter.toXML(artistTag.getItem(), outputType, sartistTag.getScore()));
                }
            }
        } else {
            throw new ParameterException(ErrorCode.MissingArgument, "Can't find specified listener");
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
