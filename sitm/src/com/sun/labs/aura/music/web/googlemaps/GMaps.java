/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.web.googlemaps;

import com.sun.labs.aura.music.web.Commander;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 *
 * @author plamere
 */
public class GMaps {

    //private String API_KEY = "BQIAAAAAwwPlYWQe-nE08q4dzHhDRRyw6J6P0HL6EteUvYVXZenhdQVwxRatKyCGAMwshEi0A_3n-HL411loQ";
    private String API_KEY = "ABQIAAAAAwwPlYWQe-nE08q4dzHhDRQ8Robh6HvpClbSAttr2ReK-j9H2RTIvOlFanZ0EWazEeJQWhFV17Kc1Q";
    private Commander commander;
//http://maps.google.com/maps/geo?q=nashua+nh&output=xml&sensor=true_or_false&key=abcdefg

    public GMaps() throws IOException {
        commander = new Commander("google maps", "http://maps.google.com/maps/geo", "&output=csv&sensor=false&key=" + API_KEY);
        commander.setRetries(1);
        commander.setTimeout(1000);
        commander.setTraceSends(false);
        commander.setMinimumCommandPeriod(500);
    }

    public Location getLocation(String placeName) throws IOException {
        InputStream is = commander.sendCommandRaw("?q=" + commander.encode(placeName));
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String line = in.readLine();
        String[] fields = line.split(",");
        if (fields.length == 4) {
            int status = Integer.parseInt(fields[0]);
            if (status == 200) {
                int accuracy = Integer.parseInt(fields[1]);
                float lat = Float.parseFloat(fields[2]);
                float longitude = Float.parseFloat(fields[3]);
                return new Location(lat, longitude);
            } else if (status == 620) {
                System.err.println("WARNING: gmaps query too fast");
            }
        }
        return null;
    }

    private static void dump(GMaps gmaps, String place) throws IOException {
        Location l = gmaps.getLocation(place);
        if (l != null) {
            System.out.printf("%.5f, %.5f %s\n", l.getLatitude(), l.getLongitude(), place);
        } else {
            System.out.printf("Can't find place for " + place);
        }
    }

    public static void main(String[] args) throws Exception {
        GMaps gmaps = new GMaps();

        dump(gmaps, "nashua nh");
        dump(gmaps, "montreal ca");
        dump(gmaps, "montreal ca");
        dump(gmaps, "london uk");
        dump(gmaps, "akron oh");
        dump(gmaps, "sydney australia");
        dump(gmaps, "ludlow vermont");
        dump(gmaps, "austin tx");
        dump(gmaps, "garbage location tx");
    }
}
