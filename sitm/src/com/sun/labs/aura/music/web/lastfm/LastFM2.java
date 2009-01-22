/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.web.lastfm;

import com.sun.labs.aura.music.web.Commander;
import com.sun.labs.aura.music.web.XmlUtil;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 *
 * @author plamere
 */
public class LastFM2 {

    private String API_KEY = "0d89e322b86fa8e4f68bf446e64b95fc";
    private Commander commander;

    public LastFM2() throws IOException {
        commander = new Commander("last.fm", "http://ws.audioscrobbler.com/2.0/", "&api_key=" + API_KEY);
        commander.setRetries(1);
        commander.setTimeout(10000);
        commander.setTraceSends(false);
        commander.setMinimumCommandPeriod(500);
    }

    public void setTrace(boolean trace) {
        commander.setTraceSends(trace);
    }

    public void setMinimumCommandPeriod(long period) {
        commander.setMinimumCommandPeriod(period);
    }

    public LastArtist2 getArtistInfoByName(String artistName) throws IOException {
        String url = getArtistInfoByNameURL(artistName);
        return getArtistInfoFromLastFM(url);
    }

    public LastArtist2 getArtistInfoByMBID(String mbid) throws IOException {
        String url = getArtistInfoByMbidURL(mbid);
        return getArtistInfoFromLastFM(url);
    }

    public LastAlbum2 getAlbumInfoByName(String artistName, String albumName) throws IOException {
        String url = getAlbumInfoByNameURL(artistName, albumName);
        return getAlbumInfoFromLastFM(url);
    }

    public LastAlbum2 getAlbumInfoByMBID(String mbid) throws IOException {
        String url = getAlbumInfoByMbidURL(mbid);
        return getAlbumInfoFromLastFM(url);
    }

    public LastTrack getTrackInfoByName(String artistName, String trackName) throws IOException {
        String url = getTrackInfoByNameURL(artistName, trackName);
        return getTrackInfoFromLastFM(url);
    }

    public LastTrack getTrackInfoByMBID(String mbid) throws IOException {
        String url = getTrackInfoByMbidURL(mbid);
        return getTrackInfoFromLastFM(url);
    }

    public SocialTag[] getTrackTopTagsByName(String artistName, String trackName) throws IOException {
        String url = getTrackTopTagsByNameURL(artistName, trackName);
        return getTrackTopTagsFromLastFM(url);
    }

    public SocialTag[] getTrackTopTagsByMBID(String mbid) throws IOException {
        String url = getTrackTopTagsByMbidURL(mbid);
        return getTrackTopTagsFromLastFM(url);
    }

    private LastArtist2 getArtistInfoFromLastFM(String url) throws IOException {
        LastArtist2 artist = new LastArtist2();

        Document doc = commander.sendCommand(url);

        checkStatus(doc);

        Element docElement = doc.getDocumentElement();
        Element artistNode = (Element) XmlUtil.getDescendent(docElement, "artist");

        if (artistNode != null) {
            artist.setName(XmlUtil.getElementContents(artistNode, "name"));
            artist.setMbid(XmlUtil.getElementContents(artistNode, "mbid"));
            artist.setUrl(XmlUtil.getElementContents(artistNode, "url"));

            List<Node> imageList = XmlUtil.getDescendents(artistNode, "image");

            if (imageList != null) {
                for (int i = 0; i < imageList.size(); i++) {
                    Element image = (Element) imageList.get(i);
                    String size = image.getAttribute("size");
                    String imageUrl = image.getTextContent();
                    if (size != null && imageUrl != null) {
                        if (size.equalsIgnoreCase("small")) {
                            artist.setSmallImage(imageUrl);
                        } else if (size.equalsIgnoreCase("medium")) {
                            artist.setMediumImage(imageUrl);
                        } else if (size.equalsIgnoreCase("large")) {
                            artist.setLargeImage(imageUrl);
                        }
                    }
                }
            }

            String streamable = XmlUtil.getDescendentText(artistNode, "streamable");
            artist.setStreamable("1".equals(streamable));

            Element statsNode = (Element) XmlUtil.getDescendent(artistNode, "stats");
            if (statsNode != null) {
                artist.setPlaycount(XmlUtil.getElementContentsAsInteger(statsNode, "playcount"));
                artist.setListeners(XmlUtil.getElementContentsAsInteger(statsNode, "listeners"));
            }

            Element bioNode = (Element) XmlUtil.getDescendent(artistNode, "bio");
            if (bioNode != null) {
                artist.setBioSummary(XmlUtil.getDescendentText(bioNode, "summary"));
                artist.setBioFull(XmlUtil.getDescendentText(bioNode, "content"));
            }
        }

        return artist;
    }

