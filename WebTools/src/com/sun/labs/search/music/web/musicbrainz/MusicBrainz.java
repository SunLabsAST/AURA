/*
 * MusicBrainz.java
 *
 * Created on February 21, 2007, 9:32 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.search.music.web.musicbrainz;

import com.sun.labs.search.music.web.Commander;
import com.sun.labs.search.music.web.XmlUtil;
import java.io.IOException;
import java.net.URLEncoder;
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
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy");
    
    /**
     * Creates a new instance of MusicBrainz
     */
    public MusicBrainz() throws IOException {
        mbCommander = new Commander("musicbrainz", "http://musicbrainz.org/ws/1/", "");
        mbCommander.setTraceSends(false);
        mbCommander.setTrace(false);
        mbCommander.setMinimumCommandPeriod(1000L);
    }
    
    public static void main(String[] args) throws IOException {
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
    
    public MusicBrainzArtistInfo getArtistInfo(String mbid) throws IOException {
        String query =  "artist/" + mbid +"/?type=xml&inc=url-rels+sa-Official+artist-rels";
        Document mbDoc = mbCommander.sendCommand(query);
        Element docElement = mbDoc.getDocumentElement();
        Element artistElement = XmlUtil.getFirstElement(docElement, "artist");
        
        MusicBrainzArtistInfo mbai = new MusicBrainzArtistInfo();
        String artistType = artistElement.getAttribute("type");
        String name = XmlUtil.getElementContents(artistElement, "name");
        String sortName = XmlUtil.getElementContents(artistElement, "sort-name");
        
        mbai.setID(mbid);
        mbai.setType(artistType);
        mbai.setScore(100);
        mbai.setName(name);
        mbai.setSortName(sortName);
        
        // add the MusicBrainz link
        mbai.addURL("MusicBrainz", "http://musicbrainz.org/artist/" + mbid);
        
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
                
                if (type.equalsIgnoreCase("Album Official")) {
                    String title = XmlUtil.getElementContents(release, "title");
                    MusicBrainzAlbumInfo album = new MusicBrainzAlbumInfo();
                    album.setId(id);
                    album.setTitle(title);
                    album.setAsin(asin);
                    mbai.addAlbum(album);
                    asinSet.add(asin);
                }
            }
        }
        
        // collect the list of artist relations
        return mbai;
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
}