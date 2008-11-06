/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
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
