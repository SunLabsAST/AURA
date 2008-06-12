/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.crawler;

import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.StoreFactory;
import com.sun.labs.aura.music.Photo;
import com.sun.labs.aura.music.Video;
import com.sun.labs.aura.music.web.flickr.FlickrManager;
import com.sun.labs.aura.music.web.flickr.Image;
import com.sun.labs.aura.music.web.youtube.Youtube;
import com.sun.labs.aura.music.web.youtube.YoutubeVideo;
import com.sun.labs.aura.util.AuraException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * A set of utility methods shared by the various crawlers
 * @author plamere
 */
public class Util {

    DataStore dataStore;
    FlickrManager flickr;
    Youtube youtube;

    Util(DataStore dataStore, FlickrManager flickr, Youtube youtube) {
        this.dataStore = dataStore;
        this.flickr = flickr;
        this.youtube = youtube;
    }

    List<Photo> collectFlickrPhotos(String query, int count) throws AuraException, RemoteException, IOException {
        List<Photo> photos = new ArrayList();
        Image[] images = flickr.getPhotosForArtist(query, count);
        for (Image image : images) {
            Photo photo = null;
            Item item = dataStore.getItem(image.getImageURL());
            if (item == null) {
                item = StoreFactory.newItem(ItemType.PHOTO, image.getImageURL(), image.getTitle());
                photo = new Photo(item);
                photo.setCreatorRealName(image.getCreatorRealName());
                photo.setCreatorUserName(image.getCreatorUserName());
                photo.setImgUrl(image.getImageURL());
                photo.setThumbnailUrl(image.getThumbNailImageUrl());
                photo.setSmallImgUrl(image.getSmallImageUrl());
                photo.setPhotoPageUrl(image.getPhotoPageURL());
                photo.flush(dataStore);
            } else {
                photo = new Photo(item);
            }
            photos.add(photo);
        }
        return photos;
    }

    List<Video> collectYoutubeVideos(String query, int count) throws AuraException, RemoteException, IOException {
        List<Video> videos = new ArrayList();
        List<YoutubeVideo> ytvideos = youtube.musicSearch(query, count);
        for (YoutubeVideo ytvideo : ytvideos) {
            Item item = dataStore.getItem(ytvideo.getURL().toExternalForm());
            Video itemVideo = null;
            if (item == null) {
                item = StoreFactory.newItem(ItemType.VIDEO, ytvideo.getURL().toExternalForm(), ytvideo.getTitle());
                itemVideo = new Video(item);
                itemVideo.setThumbnailUrl(ytvideo.getThumbnail().toExternalForm());
                itemVideo.flush(dataStore);
            } else {
                itemVideo = new Video(item);
            }
            videos.add(itemVideo);
        }
        return videos;
    }
}
