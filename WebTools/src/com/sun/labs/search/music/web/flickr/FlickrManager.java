/*
 * FlickrManager.java
 *
 * Created on April 3, 2007, 8:21 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.search.music.web.flickr;

import com.aetrion.flickr.FlickrException;
import com.aetrion.flickr.Flickr;
import com.aetrion.flickr.photos.Photo;
import com.aetrion.flickr.photos.PhotoList;
import com.aetrion.flickr.photos.SearchParameters;
import com.aetrion.flickr.photos.PhotosInterface;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xml.sax.SAXException;

/**
 *
 * @author plamere
 */
public class FlickrManager {
    private final static String FLICKR_ID = "edced8597f71de94baa0505840b34a7b";
    private final static int MAX_PER_PAGE = 500;
    private final static boolean ONLY_ATTRIBUTION_LICENSE = false;
    private final static boolean ONLY_CC_LICENSE = false;
    private final static Image[] EMPTY = new Image[0];
    
    
    /**
     * Creates a new instance of FlickrManager
     */
    public FlickrManager() {
    }
    
    public Image[] getPhotosForArtist(String name, int maxCount) {
        String normalizedName = normalizeName(name);
        return getImagesByTags(normalizedName + " concert", true, maxCount, true);
    }
    
    private String normalizeName(String name) {
        name = name.replaceAll(" ", "");
        name = name.replaceAll("&", "%26");
        name = name.toLowerCase();
        name = name.replaceFirst("^the", "");
        return name;
    }
    
    private Image[] getImagesByTags(String text, boolean all, int max, boolean interesting) {
        Flickr flickr = new Flickr(FLICKR_ID);
        PhotosInterface photosInterface = flickr.getPhotosInterface();
        
        List<Image> list = new ArrayList<Image>(max);
        
        if (text.length() == 0) {
            return EMPTY;
        }
        
        if (max > 500) {
            max = 500;
        }
        
        SearchParameters search = new SearchParameters();
        String[] tags = text.split(" ");
        search.setTags(tags);
        
        if (ONLY_ATTRIBUTION_LICENSE) {
            search.setLicense("4");
        } else if (ONLY_CC_LICENSE) {
            search.setLicense("1,2,3,4,5,6");
        }
        if (all)  {
            search.setTagMode("all");
        } else {
            search.setTagMode("any");
        }
        if (interesting) {
            search.setSort(SearchParameters.INTERESTINGNESS_DESC);
        } else {
            search.setSort(SearchParameters.RELEVANCE);
        }
        
        try {
            PhotoList photos = null;
            
            photos = photosInterface.search(search, max, 1);
            for (Object oPhoto : photos) {
                Photo photo = (Photo) oPhoto;
                photo = photosInterface.getPhoto(photo.getId());
                Image image = new Image();
                image.setTitle(photo.getTitle());
                image.setImageURL(photo.getMediumUrl());
                image.setId(photo.getId());
                image.setCreatorRealName(photo.getOwner().getRealName());
                image.setCreatorUserName(photo.getOwner().getUsername());
                image.setPhotoPageURL(photo.getUrl());
                image.setSmallImageUrl(photo.getSmallUrl());
                image.setThumbNailImageUrl(photo.getSmallSquareUrl());
                list.add(image);
            }
        } catch (IOException ex) {
            error("Trouble talking to flickr");
            ex.printStackTrace();
            delay(1);
        } catch (SAXException ex) {
            error("Trouble parsing flickr results");
            ex.printStackTrace();
            delay(1);
        } catch (FlickrException ex) {
            error("Flickr is complaining " + ex);
            ex.printStackTrace();
            delay(1);
        } catch (Exception e) {
            error("Unexpected exception " + e);
            e.printStackTrace();
            delay(1);
        }
        return (Image[]) list.toArray(EMPTY);
    }
    
    private void error(String msg) {
        System.err.println("Flickr Error: " + msg);
    }
    
    private void delay(int secs) {
        try {
            Thread.sleep(secs * 1000L);
        } catch (InterruptedException ie) {
        }
    }
    
    public static void main(String[] args) {
        FlickrManager fm = new FlickrManager();
        
        Image[] images = fm.getPhotosForArtist("weezer", 10);
        for (Image image : images) {
            image.dump();
        }
    }
}
