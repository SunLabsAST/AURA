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

import com.sun.labs.aura.music.web.Commander;
import com.sun.labs.aura.music.web.WebServiceAccessor;
import com.sun.labs.aura.music.web.XmlUtil;
import com.sun.labs.aura.util.AuraException;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author plamere
 */
public class Youtube extends WebServiceAccessor {

    private Commander commander;
    
    /** Creates a new instance of Youtube */
    public Youtube() throws AuraException {
        super("Youtube v1", "YOUTUBE_API_KEY");

        try {
            commander = new Commander("YouTube", "http://www.youtube.com/api2_rest?dev_id=" + API_KEY, "");
            commander.setTraceSends(false);
            commander.setTrace(false);
            commander.setMinimumCommandPeriod(1000L);
        } catch (IOException ex) {
            System.err.println("Can't get youtube commander " + ex);
        }
    }
    
    // http://www.youtube.com/api2_rest?method=youtube.videos.list_by_tag&dev_id=KEY&tag=weezer&page=1
    
    public List<YoutubeVideo> musicSearch(String query, int maxResults) throws IOException {
        List<YoutubeVideo>  videos = new ArrayList<YoutubeVideo>(100);
        String encodedQuery = URLEncoder.encode(query, "UTF-8");
        int perPage = 100;
                
        videos = fetchVideos("&method=youtube.videos.list_by_tag&page=1&per_page=" + perPage + "&tag=" + encodedQuery);
        if (videos.size() > maxResults) {
            videos = videos.subList(0, maxResults);
        }
        return videos;
    }

    public List<YoutubeVideo> musicSearch2(String query, int maxResults) throws IOException {
        List<YoutubeVideo>  videos = new ArrayList<YoutubeVideo>(100);
        String encodedQuery = URLEncoder.encode(query, "UTF-8");
        int perPage = 100;

        videos = fetchVideos("&method=youtube.videos.list_by_tag&page=1&per_page=" + perPage + "&tag=" + encodedQuery);
        if (videos.size() > maxResults) {
            videos = videos.subList(0, maxResults);
        }
        return videos;
    }

    public List<YoutubeVideo> fetchVideos(String url) throws IOException {
        List<YoutubeVideo>  videos = new ArrayList<YoutubeVideo>(100);
        Document doc = commander.sendCommand(url);
        Element docElement = doc.getDocumentElement();
        NodeList list = docElement.getElementsByTagName("video");

        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            Element element = (Element) node;
            YoutubeVideo video = new YoutubeVideo();
            video.setAuthor(XmlUtil.getElementContents(element, "author"));
            video.setId(XmlUtil.getElementContents(element, "id"));
            video.setTitle(XmlUtil.getElementContents(element, "title"));

            video.setLength(getInteger(element, "length_seconds"));
            video.setRating(getFloat(element, "rating_avg"));
            video.setRatingCount(getInteger(element, "rating_count"));
            video.setDescription(XmlUtil.getElementContents(element, "description"));
            video.setViewCount(getInteger(element, "view_count"));
            video.setCommentCount(getInteger(element, "comment_count"));
            video.setTags(XmlUtil.getElementContents(element, "tags"));
            video.setURL(new URL(XmlUtil.getElementContents(element, "url")));
            video.setThumbnail(new URL(XmlUtil.getElementContents(element, "thumbnail_url")));
            videos.add(video);
        }
        Collections.sort(videos, YoutubeVideo.PLAY_ORDER);
        Collections.reverse(videos);
        return videos;
    }
    
    private void rotateVideos(List<YoutubeVideo> videos) {
        if (videos.size() > 1) {
            Collections.rotate(videos, -1);
        }
    }
    
    private int getInteger(Element element, String name) throws IOException {
        String sval = XmlUtil.getElementContents(element, name);
        int val = 0;
        if (sval == null || sval.equalsIgnoreCase("none")) {
            return 0;
        }
        try {
            val = Integer.parseInt(sval);
            return val;
        } catch (NumberFormatException nfe) {
            System.out.println("NFE  for " + name + " = " + val);
            return 0;
        }
    }
    
    private float getFloat(Element element, String name) throws IOException {
        String sval = XmlUtil.getElementContents(element, name);
        float val = 0;
        try {
            val = Float.parseFloat(sval);
            return val;
        } catch (NumberFormatException nfe) {
            System.out.println("NFE  for " + name + " = " + val);
            return 0;
        }
    }
    
    public static void main(String[] args) throws IOException, AuraException {
        Youtube youtube = new Youtube();
        
        List<YoutubeVideo> videos = youtube.musicSearch("yes", 20);
        for (YoutubeVideo video :videos) {
            video.dump();
        }

        videos = youtube.musicSearch("godspeed you! black emperor", 20);
        for (YoutubeVideo video :videos) {
            video.dump();
        }
    }
}
