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

package com.sun.labs.aura.music.web.musicbrainz;

import com.sun.labs.aura.music.web.Commander;
import com.sun.labs.aura.music.web.XmlUtil;
import com.sun.labs.aura.util.Scored;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author plamere
 */
public class MusicBrainz {
    private final static int SCORE_THRESHOLD = 95;
    private Commander  mbCommander;

    public static final SimpleDateFormat mbDateFormater = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Creates a new instance of MusicBrainz
     */
    public MusicBrainz() throws IOException {
        mbCommander = new Commander("musicbrainz", "http://musicbrainz.org/ws/1/", "");
        mbCommander.setTraceSends(false);
        mbCommander.setTrace(false);
        mbCommander.setMinimumCommandPeriod(1000L);
    }
    
    public void setTrace(boolean trace) {
        mbCommander.setTraceSends(trace);
    }

    private final String appendToQuery(String query, String field, String value) throws IOException {
        if (value!=null && value.length()>0) {
            query += "&" + field + "=" + normalize(value);
        }
        return query;
    }

    public List<Scored<MusicBrainzAlbumInfo>> albumSearch(String title, String artistName,
            String artistMbid) throws IOException {

        // Construct the query with the information that was provided
        String query = "release/?type=xml";
        query = appendToQuery(query, "title", title);
        query = appendToQuery(query, "artist", artistName);
        query = appendToQuery(query, "artistid", artistMbid);
        query += "&inc=artist+release-events";

        List<Scored<MusicBrainzAlbumInfo>> albumList = new ArrayList<Scored<MusicBrainzAlbumInfo>>();
        Document mbDoc = mbCommander.sendCommand(query);
        Element docElement = mbDoc.getDocumentElement();
        NodeList albums = docElement.getElementsByTagName("release");
        for (int i=0; i<albums.getLength(); i++) {

            Element albumElement = (Element) albums.item(i);
            int score = Integer.parseInt(albumElement.getAttribute("ext:score"));
            MusicBrainzAlbumInfo albumInfo = extractAlbumInfo(albumElement);

            // Get artist info
            Element artistNode = (Element) XmlUtil.getDescendent(albumElement, "artist");
            if (artistNode != null) {
                albumInfo.addArtistId(artistNode.getAttribute("id"));
            }

            albumList.add(new Scored<MusicBrainzAlbumInfo>(albumInfo, score));
        }
        return albumList;
    }

    /**
     * Returned a scored list of tracks by doing a search on the musicbrainz api.
     * Each parameter can be null or a zero-length string. You can use any number
     * of parameters.
     */
    public List<Scored<MusicBrainzTrackInfo>> trackSearch(String title, String artistName,
            String artistMbid, String albumName, String albumMbid) throws IOException {

        // Construct the query with the information that was provided
        String query = "track/?type=xml";
        query = appendToQuery(query, "title", title);
        query = appendToQuery(query, "artist", artistName);
        query = appendToQuery(query, "artistid", artistMbid);
        query = appendToQuery(query, "release", albumName);
        query = appendToQuery(query, "releaseid", albumMbid);
        query += "&inc=artist+releases";

        List<Scored<MusicBrainzTrackInfo>> trackList = new ArrayList<Scored<MusicBrainzTrackInfo>>();
        Document mbDoc = mbCommander.sendCommand(query);
        Element docElement = mbDoc.getDocumentElement();
        NodeList tracks = docElement.getElementsByTagName("track");
        for (int i=0; i<tracks.getLength(); i++) {
            Element trackElement = (Element) tracks.item(i);

            MusicBrainzTrackInfo trackInfo = new MusicBrainzTrackInfo();

            int score = Integer.parseInt(trackElement.getAttribute("ext:score"));
            
            // Get track info
            trackInfo.setMbid(trackElement.getAttribute("id"));
            trackInfo.setTitle(XmlUtil.getElementContents(trackElement, "title"));
            trackInfo.setDuration(XmlUtil.getElementContentsAsInteger(trackElement, "duration"));

            // Get artist info
            Element artistNode = (Element) XmlUtil.getDescendent(trackElement, "artist");
            if (artistNode != null) {
                trackInfo.setArtistMbid(artistNode.getAttribute("id"));
                trackInfo.setArtistName(XmlUtil.getElementContents(artistNode, "name"));
            }

            // Get release info
            List<Node> releaseList = XmlUtil.getDescendents(trackElement, "release-list");
            for (int j=0; j<releaseList.size(); j++) {
                Element relationlist = (Element) releaseList.get(j);
                NodeList releasesList = relationlist.getElementsByTagName("release");
                
                Element release = (Element) releasesList.item(0);
                trackInfo.setAlbumMbid(release.getAttribute("id"));
                trackInfo.setAlbumName(XmlUtil.getElementContents(release, "title"));
                break;
            }
            trackList.add(new Scored<MusicBrainzTrackInfo>(trackInfo, score));
        }

        return trackList;
    }
  
