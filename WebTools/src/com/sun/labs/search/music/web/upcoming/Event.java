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
