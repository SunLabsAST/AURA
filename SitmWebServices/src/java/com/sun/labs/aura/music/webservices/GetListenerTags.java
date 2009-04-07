/*
 * Copyright 2008-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
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