    // http://musicbrainz.org/ws/1/artist/?type=xml&name=Tori+Amos
    public List<MusicBrainzArtistInfo> artistSearch(String artistName) throws IOException {
        List<MusicBrainzArtistInfo> infos = new ArrayList<MusicBrainzArtistInfo>();
        String normalizedName = normalize(artistName);
        String query =  "artist/?type=xml&name="+ normalizedName;
        Document mbDoc = mbCommander.sendCommand(query);
        Element docElement = mbDoc.getDocumentElement();
        NodeList artists = docElement.getElementsByTagName("artist");
        for (int i = 0; i < artists.getLength(); i++) {
            Element artistElement = (Element) artists.item(i);
            String id = artistElement.getAttribute("id");
            String type = artistElement.getAttribute("type");
            int score = Integer.parseInt(artistElement.getAttribute("ext:score"));
            String name = XmlUtil.getElementContents(artistElement, "name");
            String sortName = XmlUtil.getElementContents(artistElement, "sort-name");
            Element lifespan = XmlUtil.getFirstElement(artistElement, "life-span");
            
            MusicBrainzArtistInfo mbai = new MusicBrainzArtistInfo();
            
            mbai.setID(id);
            mbai.setType(type);
            mbai.setScore(score);
            mbai.setName(name);
            mbai.setSortName(sortName);
            
            if (lifespan != null)  {
                int beginYear = getYear(lifespan.getAttribute("begin"));
                int endYear = getYear(lifespan.getAttribute("end"));
                mbai.setBeginYear(beginYear);
                mbai.setEndYear(endYear);
            }
            
            infos.add(mbai);
        }
        return infos;
    }
    
    // http://musicbrainz.org/ws/1/artist/8a338e06-d182-46f2-bd16-30a09bc840ba?type=xml&inc=url-rels+artist-rels
    
    public MusicBrainzArtistInfo populateArtistInfo(MusicBrainzArtistInfo artistInfo) throws IOException {
        return getArtistInfo(artistInfo.getID());
    }
    
    public MusicBrainzArtistInfo getArtistInfo(String artistMbid) throws IOException {
        String query =  "artist/" + artistMbid +"/?type=xml&inc=url-rels+sa-Official+artist-rels";
        Document mbDoc = mbCommander.sendCommand(query);
        Element docElement = mbDoc.getDocumentElement();
        Element artistElement = XmlUtil.getFirstElement(docElement, "artist");
        
        MusicBrainzArtistInfo mbai = new MusicBrainzArtistInfo();
        String artistType = artistElement.getAttribute("type");
        String name = XmlUtil.getElementContents(artistElement, "name");
        String sortName = XmlUtil.getElementContents(artistElement, "sort-name");
        
        mbai.setID(artistMbid);
        mbai.setType(artistType);
        mbai.setScore(100);
        mbai.setName(name);
        mbai.setSortName(sortName);
        
        // add the MusicBrainz link
        mbai.addURL("MusicBrainz", "http://musicbrainz.org/artist/" + artistMbid);
        
        Element lifespan = XmlUtil.getFirstElement(artistElement, "life-span");
        if (lifespan != null)  {
            int beginYear = getYear(lifespan.getAttribute("begin"));
            int endYear = getYear(lifespan.getAttribute("end"));
            mbai.setBeginYear(beginYear);
            mbai.setEndYear(endYear);
        }
        
        List<Node> relationNodes = XmlUtil.getDescendents(artistElement, "relation-list");
        for (int i = 0; i < relationNodes.size(); i++) {
            Element relationlist = (Element) relationNodes.get(i);
            if (relationlist.getAttribute("target-type").equals("Url")) {
                NodeList urls = relationlist.getElementsByTagName("relation");
                for (int j = 0; j < urls.getLength(); j++) {
                    Element url = (Element) urls.item(j);
                    String type = url.getAttribute("type");
                    String target = url.getAttribute("target");
                    mbai.addURL(type, target);
                }
            } else if (relationlist.getAttribute("target-type").equals("Artist")) {
                NodeList relations = relationlist.getElementsByTagName("relation");
                for (int j = 0; j < relations.getLength(); j++) {
                    Element relation = (Element) relations.item(j);
                    String id = relation.getAttribute("target");
                    mbai.addCollaborator(id);
                }
            }
        }
        
        // the asinSet keeps track of dup asins, so we only add an asin once per artist.
        Set<String> asinSet = new HashSet<String>();
        NodeList releaseNodes = docElement.getElementsByTagName("release");
        for (int i = 0; i < releaseNodes.getLength(); i++) {
            Element release = (Element) releaseNodes.item(i);
            String asin = XmlUtil.getElementContents(release, "asin");
            if (asin != null && !asinSet.contains(asin)) {
                String id = release.getAttribute("id");
                String type = release.getAttribute("type");

                // Only get official albums, ignoring live releases and singles
                if (type.equalsIgnoreCase("Album Official")) {
                    String title = XmlUtil.getElementContents(release, "title");
                    MusicBrainzAlbumInfo album = new MusicBrainzAlbumInfo();
                    album.setMbid(id);
                    album.addArtistId(artistMbid);
                    album.setTitle(title);
                    album.setAsin(asin);

                    // Get the release's urls
                    List<Node> releaseRelationNodes = XmlUtil.getDescendents(release, "relation-list");
                    for (int k = 0; k < releaseRelationNodes.size(); k++) {
                        Element relationlist = (Element) releaseRelationNodes.get(k);
                        if (relationlist.getAttribute("target-type").equals("Url")) {
                            NodeList urls = relationlist.getElementsByTagName("relation");
                            for (int j = 0; j < urls.getLength(); j++) {
                                Element url = (Element) urls.item(j);
                                String releaseType = url.getAttribute("type");
                                // skip the amazon asin url since we can reconstruct it
                                if (releaseType.equals("AmazonAsin")) {
                                    continue;
                                }
                                String target = url.getAttribute("target");
                                album.addURL(type, target);
                            }
                        }
                    }

                    mbai.addAlbum(album);
                    asinSet.add(asin);
                }
            }
        }
        
        // collect the list of artist relations
        return mbai;
    }


