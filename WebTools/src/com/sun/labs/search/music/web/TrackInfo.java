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

package com.sun.labs.search.music.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.*;

import org.blinkenlights.jid3.*;
import org.blinkenlights.jid3.v1.*;
import org.blinkenlights.jid3.v2.*;


public class TrackInfo {
    private String artist;
    private String album;
    private String title;
    private String genre;
    private String path;
    private int track = 0;
    private int year = 0;
    private boolean tagFound;
    
    private TrackInfo() {}
    
    public String getArtist() {
        return artist;
    }
    
    public String getAlbum() {
        return album;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getGenre() {
        return genre;
    }
    
    public int getTrack() {
        return track;
    }
    
    public int getYear() {
        return year;
    }
    
    public void dump(String msg) {
        System.out.println("------ " + msg + " ---------");
        System.out.println("  Path: " + path);
        System.out.println("Artist: " + artist);
        System.out.println(" Album: " + album);
        System.out.println(" Title: " + title);
        System.out.println(" Genre: " + genre);
        System.out.println(" Track: " + track);
        System.out.println("  Year: " + year);
        if (!tagFound) {
            System.out.println("PatchedTitle: " + title + "\tfrom:\t" + path);
        }
    }
    
    public static TrackInfo getTrackInfo(File file) {
        boolean tagFound = true;
        if (!file.canRead()) {
            System.err.println("ERR: Can't read " + file);
            return null;
        }
        
        String artist = "";
        String album = "";
        String genre = "";
        String title = "";
        String year = "";
        String track = "";
        String tag = "";
        String status ="OK";
        
        MP3File mp3 = new MP3File(file);
        ID3V1Tag v1tag = null;
        ID3V2Tag v2tag = null;
        try {
            v1tag = mp3.getID3V1Tag();
            v2tag = mp3.getID3V2Tag();
        } catch (ID3Exception ex) {
            System.err.println("Error reading mp3 tags " + ex);
        }
        if (v1tag != null) {
            tag += "ID3";
            artist = v1tag.getArtist();
            album = v1tag.getAlbum();
            genre = v1tag.getGenre().toString();
            title = v1tag.getTitle();
            year = getYear(v1tag);
            track = getTrack(v1tag);
        }
        
        if (v2tag != null) {
            if (tag.length() > 0) {
                tag += "+";
            }
            tag += "ID3V2";
            
            artist = takeBest(artist, v2tag.getArtist());
            album = takeBest(album, v2tag.getAlbum());
            genre = takeBest(genre, v2tag.getGenre());
            title = takeBest(title, v2tag.getTitle());
            track = takeBest(track, getTrack(v2tag));
            year = takeBest(year,  getYear(v2tag));
        }
        genre = filterGenre(genre);
        
        if (tag.length() == 0) {
            tag = "NO-TAG";
        }
        
        
        if (title.length() == 0) {
            title = extractFromPath(file, 0);
            tagFound = false;
        }
        
        TrackInfo ti = new TrackInfo();
        ti.artist = artist.trim();
        ti.album = album.trim();
        ti.genre = genre.trim();
        ti.title = title.trim();
        ti.path = file.getAbsolutePath();
        ti.tagFound = tagFound;
        try {
            ti.track = Integer.parseInt(track);
            
        } catch (NumberFormatException nfe) {
            
        }
        
        try {
            ti.year = Integer.parseInt(year);
        } catch (NumberFormatException nfe) {
        }
        return ti;
    }
    
    /**
     * Returns the best version. V2 is better than V1
     * unless V2 is null or empty. Note that many mp3s return
     * the genre "null" so we explicitly ignore that as well.
     */
    private static String takeBest(String v1, String v2) {
        if (v2 != null && v2.length() > 0 && !v2.equals("null")) {
            return v2;
        } else {
            return v1;
        }
    }
    
    private static boolean isFullyLabled(String artist, String album, String title, String genre) {
        //System.out.printf("%s %s %s %s\n", artist, album, title, genre);
        return artist.length() > 0 && album.length() > 0 && title.length() > 0 && genre.length() > 0;
    }
    
    /**
     * Extracts a segment from the end of the path
     */
    private static String extractFromPath(File file, int segment) {
        String path = file.getAbsolutePath();
        String[] segments = path.split(file.separator);
        int index = (segments.length - 1) - segment;
        if (index >= 0) {
            return segments[index];
        }
        return "";
    }
    /**
     * Some MP3 files are tagged with genres like so (33). This method
     * will convert a genre to an english description.
     */
    
    private static Pattern numberInParens = Pattern.compile("\\(([0-9]+)\\)");
    
    private static String filterGenre(String g) {
        
        String genre = g;
        Matcher matcher = numberInParens.matcher(g);
        if (matcher.find()) {
            String num = matcher.group(1);
            int val = Integer.parseInt(num);
            genre =  mapGenre(val);
        }
        if (genre == null || genre.length() == 0) {
            genre = "Unknown";
        }
        return genre;
    }
    
    
    private static String getYear(ID3Tag tag) {
        String year = "";
        try {
            if (tag instanceof ID3V2Tag) {
                ID3V2Tag v2tag = (ID3V2Tag) tag;
                year = "" + v2tag.getYear();
            } else {
                ID3V1Tag v1tag = (ID3V1Tag) tag;
                year = v1tag.getYear();
            }
        } catch (ID3Exception e) {
            year =  "";
        }
        return year;
    }
    
    private static String getTrack(ID3Tag tag) {
        String track = "";
        try {
            if (tag instanceof ID3V2Tag) {
                ID3V2Tag v2tag = (ID3V2Tag) tag;
                track = "" + v2tag.getTrackNumber();
            } else if (tag instanceof ID3V1_1Tag) {
                ID3V1_1Tag v11tag = (ID3V1_1Tag) tag;
                track = "" + v11tag.getAlbumTrack();
            } else {
                track = "";
            }
        } catch (ID3Exception e) {
            track =  "";
        }
        return track;
    }
    
    private static String[] genreString = {
        "Blues", "Classic Rock", "Country", "Dance", "Disco", "Funk",
        "Grunge", "Hip-Hop", "Jazz", "Metal", "New Age", "Oldies", "Other",
        "Pop", "R&B", "Rap", "Reggae", "Rock", "Techno", "Industrial",
        "Alternative", "Ska", "Death Metal", "Pranks", "Soundtrack",
        "Euro-Techno", "Ambient", "Trip-Hop", "Vocal", "Jazz+Funk", "Fusion",
        "Trance", "Classical", "Instrumental", "Acid", "House", "Game",
        "Sound Clip", "Gospel", "Noise", "AlternRock", "Bass", "Soul",
        "Punk", "Space", "Meditative", "Instrumental Pop", "Instrumental Rock",
        "Ethnic", "Gothic", "Darkwave", "Techno-Industrial", "Electronic",
        "Pop-Folk", "Eurodance", "Dream", "Southern Rock", "Comedy",
        "Cult", "Gangsta", "Top 40", "Christian Rap", "Pop/Funk",
        "Jungle", "Native American", "Cabaret", "New Wave", "Psychadelic",
        "Rave", "Showtunes", "Trailer", "Lo-Fi", "Tribal",
        "Acid Punk", "Acid Jazz", "Polka", "Retro", "Musical",
        "Rock & Roll", "Hard Rock", "Folk", "Folk-Rock", "National Folk",
        "Swing", "Fast Fusion", "Bebob", "Latin", "Revival",
        "Celtic", "Bluegrass", "Avantgarde", "Gothic Rock", "Progressive Rock",
        "Psychedelic Rock", "Symphonic Rock", "Slow Rock", "Big Band",
        "Chorus", "Easy Listening", "Acoustic", "Humour", "Speech",
        "Chanson", "Opera", "Chamber Music", "Sonata", "Symphony",
        "Booty Bass", "Primus", "Porn Groove", "Satire", "Slow Jam",
        "Club", "Tango", "Samba", "Folklore", "Ballad", "Power Ballad",
        "Rhythmic Soul", "Freestyle", "Duet", "Punk Rock", "Drum Solo",
        "A capella", "Euro-House", "Dance Hall"
    };
    
    private static String mapGenre(int genre) {
        if (genre >= 0 && genre < genreString.length) {
            return genreString[genre];
        } else {
            return null;
        }
    }

    public String getPath() {
        return path;
    }
}

