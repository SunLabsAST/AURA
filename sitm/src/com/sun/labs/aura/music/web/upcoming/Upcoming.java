/*
 * Upcoming.java
 *
 * Created on April 4, 2007, 8:44 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.web.upcoming;

import com.sun.labs.aura.music.web.Commander;
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
    public List<UpcomingEvent> searchEventsByArtist(String artistName) throws IOException {
        String encodedName = normalizeName(artistName);
        Document doc = commander.sendCommand("&method=event.search&category_id=1&search_text=" + encodedName);
        return extractEvents(doc);
    }
    
    private String normalizeName(String name) throws IOException {
        return URLEncoder.encode("\"" + name + "\"", "UTF-8");
    }
    
    public List<UpcomingEvent> searchEventsByZipcode(String zipcode) throws IOException {
        Document doc = commander.sendCommand("&method=event.search&category_id=1&location=" + zipcode);
        return extractEvents(doc);
    }
    
    private List<UpcomingEvent> extractEvents(Document doc) {
        List<UpcomingEvent> list = new ArrayList<UpcomingEvent>();
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
            UpcomingEvent newEvent = new UpcomingEvent();
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
            List<UpcomingEvent> events = upcoming.searchEventsByArtist("the arcade fire");
            for (UpcomingEvent event : events) {
                event.dump();
            }
        }
        {
            List<UpcomingEvent> events = upcoming.searchEventsByZipcode("03060");
            for (UpcomingEvent event : events) {
                event.dump();
            }
        }
    }
}
