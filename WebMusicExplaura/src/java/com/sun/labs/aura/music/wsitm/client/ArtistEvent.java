/*
 * ArtistEvent.java
 *
 * Created on April 5, 2007, 5:47 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 *
 * @author plamere
 */
public class ArtistEvent implements IsSerializable {
    private String name;
    private String date;
    private String venue;
    private String venueAddress;
    private String eventID;
    private String venueID;
    
    /** Creates a new instance of ArtistEvent */
    public ArtistEvent() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getVenueAddress() {
        return venueAddress;
    }

    public void setVenueAddress(String venueAddress) {
        this.venueAddress = venueAddress;
    }

    public String getEventID() {
        return eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public String getVenueID() {
        return venueID;
    }
    
    public String getVenueURL() {
        return "http://upcoming.org/venue/" + getVenueID() + "/";
    }
    
    public String getEventURL() {
        return "http://upcoming.org/event/" + getEventID() + "/";
    }

    public void setVenueID(String venueID) {
        this.venueID = venueID;
    }
    
}
