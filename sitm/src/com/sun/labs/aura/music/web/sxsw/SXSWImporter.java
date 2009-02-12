/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.web.sxsw;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author plamere
 */
public class SXSWImporter {
    private int lineCount = 0;
    private ArtistPatcher patcher ;

    public SXSWImporter(ArtistPatcher patcher) {
        this.patcher = patcher;
    }

    public List<Artist> getArtists(String path) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(path));
        return getArtists(in);
    }

    public List<Artist> getArtists(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        List<Artist> artists =  getArtists(in);
        System.out.printf("Found %d artists\n", artists.size());
        return artists;
    }

    public List<Artist> getArtists(BufferedReader in) throws IOException {
        List<Artist> artists = new ArrayList<Artist>();
        String line = null;

        try {
            while ((line = in.readLine()) != null) {
                lineCount++;
                if (isArtistLine(line)) {
                    // System.out.println("   YES   " + line);
                    String name = getName(line);
                    String correction = null;
                    if (patcher != null) {
                        correction = patcher.patchName(name);
                    }
                    if (correction != null) {
                        System.out.printf("Patched %s to %s\n", name, correction);
                        name = correction;
                    }
                    String artistURL = getURL(line);
                    String locale = getLocale(line);
                    Artist artist = new Artist();
                    artist.setName(name);
                    artist.setUrl(artistURL);
                    artist.setWhere(locale);
                    artists.add(artist);
                } else {
                    // System.out.println("NO " + line);
                }
            }
        } finally {
            in.close();
        }
        return artists;
    }


    private Pattern artistLinePattern = Pattern.compile("^.*\\(.*\\)\\s*<br.*$");

    private boolean isArtistLine(String line) {
        Matcher matcher = artistLinePattern.matcher(line);
        return matcher.matches();
    }

//                <a href="http://www.theabramsbrothers.com">The Abrams Brothers </a>(Kingston ON)<br />
    //private Pattern urlPattern = Pattern.compile("\\<a href\\=\"(.*)");
    private Pattern urlPattern = Pattern.compile("<a href=\"([^\"]*).*");

    private String getURL(String line) {
        String url = "";
        Matcher matcher = urlPattern.matcher(line);
        if (matcher.matches()) {
            url = matcher.group(1).trim();
        } else {
            System.err.printf("No URL match on line %d %s\n", lineCount, line);
        }
        return url;
    }
    private Pattern namePattern = Pattern.compile("[^>]*>([^<]*)<.*");

    private String getName(String line) {
        String result = "";
        Matcher matcher = namePattern.matcher(line);
        if (matcher.matches()) {
            result = matcher.group(1).trim();
        } else {
            // must be a non URL pattern
            String fields[] = line.split("\\(");
            if (fields.length == 2) {
                result = fields[0].trim();
            } else {
                System.err.printf("Can't parse name on line %d %s\n", lineCount, line);
            }
        }

        return deentify(result);
    }
    //private Pattern localePattern = Pattern.compile("[^\\(]*\\(([^\\)]*)\\).*");
    private Pattern localePattern = Pattern.compile("^.*\\((.*)\\)\\s*<br.*$");

    private String getLocale(String line) {
        String result = "";
        Matcher matcher = localePattern.matcher(line);
        if (matcher.matches()) {
            result = matcher.group(1);
            result = result.replaceAll("NULL", "");
            result = result.trim();
        //System.out.println(result);
        } else {
            System.err.printf("No locale match on line %d %s\n", lineCount, line);
        }
        return deentify(result);
    }

    private String deentify(String s) {
        String old = s;
        s = s.replaceAll("\\&amp;", "&");
        s = s.replaceAll("\\&quot;", "\"");
        s = s.replaceAll("\\&#x27;", "'");
        s = s.replaceAll("\\&aacute;", "á");
        s = s.replaceAll("\\&eacute;", "é");
        if (false && !old.equals(s)) {
            System.out.printf("[%s] [%s]\n", old, s);
        }
        return s;
    }


    public static void main(String[] args) throws IOException {
        SXSWImporter si = new SXSWImporter(null);
        List<Artist> artists = si.getArtists(new URL("http://sxsw.com/music/shows/bands"));
        for (Artist artist : artists) {
            System.out.printf("%s %s %s\n", artist.getName(), artist.getWhere(), artist.getUrl());
        }
    }
}