    private void checkStatus(Document doc) throws IOException {
        Element docElement = doc.getDocumentElement();
        if (!docElement.getTagName().equals("lfm")) {
            throw new IOException("Mising LFM elemement in lastfm resonse");
        }
        if (!docElement.getAttribute("status").equals("ok")) {
            String err = XmlUtil.getElementContents(docElement, "error");
            throw new IOException("LFM Error " + err);
        }
    }

    private LastAlbum2 getAlbumInfoFromLastFM(String url) throws IOException {
        LastAlbum2 album = new LastAlbum2();
        Document doc = commander.sendCommand(url);

        checkStatus(doc);

        Element docElement = doc.getDocumentElement();
        Element albumNode = (Element) XmlUtil.getDescendent(docElement, "album");

        if (albumNode != null) {
            album.setName(XmlUtil.getElementContents(albumNode, "name"));
            album.setMbid(XmlUtil.getElementContents(albumNode, "mbid"));
            album.setUrl(XmlUtil.getElementContents(albumNode, "url"));
            album.setArtistName(XmlUtil.getElementContents(albumNode, "artist"));
            album.setLfmID(XmlUtil.getElementContents(albumNode, "id"));
            album.setReleaseDate(XmlUtil.getElementContents(albumNode, "releasedate"));
            album.setListeners(XmlUtil.getElementContentsAsInteger(albumNode, "listeners"));
            album.setPlaycount(XmlUtil.getElementContentsAsInteger(albumNode, "playcount"));

            List<Node> imageList = XmlUtil.getDescendents(albumNode, "image");
            if (imageList != null) {
                for (int i = 0; i < imageList.size(); i++) {
                    Element image = (Element) imageList.get(i);
                    String size = image.getAttribute("size");
                    String imageUrl = image.getTextContent();
                    if (size != null && imageUrl != null) {
                        if (size.equalsIgnoreCase("small")) {
                            album.setSmallImage(imageUrl);
                        } else if (size.equalsIgnoreCase("medium")) {
                            album.setMediumImage(imageUrl);
                        } else if (size.equalsIgnoreCase("large")) {
                            album.setLargeImage(imageUrl);
                        } else if (size.equalsIgnoreCase("extralarge")) {
                            album.setHugeImage(imageUrl);
                        }
                    }
                }
            }

            Element wikiNode = (Element) XmlUtil.getDescendent(albumNode, "wiki");
            if (wikiNode != null) {
                album.setWikiSummary(XmlUtil.getDescendentText(wikiNode, "summary"));
                album.setWikiFull(XmlUtil.getDescendentText(wikiNode, "content"));
            }
        }
        return album;
    }

