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

package com.sun.labs.aura.music.webservices;

import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.music.Artist;
import com.sun.labs.aura.music.MusicDatabase;
import com.sun.labs.aura.music.webservices.ItemFormatter.OutputType;
import com.sun.labs.aura.util.AuraException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author plamere
 */
public class ItemFormatterManager {

    private Map<ItemType, ItemFormatter> formatters = new HashMap<ItemType, ItemFormatter>();
    MusicDatabase mdb;

    public ItemFormatterManager(MusicDatabase mdb) {
        this.mdb = mdb;
        // The artist formatter
        addItemFormatter(ItemType.ARTIST, getArtistFormatter());
        addItemFormatter(ItemType.ALBUM, getDefaultFormatter(ItemType.ALBUM));
        addItemFormatter(ItemType.ARTIST_TAG, getDefaultFormatter(ItemType.ARTIST_TAG));
        addItemFormatter(ItemType.BLOGENTRY, getDefaultFormatter(ItemType.BLOGENTRY));
        addItemFormatter(ItemType.EVENT, getDefaultFormatter(ItemType.EVENT));
        addItemFormatter(ItemType.PHOTO, getDefaultFormatter(ItemType.PHOTO));
        addItemFormatter(ItemType.TRACK, getDefaultFormatter(ItemType.TRACK));
        addItemFormatter(ItemType.USER, getDefaultFormatter(ItemType.USER));
        addItemFormatter(ItemType.TAG_CLOUD, getDefaultFormatter(ItemType.TAG_CLOUD));
    }

    public String toXML(Item item, OutputType outputType, double score) {
        ItemFormatter formatter = getItemFormatter(item.getType());
        return formatter.toXML(item, outputType, score);
    }

    public String toXML(Item item, OutputType outputType) {
        ItemFormatter formatter = getItemFormatter(item.getType());
        return formatter.toXML(item, outputType, null);
    }

    private ItemFormatter getDefaultFormatter(ItemType type) {
        ItemFormatter formatter = new ItemFormatter(type);
        return formatter;
    }

    private ItemFormatter getArtistFormatter() {
        ItemFormatter formatter = new ItemFormatter(ItemType.ARTIST);

        formatter.addValueGetter("normpop", new ValueGetter() {

            @Override
            public Object getValue(Item item, String name) {
                try {
                    return Float.toString(mdb.artistGetNormalizedPopularity(new Artist(item)));
                } catch (AuraException ex) {
                    return null;
                }
            }
        });

        formatter.addValueGetter("photo", new ValueGetter() {

            @Override
            public Object getValue(Item item, String name) {
                Artist artist = new Artist(item);
                return Util.selectRandomFromCollection(artist.getPhotos());
            }
        });

        formatter.addValueGetter("track", new ValueGetter() {

            @Override
            public Object getValue(Item item, String name) {
                Artist artist = new Artist(item);
                return Util.selectRandomFromCollection(artist.getAudio());
            }
        });

        formatter.addValueGetter("video", new ValueGetter() {

            @Override
            public Object getValue(Item item, String name) {
                Artist artist = new Artist(item);
                return Util.selectRandomFromCollection(artist.getVideos());
            }
        });

        formatter.addToDataSet(OutputType.Small, "video");
        formatter.addToDataSet(OutputType.Small, "track");
        formatter.addToDataSet(OutputType.Small, "photo");
        formatter.addToDataSet(OutputType.Small, "normpop");
        formatter.addToDataSet(OutputType.Small,  Artist.FIELD_SPOTIFY);

        formatter.addToDataSet(OutputType.Medium, "video");
        formatter.addToDataSet(OutputType.Medium, "track");
        formatter.addToDataSet(OutputType.Medium, "photo");
        formatter.addToDataSet(OutputType.Medium, "normpop");
        formatter.addToDataSet(OutputType.Medium, Artist.FIELD_SPOTIFY);
        formatter.addToDataSet(OutputType.Medium, Artist.FIELD_BIOGRAPHY_SUMMARY);
        formatter.addToDataSet(OutputType.Medium, Artist.FIELD_URLS);
        formatter.addToDataSet(OutputType.Medium, Artist.FIELD_EVENTS);
        formatter.addToDataSet(OutputType.Medium, Artist.FIELD_ALBUM);

        formatter.addToDataSet(OutputType.Large, "video");
        formatter.addToDataSet(OutputType.Large, "track");
        formatter.addToDataSet(OutputType.Large, "photo");
        formatter.addToDataSet(OutputType.Large, "normpop");
        formatter.addToDataSet(OutputType.Large, Artist.FIELD_SPOTIFY);
        formatter.addToDataSet(OutputType.Large, Artist.FIELD_BIOGRAPHY_SUMMARY);
        formatter.addToDataSet(OutputType.Large, Artist.FIELD_URLS);
        formatter.addToDataSet(OutputType.Large, Artist.FIELD_EVENTS);
        formatter.addToDataSet(OutputType.Large, Artist.FIELD_ALBUM);
        formatter.addToDataSet(OutputType.Large, Artist.FIELD_VIDEOS);
        formatter.addToDataSet(OutputType.Large, Artist.FIELD_PHOTOS);
        formatter.addToDataSet(OutputType.Large, Artist.FIELD_AUDIO);
        formatter.addToDataSet(OutputType.Large, Artist.FIELD_SOCIAL_TAGS);
        formatter.addToDataSet(OutputType.Large, Artist.FIELD_RELATED_ARTISTS);

        return formatter;
    }

    private void addItemFormatter(ItemType type, ItemFormatter formatter) {
        formatters.put(type, formatter);
    }

    public ItemFormatter getItemFormatter(ItemType type) {
        return formatters.get(type);
    }
}
