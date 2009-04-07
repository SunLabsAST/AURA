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

package com.sun.labs.aura.music.crawler;

import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.datastore.StoreFactory;
import com.sun.labs.aura.music.Photo;
import com.sun.labs.aura.music.Video;
import com.sun.labs.aura.music.web.flickr.FlickrManager;
import com.sun.labs.aura.music.web.flickr.Image;
import com.sun.labs.aura.music.web.youtube.Youtube2;
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
    FlickrManager flickr;
    Youtube2 youtube;

    Util(FlickrManager flickr, Youtube2 youtube) {
        this.flickr = flickr;
        this.youtube = youtube;
    }

    List<Photo> collectFlickrPhotos(DataStore dataStore, String query, int count) throws AuraException, RemoteException, IOException {
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

    List<Video> collectYoutubeVideos(DataStore dataStore, String query, int count) throws AuraException, RemoteException, IOException {
        List<Video> videos = new ArrayList();
        List<YoutubeVideo> ytvideos = youtube.musicVideoSearch(query, count);
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