    private LastTrack getTrackInfoFromLastFM(String url) throws IOException {
        LastTrack track = new LastTrack();
        Document doc = commander.sendCommand(url);

        checkStatus(doc);

        Element docElement = doc.getDocumentElement();
        Element trackNode = (Element) XmlUtil.getDescendent(docElement, "track");

        if (trackNode != null) {
            track.setLfmid(XmlUtil.getElementContents(trackNode, "id"));
            track.setName(XmlUtil.getElementContents(trackNode, "name"));
            track.setMbid(XmlUtil.getElementContents(trackNode, "mbid"));
            track.setUrl(XmlUtil.getElementContents(trackNode, "url"));
            track.setDuration(XmlUtil.getElementContentsAsInteger(trackNode, "duration"));
            track.setStreamble(XmlUtil.getElementContentsAsInteger(trackNode, "streamable") == 1);
            track.setListeners(XmlUtil.getElementContentsAsInteger(trackNode, "listeners"));
            track.setPlaycount(XmlUtil.getElementContentsAsInteger(trackNode, "playcount"));

            // the artist info

            Element artistNode = (Element) XmlUtil.getDescendent(trackNode, "artist");
            if (artistNode != null) {
                track.setArtistName(XmlUtil.getElementContents(artistNode, "name"));
                track.setArtistMbid(XmlUtil.getElementContents(artistNode, "mbid"));
                track.setArtistUrl(XmlUtil.getElementContents(artistNode, "url"));
            }

            // the album info

            Element albumNode = (Element) XmlUtil.getDescendent(trackNode, "album");
            if (albumNode != null) {
                track.setAlbumPosition(sint(albumNode.getAttribute("position")));
                track.setAlbumArtist(XmlUtil.getElementContents(albumNode, "artist"));
                track.setAlbumTitle(XmlUtil.getElementContents(albumNode, "title"));
                track.setAlbumMbid(XmlUtil.getElementContents(albumNode, "mbid"));
                track.setAlbumUrl(XmlUtil.getElementContents(albumNode, "url"));

                List<Node> imageList = XmlUtil.getDescendents(albumNode, "image");
                if (imageList != null) {
                    for (int i = 0; i < imageList.size(); i++) {
                        Element image = (Element) imageList.get(i);
                        String size = image.getAttribute("size");
                        String imageUrl = image.getTextContent();
                        if (size != null && imageUrl != null) {
                            if (size.equalsIgnoreCase("small")) {
                                track.setSmallImage(imageUrl);
                            } else if (size.equalsIgnoreCase("medium")) {
                                track.setMediumImage(imageUrl);
                            } else if (size.equalsIgnoreCase("large")) {
                                track.setLargeImage(imageUrl);
                            } else if (size.equalsIgnoreCase("extralarge")) {
                                track.setLargeImage(imageUrl);
                            }
                        }
                    }
                }
            }

            // the top tags
            Element topTags = (Element) XmlUtil.getDescendent(trackNode, "toptags");
            List<Node> tagList = XmlUtil.getDescendents(topTags, "tag");
            if (tagList != null) {
                for (int i = 0; i < tagList.size(); i++) {
                    Node tagNode =  tagList.get(i);
                    String tag = XmlUtil.getDescendentText(tagNode, "tag");
                    track.addTopTag(tag);
                }
            }

            // the wiki info
            Element wikiNode = (Element) XmlUtil.getDescendent(trackNode, "wiki");
            if (wikiNode != null) {
                track.setWikiSummary(XmlUtil.getDescendentText(wikiNode, "summary"));
                track.setWikiContent(XmlUtil.getDescendentText(wikiNode, "content"));
            }
        }
        return track;
    }

    private SocialTag[] getTrackTopTagsFromLastFM(String url) throws IOException {
        Document doc = commander.sendCommand(url);
        checkStatus(doc);

        List<SocialTag> tagList = new ArrayList<SocialTag>();
        Element docElement = doc.getDocumentElement();
        Element topTagsNode = (Element) XmlUtil.getDescendent(docElement, "toptags");
        if (topTagsNode != null) {
            List<Node> tagNodes = XmlUtil.getDescendents(topTagsNode, "tag");
            if (tagNodes != null) {
                for (int i = 0; i < tagNodes.size(); i++) {
                    Element tagNode = (Element) tagNodes.get(i);
                    String name = XmlUtil.getDescendentText(tagNode, "name");
                    int count = sint(XmlUtil.getDescendentText(tagNode, "count"));
                    if (count <= 1) {
                        count = 1;
                    }
                    SocialTag st = new SocialTag(name, count);
                    tagList.add(st);
                }
            }
        }
        return tagList.toArray(new SocialTag[0]);
    }

    //http://ws.audioscrobbler.com/2.0/?method=artist.getinfo&artist=sonny%20bono&api_key=b25b959554ed76058ac220b7b2e0a026
    private String getArtistInfoByNameURL(String artistName) {
        String encodedArtistName = encodeName(artistName);
        return "?method=artist.getinfo&artist=" + encodedArtistName;
    }

    private String getArtistInfoByMbidURL(String mbid) {
        return "?method=artist.getinfo&mbid=" + mbid;
    }

