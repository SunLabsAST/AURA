/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.items;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author mailletf
 */
public class RecsNTagsContainer implements IsSerializable {

    public ArrayList<ScoredC<ArtistCompact>> recs;
    public HashMap<String, ScoredTag> tagMap;

    public RecsNTagsContainer() {
    }

    public RecsNTagsContainer(ArrayList<ScoredC<ArtistCompact>> recs,
            HashMap<String, ScoredTag> tagMap) {
        this.recs = recs;
        this.tagMap = tagMap;
    }
}
