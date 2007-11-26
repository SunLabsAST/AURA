/*
 * Event.java
 *
 * Created on April 4, 2007, 8:52 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.search.music.web.upcoming;

/**
 *
 * @author plamere
 */

/** Creates a new instance of Event */
public class Event {
    private String name;
    private String date;
    private String venue;
    private String venueAddress;
    private String eventID;
    private String venueID;
    
    
    public String getName() {
        return name;
    }
    
    void setName(String name) {
        this.name = name;
    }
    
    public String getDate() {
        return date;
    }
    
    void setDate(String date) {
        this.date = date;
    }
    
    public String getVenue() {
        return venue;
    }
    
    public void setVenue(String venue) {
        this.venue = venue;
    }
    
    public String getEventID() {
        return eventID;
    }
    
    void setEventID(String eventID) {
        this.eventID = eventID;
    }
    
    public String getVenueID() {
        return venueID;
    }
    
    void setVenueID(String venueID) {
        this.venueID = venueID;
    }
    
    public String getVenueAddress() {
        return venueAddress;
    }
    
    public void setVenueAddress(String venueAddress) {
        this.venueAddress = venueAddress;
    }
    
    void dump() {
        System.out.printf("==== %s =====\n", name);
        System.out.printf("    Date: %s\n", date);
        System.out.printf("   Venue: %s\n", venue);
        System.out.printf(" Address: %s\n", venueAddress);
        System.out.printf(" eventID: %s\n", eventID);
        System.out.printf(" venueID: %s\n", venueID);
    }
}