    private String getAlbumInfoByMbidURL(String mbid) {
        return "?method=album.getinfo&mbid=" + mbid;
    }

    private String getTrackInfoByMbidURL(String mbid) {
        return "?method=track.getinfo&mbid=" + mbid;
    }

    private String getTrackInfoByNameURL(String artistName, String trackName) {
        String encodedArtistName = encodeName(artistName);
        String encodedTrackName = encodeName(trackName);
        return "?method=track.getinfo&artist=" + encodedArtistName + "&track=" + encodedTrackName;
    }

    private String getTrackTopTagsByMbidURL(String mbid) {
        return "?method=track.gettoptags&mbid=" + mbid;
    }

    private String getTrackTopTagsByNameURL(String artistName, String trackName) {
        String encodedArtistName = encodeName(artistName);
        String encodedTrackName = encodeName(trackName);
        return "?method=track.gettoptags&artist=" + encodedArtistName + "&track=" + encodedTrackName;
    }

    private String getAlbumInfoByNameURL(String artistName, String albumName) {
        String encodedArtistName = encodeName(artistName);
        String encodedAlbumName = encodeName(albumName);
        return "?method=album.getinfo&artist=" + encodedArtistName + "&album=" + encodedAlbumName;
    }

    private String encodeName(String artistName) {
        try {
            String encodedName = URLEncoder.encode(artistName, "UTF-8");
            // lastfm double encodes things (Crazy!)
            //  encodedName = URLEncoder.encode(encodedName, "UTF-8");
            return encodedName;
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    private int sint(String s) {
        int i = 0;
        try {
            if (s != null && s.length() > 0) {
                i = Integer.parseInt(s);
            }
        } catch (NumberFormatException ex) {
        }
        return i;
    }

    public static void main(String[] args) throws IOException {
        LastFM2 lfm2 = new LastFM2();

        if (false) { // artist test
            {
                LastArtist2 la2 = lfm2.getArtistInfoByName("deerhoof");
                la2.dump(true);
            }

            {
                LastArtist2 la2 = lfm2.getArtistInfoByName("sonny & cher");
                la2.dump(true);
            }

            {
                LastArtist2 la2 = lfm2.getArtistInfoByName("sonny bono");
                la2.dump(true);
            }

            {
                LastArtist2 la2 = lfm2.getArtistInfoByName("bono");
                la2.dump(true);
            }

            {
                LastArtist2 la2 = lfm2.getArtistInfoByMBID("11eabe0c-2638-4808-92f9-1dbd9c453429");
                la2.dump(true);
            }

            {
                LastArtist2 la2 = lfm2.getArtistInfoByName("sonny & crap");
                la2.dump(true);
            }
        } else if (false) { // album test

            {
                LastAlbum2 album = lfm2.getAlbumInfoByName("the beatles", "revolver");
                album.dump();
            }
            {
                LastAlbum2 album = lfm2.getAlbumInfoByName("deerhoof", "friend opportunity");
                album.dump();
            }
            {
                LastAlbum2 album = lfm2.getAlbumInfoByName("weezer", "weezer");
                album.dump();
            }
            {
                LastAlbum2 album = lfm2.getAlbumInfoByName("cher", "believe");
                album.dump();
            }
        } else if (true) {
            {
                LastTrack track = lfm2.getTrackInfoByName("cher", "believe");
                track.dump();
            }
            {
                LastTrack track = lfm2.getTrackInfoByName("the beatles", "revolution");
                track.dump();
            }
            {
                LastTrack track = lfm2.getTrackInfoByName("dave brubeck", "take five");
                track.dump();
            }
            {
                LastTrack track = lfm2.getTrackInfoByName("daft punk", "harder, better, faster, stronger");
                track.dump();
                SocialTag[] tags = lfm2.getTrackTopTagsByName("daft punk", "harder, better, faster, stronger");
                for (SocialTag st : tags) {
                    System.out.println("   " + st.getName() + " " + st.getFreq());
                }
            }
            {
                LastTrack track = lfm2.getTrackInfoByName("Aphex Twin", "?Mi-1=-a?Di(n)(?Fij(n-1)+Fexti(n-1))");
                track.dump();
            }
        }
    }

}
