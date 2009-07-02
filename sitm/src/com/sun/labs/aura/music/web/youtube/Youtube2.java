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

package com.sun.labs.aura.music.web.youtube;

import com.google.gdata.client.Query.CategoryFilter;
import com.google.gdata.client.youtube.YouTubeQuery;
import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.Category;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.data.youtube.VideoFeed;
import com.google.gdata.data.youtube.YouTubeMediaContent;
import com.google.gdata.data.youtube.YouTubeMediaGroup;
import com.google.gdata.data.youtube.YouTubeNamespace;
import com.google.gdata.util.ServiceException;
import com.sun.labs.aura.music.web.WebServiceAccessor;
import com.sun.labs.aura.util.AuraException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author plamere
 */
public class Youtube2 extends WebServiceAccessor {

    private final static String YOUTUBE_GDATA_SERVER = "http://gdata.youtube.com";
    private final static String VIDEOS_FEED = YOUTUBE_GDATA_SERVER + "/feeds/api/videos";
    private YouTubeService yts;

    public Youtube2() throws AuraException {
        super("Youtube v2", "YOUTUBE_API_KEY");
        yts = new YouTubeService("project-aura", API_KEY);
    }

    public List<YoutubeVideo> musicVideoSearch(String artistName, int max) throws IOException {
        try {
            YouTubeQuery query = new YouTubeQuery(new URL(VIDEOS_FEED));
            CategoryFilter musicFilter = new CategoryFilter();
            musicFilter.addCategory(new Category(YouTubeNamespace.CATEGORY_SCHEME, "Music"));
            query.setMaxResults(max);
            query.addCategoryFilter(musicFilter);
            // order results by the number of views (most viewed first)
            //query.setOrderBy(YouTubeQuery.OrderBy.VIEW_COUNT);
            query.setOrderBy(YouTubeQuery.OrderBy.RELEVANCE);
            // (by default, it is excluded)
            query.setSafeSearch(YouTubeQuery.SafeSearch.MODERATE);
            String searchTerms = artistName;
            query.setFullTextQuery(searchTerms);
            VideoFeed videoFeed = yts.query(query, VideoFeed.class);
            List<YoutubeVideo> vidlist = new ArrayList<YoutubeVideo>();
            for (VideoEntry videoEntry : videoFeed.getEntries()) {
                YoutubeVideo video = new YoutubeVideo();
                if (videoEntry.getTitle() != null) {
                    video.setTitle(videoEntry.getTitle().getPlainText());
                }
                /*
                if (videoEntry.getSummary() != null) {
                video.setDescription(videoEntry.getSummary().getPlainText());
                }
                 * */
                video.setId(videoEntry.getId());

                if (videoEntry.getStatistics() != null) {
                    video.setViewCount((int) videoEntry.getStatistics().getViewCount());
                    if (videoEntry.getAuthors().size() > 0) {
                        video.setAuthor(videoEntry.getAuthors().get(0).getName());
                    }
                }
                if (videoEntry.getRating() != null) {
                    video.setRatingCount(videoEntry.getRating().getNumRaters());
                    video.setRating(videoEntry.getRating().getAverage());
                }
                YouTubeMediaGroup mediaGroup = videoEntry.getMediaGroup();
                if (mediaGroup != null) {
                    if (mediaGroup.getThumbnails().size() > 0) {
                        video.setThumbnail(new URL(mediaGroup.getThumbnails().get(0).getUrl()));
                    }
                    StringBuilder keywords = new StringBuilder();
                    for (String keyword : mediaGroup.getKeywords().getKeywords()) {
                        keywords.append(keyword);
                        keywords.append(" ");
                    }
                    video.setTags(keywords.toString().trim());
                    video.setDescription(mediaGroup.getDescription().getContent().getPlainText());

                    video.setURL(new URL(mediaGroup.getPlayer().getUrl()));
                    for (YouTubeMediaContent mediaContent : mediaGroup.getYouTubeContents()) {
                        // look for videos that are embeddable (type 5) = use the first one that we find
                        if (mediaContent.getYouTubeFormat() == 5) {
                            video.setFlashURL(new URL(mediaContent.getUrl()));
                            int duration = mediaContent.getDuration();
                            video.setLength(duration);
                            break;
                        }
                    }
                    vidlist.add(video);
                }
            }
            return vidlist;
        } catch (ServiceException ex) {
            throw new IOException("service exception", ex);
        }
    }

    public static void searchTest(Youtube2 yt, String name) throws Exception {
        List<YoutubeVideo> results = yt.musicVideoSearch(name, 3);
        for (YoutubeVideo video : results) {
            System.out.println("  " + video.toString());
            video.dump();
            System.out.println("");
        }
    }

    public static void main(String[] args) throws Exception {
        Youtube2 yt = new Youtube2();
        searchTest(yt, "weezer");
        System.out.println("");
        searchTest(yt, "yes");
        System.out.println("");
        searchTest(yt, "Emerson, Lake and Palmer");
    }
}
