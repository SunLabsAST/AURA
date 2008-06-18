/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
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
