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

package com.sun.labs.search.music.web.upcoming;

import com.sun.labs.search.music.web.Commander;
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
public class Upcoming {
    private Commander commander;
    
    /** Creates a new instance of Upcoming */
    public Upcoming() throws IOException {
        commander = new Commander("upcoming", "http://upcoming.yahooapis.com/services/rest/?api_key=fdb4bbe79f", "");
        commander.setTraceSends(false);
    }
    
    // http://upcoming.yahooapis.com/services/rest/?api_key=fdb4bbe79f&method=event.search&search_text=miles+davis&category_id=1
    public List<Event> searchEventsByArtist(String artistName) throws IOException {
        String encodedName = normalizeName(artistName);
        Document doc = commander.sendCommand("&method=event.search&category_id=1&search_text=" + encodedName);
        return extractEvents(doc);
    }
    
    private String normalizeName(String name) throws IOException {
        return URLEncoder.encode("\"" + name + "\"", "UTF-8");
    }
    
    public List<Event> searchEventsByZipcode(String zipcode) throws IOException {
        Document doc = commander.sendCommand("&method=event.search&category_id=1&location=" + zipcode);
        return extractEvents(doc);
    }
    
    private List<Event> extractEvents(Document doc) {
        List<Event> list = new ArrayList<Event>();
        Element docElement = doc.getDocumentElement();
        NodeList events = docElement.getElementsByTagName("event");
        for (int i = 0; i < events.getLength(); i++) {
            Element event = (Element) events.item(i);
            String id = event.getAttribute("id");
            String name = event.getAttribute("name");
            String date = event.getAttribute("start_date");
            String venueName = event.getAttribute("venue_name");
            String venueID = event.getAttribute("venue_id");
            String venueAddress = event.getAttribute("venue_city");
            Event newEvent = new Event();
            newEvent.setEventID(id);
            newEvent.setName(name);
            newEvent.setDate(date);
            newEvent.setVenue(venueName);
            newEvent.setVenueAddress(venueAddress);
            newEvent.setVenueID(venueID);
            list.add(newEvent);
        }
        return list;
    }
    
    public static void main(String[] args) throws Exception {
        Upcoming upcoming = new Upcoming();
        {
            List<Event> events = upcoming.searchEventsByArtist("the arcade fire");
            for (Event event : events) {
                event.dump();
            }
        }
        {
            List<Event> events = upcoming.searchEventsByZipcode("03060");
            for (Event event : events) {
                event.dump();
            }
        }
    }
}
