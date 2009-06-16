/*
 * Copyright 2008-2009 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.labs.aura.music.web.lastfm;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

/**
 * Crawls tags from last.fm
 * @author plamere
 */
public class TagCrawler {

    private LastFMImpl lastfm;
    private Set<String> doneIDs = new HashSet<String>();
    private int itemCount;
    private int missingCount;
    private int tagCount;
    private int f_count;
    private int f_tid;
    private int f_artist;
    private int f_album;
    private int f_track;
    private int f_tag;
    private int f_tagCount;

    enum TagType {

        Artist, Album, Track
    };
    private TagType curType;

    TagCrawler() throws IOException {
        lastfm = new LastFMImpl();
        lastfm.setTrace(false);
    }

    void artistCrawler(String inputFile, String outputFile) throws IOException {
        curType = TagType.Artist;
        f_count = 2;
        f_tid = 0;
        f_artist = 1;
        f_track = -1;
        f_album = -1;
        loadOutputFile(outputFile);
        processInputFile(inputFile, outputFile);
    }

    void albumCrawler(String inputFile, String outputFile) throws IOException {
        curType = TagType.Album;
        f_count = 3;
        f_tid = 0;
        f_album = 1;
        f_artist = 2;
        f_track = -1;
        loadOutputFile(outputFile);
        processInputFile(inputFile, outputFile);
    }

    void trackCrawler(String inputFile, String outputFile) throws IOException {
        curType = TagType.Track;
        f_count = 4;
        f_tid = 0;
        f_track = 1;
        f_artist = 2;
        f_album = 3;
        loadOutputFile(outputFile);
        processInputFile(inputFile, outputFile);
    }

    private void processInputFile(String trackList, String outputFile) throws IOException {
        PrintWriter out = null;
        BufferedReader in = null;

        try {
            // a printwriter that encodes as utf8 and appends
            out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outputFile, true), "utf8"), true);

            // input:
            // mbtid <sep> track <sep> artist <sep> album
            // output:
            // mbtid <sep> track <sep> artist <sep> album <sep> tag <sep> count

            in = new BufferedReader(new FileReader(trackList));
            String line = null;

            int lineCount = 0;
            while ((line = in.readLine()) != null) {
                lineCount++;
                if (line.startsWith("#")) {
                    continue;
                }
                String[] fields = line.split("\\s*<sep>\\s*");
                if (fields.length == f_count) {
                    loadTags(out, fields);
                } else {
                    System.out.printf("Bad format at line %s, found %d fields, expected %d\n", lineCount, fields.length, f_count);
                }
            }
        } catch (IOException ioe) {
            System.out.println("I/O problem " + ioe);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException ioe) {
            }
        }

        System.out.printf("Already collected %d items", doneIDs.size());

    }

    private void loadTags(PrintWriter out, String[] fields) {
        int tagsFound = 0;
        String mbtid = fields[f_tid];
        if (doneIDs.contains(mbtid)) {
            return;
        }

        try {
            itemCount++;

            SocialTag[] tags = getTags(fields);
            tagsFound = tags.length;
            if (tags.length == 0) {
                missingCount++;
                for (int i = 0; i < fields.length; i++) {
                    out.printf("%s <sep> ", fields[i]);
                }
                out.println("(none) <sep> 0");
            } else {
                for (SocialTag tag : tags) {
                    tagCount++;
                    for (int i = 0; i < fields.length; i++) {
                        out.printf("%s <sep> ", fields[i]);
                    }
                    out.printf(" %s <sep> %d", tag.getName(), tag.getFreq());
                    out.println();
                }
            }
        } catch (IOException ioe) {
            missingCount++;
            for (int i = 0; i < fields.length; i++) {
                out.printf("%s <sep> ", fields[i]);
            }
            out.println("(none) <sep> 0");
        }

        System.out.printf("Coverage: %%%d items: %d  Missing: %d   Tags: %d     %d/%s\n",
                (itemCount - missingCount) * 100 / itemCount,
                itemCount, missingCount, tagCount,
                tagsFound, getLabel(fields));
    }

    SocialTag[] getTags(String[] fields) throws IOException {
        SocialTag[] tags = null;
        switch (curType) {
            case Album:
                tags = lastfm.getAlbumTags(fields[f_artist], fields[f_album]);
                break;
            case Artist:
                tags = lastfm.getArtistTags(fields[f_artist]);
                break;
            case Track:
                tags = lastfm.getTrackTags(fields[f_artist], fields[f_track]);
                break;
        }
        return tags;
    }

    String getLabel(String[] fields) {
        String label = "";
        switch (curType) {
            case Album:
                label = fields[f_artist] + "/" + fields[f_album];
                break;
            case Artist:
                label = fields[f_artist];
                break;
            case Track:
                label = fields[f_artist] + "/" + fields[f_track];
                break;
        }
        return label;
    }

    private void loadOutputFile(String outName) {
        f_tag = f_count;
        f_tagCount = f_tag + 1;
        BufferedReader in = null;
        int lineCount = 0;
        try {
            in = new BufferedReader(new FileReader(outName));
            String line = null;

            while ((line = in.readLine()) != null) {
                lineCount++;
                if (line.startsWith("#")) {
                    continue;
                }

                String[] fields = line.split("\\s*<sep>\\s*");


                if (fields.length == f_count + 2) {
                    doneIDs.add(fields[f_tid]);
                    if (fields[f_tag].equals("(none)")) {
                        missingCount++;
                    } else {
                        tagCount++;
                    }
                } else {
                    System.out.printf("Bad format at line %s, found %d fields, expected %d\n", lineCount, fields.length, f_count + 2);
                }
            }
        } catch (IOException ioe) {
            // file is probably not  there, so no worries
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                }

            }
        }
        itemCount = doneIDs.size();
        System.out.printf("Already collected %d items\n", doneIDs.size());
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            usage();
            System.exit(0);
        }

        try {
            if (args[0].equals("-artist")) {
                TagCrawler tc = new TagCrawler();
                tc.artistCrawler(args[1], args[2]);
            } else if (args[0].equals("-track")) {
                TagCrawler tc = new TagCrawler();
                tc.trackCrawler(args[1], args[2]);

            } else if (args[0].equals("-album")) {
                TagCrawler tc = new TagCrawler();
                tc.albumCrawler(args[1], args[2]);
            } else {
                usage();
            }
        } catch (IOException ex) {
            System.out.println("Can't load the crawler: " + ex.getMessage());
            System.exit(1);
        }

    }

    private static void usage() {
        System.out.println("Usage: [-artist|-album|-track] itemlist.txt output.txt");
    }
}
