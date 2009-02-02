/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.web.sxsw;

import com.sun.labs.aura.music.web.lastfm.SocialTag;
import com.sun.labs.aura.music.web.youtube.YoutubeVideo;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author plamere
 */
public class ArtistPatcher {

    private final static String END_MARKER = "-- bio end --";
    private DBCore db;
    private TagFilter tagFilter;

    ArtistPatcher(DBCore db, TagFilter tagFilter) {
        this.db = db;
        this.tagFilter = tagFilter;
    }

    void applyPatches() throws IOException {
        int lineCount = 0;
        BufferedReader in = new BufferedReader(new FileReader("resources/artistpatch.dat"));
        String line = null;

        while ((line = in.readLine()) != null) {
            lineCount++;
            String[] fields = line.split("<sep>");
            if (fields.length == 3) {
                String artistName = fields[0].trim();
                String fieldName = fields[1].trim();
                String newValue = fields[2].trim();

                System.out.printf("Patching %s of %s to %s\n", fieldName, artistName, newValue);

                Artist artist = db.getArtist(artistName);
                if (artist != null && artist.getArtistInfo() != null) {
                    if (fieldName.equals("bio")) {
                        // suck up text until the end marker

                        StringBuilder sb = new StringBuilder();
                        while ((line = in.readLine()) != null) {
                            lineCount++;
                            if (line.startsWith(END_MARKER)) {
                                break;
                            } else {
                                sb.append(line);
                            }
                        }
                        newValue = sb.toString();
                        String oldBio = artist.getArtistInfo().getBioSummary();
                        if (!newValue.equals(oldBio)) {
                            artist.getArtistInfo().setBioSummary(newValue);
                        }
                    } else if (fieldName.equals("where")) {
                        String old = artist.getWhere();
                        if (!newValue.equals(old)) {
                            artist.setWhere(newValue);
                            artist.setLocation(null);
                        }
                    } else if (fieldName.equals("name")) {
                        // nothing to do here, the corrections
                        // are done during importing
                    } else if (fieldName.equals("image")) {
                        String old = artist.getArtistInfo().getLargeImage();
                        if (!newValue.equals(old)) {
                            artist.getArtistInfo().setLargeImage(newValue);
                        }
                    } else if (fieldName.equals("url")) {
                        String old = artist.getUrl();
                        if (!newValue.equals(old)) {
                            artist.setUrl(newValue);
                        }
                    } else if (fieldName.equals("video")) {
                        YoutubeVideo video = new YoutubeVideo(newValue);
                        artist.setVideos(new YoutubeVideo[]{video});
                    } else if (fieldName.equals("tag")) {
                        String[] tags = newValue.split(",");
                        List<SocialTag> socialTags = new ArrayList<SocialTag>();
                        for (String tag : tags) {
                            tag = tagFilter.mapTagName(tag);
                            socialTags.add(new SocialTag(tag, 50));
                        }
                        artist.setTags(socialTags.toArray(new SocialTag[0]));
                    } else {
                        System.err.println("Unknown patch field " + fieldName + " at line " + lineCount);
                    }
                } else {
                    if (!fieldName.equals("name")) {
                        System.err.printf("Can't find artist %s for patching\n", artistName);
                    }
                }
            } else {
                System.err.println("Bad patch file format at line " + lineCount);
            }
        }

        in.close();
    }
}
