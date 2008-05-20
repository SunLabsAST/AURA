package com.sun.labs.aura.music.web.spotify;

import com.sun.labs.aura.music.web.Commander;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import com.sun.labs.aura.music.web.URLUTF8Encoder;
import com.sun.labs.aura.music.web.Utilities;
import com.sun.labs.aura.music.web.XmlUtil;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author plamere
 */
public class Spotify {

    Commander commander;

    public Spotify() throws IOException {
        commander = new Commander("spotify", "http://ws.spotify.com/", "");
        commander.setRetries(1);
        commander.setTimeout(10000);
        commander.setTraceSends(false);
    }

    // http://ws.spotify.com/search?q=foo&search-type=artist
    public String getSpotifyIDforArtist(String artist) throws IOException {
        String encodedArtist = encodeArtist(artist);
        Document doc = commander.sendCommand("search?search-type=track&limit=100&q=artist:" + encodedArtist);
        Element docElement = doc.getDocumentElement();
        NodeList tracks = docElement.getElementsByTagName("track");

        if (tracks != null) {
            // spotify orders tracks by popularity, so we have to dig through them to find the
            // most popular track by this artist.

            String normalizedArtist = Utilities.normalize(artist);
            for (int i = 0; i < tracks.getLength(); i++) {
                Element track = (Element) tracks.item(i);
                Element artistElement = (Element) XmlUtil.getDescendent(track, "artist");
                String foundArtistName = XmlUtil.getDescendentText(artistElement, "name");
                if (foundArtistName != null) {
                    if (normalizedArtist.equals(Utilities.normalize(foundArtistName))) {
                        String trackID = track.getAttribute("href");
                        System.out.println("Spotify: " + artist + "->" + foundArtistName);
                        return trackID;
                    }
                }
            }
        }
        System.out.println("Spotify: No match for " + artist + " as " + encodedArtist);
        return null;
    }

    private String encodeArtist(String artist) throws IOException {
        String encodedArtist = URLEncoder.encode(artist, "UTF-8");
        return encodedArtist;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("File encoding is " + System.getProperty("file.encoding"));
        Spotify spotify = new Spotify();
        System.out.println(spotify.getSpotifyIDforArtist("weezer"));
        System.out.println(spotify.getSpotifyIDforArtist("emerson, lake"));
        System.out.println(spotify.getSpotifyIDforArtist("beck"));
        System.out.println(spotify.getSpotifyIDforArtist("spinal tap"));
        System.out.println(spotify.getSpotifyIDforArtist("beatles, the"));
        System.out.println(spotify.getSpotifyIDforArtist("monkees"));
        System.out.println(spotify.getSpotifyIDforArtist("oasis"));
        System.out.println(spotify.getSpotifyIDforArtist("bjork"));
        System.out.println(spotify.getSpotifyIDforArtist("unknown bad band"));
    }
}
