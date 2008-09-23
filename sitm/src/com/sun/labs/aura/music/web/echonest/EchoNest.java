/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */

package com.sun.labs.aura.music.web.echonest;

import com.sun.labs.aura.music.web.Commander;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author plamere
 */
public class EchoNest {
    private Commander commander;
    private String API_KEY="EHY4JJEGIOFA1RCJP";
    private final static int MAX_ROWS = 15;
    
    public EchoNest() throws IOException {
        commander = new Commander("last.fm", "http://developer.echonest.com/api/", "&api_key=" + API_KEY);
        commander.setRetries(1);
        commander.setTimeout(10000);
        commander.setTraceSends(false);
        commander.setTrace(false);
        commander.setMinimumCommandPeriod(1000);
    }

    /**
     * Gets links to audio for an artist
     * @param mbaid the musicbrainz artist id
     * @return the (possibly empty) list of URLs to audio
     */
    public List<String> getAudio(String mbaid) throws IOException {
        return getAudio(mbaid, MAX_ROWS);
    }

    /**
     * Gets links to audio for an artist
     * @param mbaid the musicbrainz artist id
     * @param count the maxium number to return (total max is 15)
     * @return the (possibly empty) list of URLs to audio
     */
    public List<String> getAudio(String mbaid, int count) throws IOException {
        List<String> results = new ArrayList<String>();

        String cmdURL = "get_audio?mbid=" + mbaid + "&rows=" + count;

        Document doc = commander.sendCommand(cmdURL);
        Element docElement = doc.getDocumentElement();
        NodeList itemList = docElement.getElementsByTagName("url");
        for (int i = 0; i < itemList.getLength(); i++) {
            Element item = (Element) itemList.item(i);
            String url = item.getTextContent();
            if (url != null) {
                results.add(url);
            }
        }
        return results;
    }

    public static void dump(EchoNest nest, String mbid) throws IOException {
        for (String url : nest.getAudio(mbid, 15)) {
            System.out.printf("%s %s\n", mbid, url);
        }
    }

    public static void main(String[] args) throws IOException {
        EchoNest echoNest = new EchoNest();
        dump(echoNest, "11eabe0c-2638-4808-92f9-1dbd9c453429");
        dump(echoNest, "83d91898-7763-47d7-b03b-b92132375c47");
        dump(echoNest, "299278d3-25dd-4f30-bae4-5b571c28034d");
    }
}
