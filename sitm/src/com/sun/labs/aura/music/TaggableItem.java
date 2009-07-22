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

package com.sun.labs.aura.music;

import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.datastore.Item;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.Tag;
import java.util.List;
import java.util.Map;


public abstract class TaggableItem extends CrawlableItem {

    public final static String FIELD_SOCIAL_TAGS = "socialTags";
    public final static String FIELD_SOCIAL_TAGS_RAW = "socialTagsRaw";
    public final static String FIELD_AUTO_TAGS = "autoTags";
    public final static String FIELD_BIO_TAGS = "bioTags";
    public final static String FIELD_BLURB_TAGS = "blurbTags";
    public final static String FIELD_REVIEW_TAGS_EN = "reviewTagsEn";
    public final static String FIELD_BLOG_TAGS_EN = "blogTagsEn";

    public static enum TagType {
        SOCIAL,
        SOCIAL_RAW,
        BIO,
        BLURB,
        AUTO,
        REVIEW_EN,
        BLOG_EN
    }

    public TaggableItem(Item item, Item.ItemType type) {
        super(item, type);
    }

    public TaggableItem() {
    }

    /**
     * Converts a tag type enum value to the corresponding datastore field
     * @param tt type of tag
     * @return corresponding datastore field
     */
    protected static final String tagTypeToField(TagType tt) {
        switch (tt) {
            case SOCIAL:        return FIELD_SOCIAL_TAGS;
            case SOCIAL_RAW:    return FIELD_SOCIAL_TAGS_RAW;
            case BIO:           return FIELD_BIO_TAGS;
            case BLURB:         return FIELD_BLURB_TAGS;
            case AUTO:          return FIELD_AUTO_TAGS;
            case REVIEW_EN:     return FIELD_REVIEW_TAGS_EN;
            case BLOG_EN:       return FIELD_BLOG_TAGS_EN;
            default:            throw new RuntimeException("Invalid parameter '"+tt.toString()+"'");
        }
    }

    @Override
    public void defineFields(DataStore ds) throws AuraException {
        super.defineFields(ds);
    }

    /**
     * Gets tags for the artist
     * @param tt type of tags
     * @return list of tags
     */
    public List<Tag> getTags(TagType tt) {
        return getTagsAsList(tagTypeToField(tt));
    }

    /**
     * Sets a tag to the artist
     * @param tt type of tag
     * @param tag name of the tag
     * @param count tag count
     */
    public void setTag(TagType tt, String tag, int count) {
        setTag(tagTypeToField(tt), tag, count);
    }

    /**
     * Sets a map of tags to the artist
     * @param tt type of tag
     * @param tags tag names and associated counts
     */
    public void setTags(TagType tt, Map<String, Integer> tags) {
        for (String tName : tags.keySet()) {
            setTag(tt, tName, tags.get(tName));
        }
    }

    /**
     * Increments the tag count for the given tag
     * @param tt type of tag
     * @param tag tag's name
     * @param count amount by which to increment the count
     */
    public void incrementTag(TagType tt, String tag, int count) {
        incrementTag(tagTypeToField(tt), tag, count);
    }

    /**
     * Increments the tag count for multiple tags
     * @param tt type of tag
     * @param tags map<tag_name, increment_count>
     */
    public void incrementTags(TagType tt, Map<String, Integer> tags) {
        for (String tName : tags.keySet()) {
            incrementTag(tt, tName, tags.get(tName));
        }
    }

    /**
     * Clears all of the tags of the given type
     * @param tt type of tag
     */
    public void clearTags(TagType tt) {
        clearTags(tagTypeToField(tt));
    }

}
