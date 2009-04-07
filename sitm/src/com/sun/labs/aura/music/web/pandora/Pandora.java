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

package com.sun.labs.aura.music.web.pandora;

import com.sun.labs.aura.music.web.Commander;
import com.sun.labs.aura.music.web.XmlUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author plamere
 */
public class Pandora {

    private Commander commander;

    public Pandora() throws IOException {
        commander = new Commander("pandora", "http://feeds.pandora.com/", "");
        commander.setRetries(1);
        commander.setTimeout(10000);
        commander.setTraceSends(false);
        commander.setMinimumCommandPeriod(1000);
    }

    public List<String> getFavoriteArtistNamesForUser(String pandoraUser) throws IOException {
        String url = "feeds/people/" + pandoraUser + "/favoriteartists.xml";
        List<String> artists = new ArrayList();

        Document doc = commander.sendCommand(url);
        Element docElement = doc.getDocumentElement();
        NodeList itemList = docElement.getElementsByTagName("item");
        for (int i = 0; i < itemList.getLength(); i++) {
            Element item = (Element) itemList.item(i);

            String artistName = XmlUtil.getElementContents(item, "title");
            artists.add(artistName);
        }
        return artists;
    }

    public static void main(String[] args) throws Exception {
        Pandora pandora = new Pandora();
        List<String> artists = pandora.getFavoriteArtistNamesForUser("paul.lamere");
        for (String artist : artists) {
            System.out.println(" " + artist);
        }

        artists = pandora.getFavoriteArtistNamesForUser("bad.lamere");
        for (String artist : artists) {
            System.out.println(" " + artist);
        }
    }
}
