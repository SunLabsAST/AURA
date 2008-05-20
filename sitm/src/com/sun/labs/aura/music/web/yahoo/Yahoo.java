/*
 * Yahoo.java
 *
 * Created on April 8, 2007, 7:26 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.web.yahoo;

import com.sun.labs.aura.music.web.Commander;
import com.sun.labs.aura.music.web.XmlUtil;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author plamere
 */
public class Yahoo {
    private final static String APP_ID = "FrodoBaggins";
    Commander commander;
    
    /** Creates a new instance of Yahoo */
    public Yahoo() {
        try {
            commander = new Commander("Yahoo", "http://search.yahooapis.com/WebSearchService/V1/webSearch?appid=" + APP_ID, "");
            commander.setTraceSends(false);
            commander.setTrace(false);
            commander.setMinimumCommandPeriod(1000L);
        } catch (IOException ex) {
            System.err.println("Can't get youtube commander " + ex);
        }
    }
    
    public List<SearchResult> searchSite(String site, String query) {
        List<SearchResult> list = new ArrayList<SearchResult>();
        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            Document doc = commander.sendCommand("&site=" + site + "&query=" + encodedQuery);
            Element docElement = doc.getDocumentElement();
            NodeList results = docElement.getElementsByTagName("Result");
            for (int i = 0; i < results.getLength(); i++) {
                Element result = (Element) results.item(i);
                
                String title = XmlUtil.getElementContents(result, "Title");
                String url = XmlUtil.getElementContents(result, "Url");
                SearchResult sr = new SearchResult(title, url);
                list.add(sr);
            }
            return list;
        } catch (IOException ioe) {
            return list;
        }
    }
    
    // http://search.yahooapis.com/WebSearchService/V1/webSearch?appid=FrodoBaggins&site=en.wikipedia.org&query=emo+music+genre
    
    
    public static void main(String[] args) {
        Yahoo yahoo = new Yahoo();
        showBestWikiMatch(yahoo, "alt country genre music");
        showBestWikiMatch(yahoo, "dnb genre music");
        showBestWikiMatch(yahoo, "metal genre music");
        showBestWikiMatch(yahoo, "rock genre music ");
        showBestWikiMatch(yahoo, "folk genre music ");
    }
    
    static void showBestWikiMatch(Yahoo yahoo, String query) {
        List<SearchResult> sr = yahoo.searchSite("en.wikipedia.org", query);
        if (sr.size() > 0) {
            System.out.println("q: " + query + "|" + sr.get(0));
        } else {
            System.out.println("no match");
        }
    }
}
