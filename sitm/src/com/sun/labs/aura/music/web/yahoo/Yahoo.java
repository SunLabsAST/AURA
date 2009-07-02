/*
 * Copyright 2007-2009 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.labs.aura.music.web.yahoo;

import com.sun.labs.aura.music.web.Commander;
import com.sun.labs.aura.music.web.WebServiceAccessor;
import com.sun.labs.aura.music.web.XmlUtil;
import com.sun.labs.aura.util.AuraException;
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
public class Yahoo extends WebServiceAccessor {

    Commander commander;
    
    /** Creates a new instance of Yahoo */
    public Yahoo() throws AuraException {
        super("Yahoo", "YAHOO_APP_ID");
        
        try {
            commander = new Commander("Yahoo", "http://search.yahooapis.com/WebSearchService/V1/webSearch?appid=" + API_KEY, "");
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
    
    
    public static void main(String[] args) throws AuraException {
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