    private MusicBrainzAlbumInfo extractAlbumInfo(Element e) throws IOException {

        MusicBrainzAlbumInfo mba = new MusicBrainzAlbumInfo();
        mba.setMbid(e.getAttribute("id"));
        mba.setAsin(XmlUtil.getElementContents(e, "asin"));
        mba.setTitle(XmlUtil.getElementContents(e, "title"));

        // add the MusicBrainz link
        mba.addURL("MusicBrainz", "http://musicbrainz.org/release/" + mba.getMbid() + ".html");

        // Get release date
        List<Node> releaseEvents = XmlUtil.getDescendents(e, "release-event-list");
        if (releaseEvents.size() >= 1) {
            NodeList releases = ((Element) releaseEvents.get(0)).getElementsByTagName("event");
            for (int i = 0; i < releases.getLength(); i++) {
                Element event = (Element) releases.item(i);
                try {
                    mba.setReleaseDate(mbDateFormater.parse(event.getAttribute("date")).getTime());
                    break;
                } catch (ParseException ex) {}
            }
        }

        return mba;
    }


    public MusicBrainzAlbumInfo getAlbumInfo(String mbid) throws IOException {
        String query =  "release/" + mbid +"/?type=xml&inc=url-rels+tracks+release-events";
        Document mbDoc = mbCommander.sendCommand(query);
        Element docElement = mbDoc.getDocumentElement();

        Element albumElement = XmlUtil.getFirstElement(docElement, "release");
        MusicBrainzAlbumInfo mba = extractAlbumInfo(albumElement);

        // Get all tracks
        List<Node> trackNodes = XmlUtil.getDescendents(albumElement, "track-list");
        if (trackNodes.size() >= 1) {
            NodeList trackList = ((Element) trackNodes.get(0)).getElementsByTagName("track");
            for (int i = 0; i < trackList.getLength(); i++) {
                Element track = (Element) trackList.item(i);
                MusicBrainzTrackInfo trackInfo = new MusicBrainzTrackInfo();
                trackInfo.setMbid(track.getAttribute("id"));
                trackInfo.setTitle(XmlUtil.getElementContents(track, "title"));
                try {
                    trackInfo.setDuration(Integer.parseInt(XmlUtil.getElementContents(track, "duration")));
                } catch (NumberFormatException e) {}
                mba.addTrack(i+1, trackInfo);
            }
        }
        return mba;
    }
    
    
    private int getYear(String dateString) {
        if (dateString != null && dateString.length() >= 4) {
            dateString = dateString.substring(0, 4);
            int year = Integer.parseInt(dateString);
            return year;
        }
        return 0;
    }
    
    
    private String normalize(String s) throws IOException {
        if (s.indexOf(" ") >= 0) {
            s =  "\"" + s + "\"";
        }
        return URLEncoder.encode(s, "UTF-8");
    }


    /**
     * Test for artist
     * @param args
     * @throws IOException
     */
    public static void main2(String[] args) throws IOException {
        MusicBrainz mb = new MusicBrainz();
        List<MusicBrainzArtistInfo> infos = mb.artistSearch("weezer");
        for (MusicBrainzArtistInfo mbai : infos) {
            mbai.dump();
        }

        MusicBrainzArtistInfo best = infos.get(0);
        String id = best.getID();
        MusicBrainzArtistInfo weezer = mb.getArtistInfo(id);
        weezer.dump();
    }

    /**
     * Test for album
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        System.out.println("Run...");
        MusicBrainz mb = new MusicBrainz();
        
        /*
        MusicBrainzAlbumInfo aI = mb.getAlbumInfo("60e2f500-b11c-4275-b9a8-81b9adca2ee7");
        aI.dump();
        */


        /*
         * TEST FOR TRACK SEARCH
        for (Scored<MusicBrainzTrackInfo> sMI : mb.trackSearch("here Without You", null, "2386cd66-e923-4e8e-bf14-2eebe2e9b973", null, null)) {
            System.out.println("score:"+sMI.getScore());
            sMI.getItem().dump();
        }
         * */

        for (Scored<MusicBrainzAlbumInfo> sMI : mb.albumSearch("Clumsy", "Our Lady Peace", null)) {
            System.out.println("score:"+sMI.getScore());
            sMI.getItem().dump();
        }

    }

}
