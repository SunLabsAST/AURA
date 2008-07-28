/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.items;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 *
 * @author mailletf
 */
public class ArtistRecommendation implements IsSerializable {

    private ItemInfo[] explanation;
    private double score;
    private ArtistCompact aC;
    
    public ArtistRecommendation() {}
    
    public ArtistRecommendation(ArtistCompact aC, ItemInfo[] explanation, double score) {
        this.explanation = explanation;
        this.score = score;
        this.aC = aC;
    }
    
    public ArtistCompact getArtist() {
        return aC;
    }
    
    public ItemInfo[] getExplanation() {
        return explanation;
    }

    public double getScore() {
        return score;
    }
}