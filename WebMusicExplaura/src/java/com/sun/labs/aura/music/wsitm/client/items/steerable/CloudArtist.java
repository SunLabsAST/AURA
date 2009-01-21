/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.items.steerable;

import com.google.gwt.user.client.ui.Image;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.ui.TagDisplayLib.TagColorType;
import java.util.HashSet;

/**
 *
 * @author mailletf
 */
public class CloudArtist extends CloudComposite {

    private Image artistImage;
        
    public CloudArtist(String artistId, String displayName, double weight, HashSet<CloudItem> items, Image artistImage) {
        super(artistId, displayName, weight, items);
        this.artistImage = artistImage;
    }

    public CloudArtist(ArtistCompact aC, double weight) {
        super(aC.getId(), aC.getName(), weight, new HashSet<CloudItem>());
        for (ItemInfo iI : aC.getDistinctiveTags()) {
            addItem(new CloudTag(iI));
        }
        this.artistImage = null; /// TBD PBL broke this
        this.weight = weight;
    }
    
    @Override
    public Image getImage() {
        return artistImage;
    }

    @Override
    public Image getIcon() {
        return new Image("icon-a.jpg");
    }

    @Override
    public TagColorType getTagColorType() {
        if (this.isSticky()) {
            return TagColorType.STICKY_ARTIST;
        } else {
            return TagColorType.ARTIST;
        }
    }

    @Override
    public CloudItemType getCloudItemType() {
        return CloudItemType.ARTIST;
    }
}