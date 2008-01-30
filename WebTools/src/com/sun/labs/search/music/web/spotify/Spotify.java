package com.sun.labs.search.music.web.spotify;


import com.sun.labs.search.music.web.Commander;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import com.sun.labs.search.music.web.XmlUtil;
import java.io.IOException;
import java.net.URLEncoder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
        String encodedArtist = URLEncoder.encode(artist, "UTF-8");
        Document doc = commander.sendCommand("search?search-type=artist&q=" + encodedArtist);
        Element docElement = doc.getDocumentElement();
        Element artistElement = XmlUtil.getFirstElement(docElement, "artist");
        if (artistElement != null) {
            String artistID =  artistElement.getAttribute("href");
            System.out.println("Spotify: " + artist + "->" + artistID);
            return artistID;
        }
        System.out.println("Spotify no match for : " + artist);
        return null;
    }

    public static void main(String[] args) throws IOException {
        Spotify spotify = new Spotify();
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
