/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.web.sxsw;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author plamere
 */
public class SXSWImporter {
    private int lineCount = 0;
    private Map<String, String> namePatcher = new HashMap<String, String>();
    private String dblocation;

    public SXSWImporter(String dblocation) {
        this.dblocation = dblocation;
        try {
            loadNameCorrections();
        } catch (IOException ioe) {
            System.err.println("Can't load name correctins");
        }
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
        createArchive(url);
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
                    String correction = namePatcher.get(name);
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

    public void loadNameCorrections() throws IOException {
        BufferedReader in = new BufferedReader(new FileReader("resources/artistpatch.dat"));
        String line = null;

        while ((line = in.readLine()) != null) {
            String[] fields = line.split("<sep>");
            if (fields.length == 3) {
                String artistName = fields[0].trim();
                String fieldName = fields[1].trim();
                String newValue = fields[2].trim();
                if (fieldName.equals("name")) {
                    namePatcher.put(artistName, newValue);
                }
            }
        }
        in.close();
    }

    public void createArchive(URL url) throws IOException {
        createArchiveDir();
        File file = new File(getArchiveName());
        if (!file.exists()) {
            URLConnection connection = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            PrintWriter out = new PrintWriter(getArchiveName());
            String line = null;
            try {
                while ((line = in.readLine()) != null) {
                    out.println(line);
                }
            } finally {
                in.close();
            }
        }
    }

    private void createArchiveDir() {
        File archive = new File(dblocation + "/archive");
        if (!archive.exists()) {
            archive.mkdir();
        }
    }

    private String getArchiveName() {
        Calendar cal = Calendar.getInstance();
        return String.format(dblocation + "/archive/%d.%d.%d:%02d:00.html", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY));
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

    public static void mainfile(String[] args) throws IOException {
        SXSWImporter si = new SXSWImporter("test");
        List<Artist> artists = si.getArtists("sxsw-artists.txt");
        for (Artist artist : artists) {
            System.out.printf("%s %s %s\n", artist.getName(), artist.getWhere(), artist.getUrl());
        }
    }

    public static void main(String[] args) throws IOException {
        SXSWImporter si = new SXSWImporter("test");
        List<Artist> artists = si.getArtists(new URL("http://sxsw.com/music/shows/bands"));
        for (Artist artist : artists) {
            System.out.printf("%s %s %s\n", artist.getName(), artist.getWhere(), artist.getUrl());
        }
    }
}
