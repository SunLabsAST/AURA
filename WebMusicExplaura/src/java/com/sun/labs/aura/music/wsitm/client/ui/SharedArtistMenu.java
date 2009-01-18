/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui;

import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.ClientDataManager;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import com.sun.labs.aura.music.wsitm.client.ui.ContextMenu.ArtistDependentSharedMenu;

/**
 *
 * @author mailletf
 */
public class SharedArtistMenu extends ContextMenu implements ArtistDependentSharedMenu {

    protected ArtistCompact currArtist;
    protected ClientDataManager cdm;
    
    public SharedArtistMenu(ClientDataManager tCdm) {
        super();
        this.cdm = tCdm;

        addElement("View artist details", new ClickListener() {

           @Override
            public void onClick(Widget sender) {
                History.newItem("artist:" + currArtist.getId());
            }


        });
        addElement("View tag cloud", new ClickListener() {

            @Override
            public void onClick(Widget sender) {
                TagDisplayLib.showTagCloud("Tag cloud for "+currArtist.getName(),
                        currArtist.getDistinctiveTags(), TagDisplayLib.ORDER.SHUFFLE, cdm);
            }
        });

        addSeperator();
        
        addElement("Start new steerable from artist's top tags", new ClickListener() {

            @Override
            public void onClick(Widget sender) {
                cdm.setSteerableReset(true);
                History.newItem("steering:" + currArtist.getId());
            }
        });
        addElement("Add artist to steerable", new ClickListener() {

            @Override
            public void onClick(Widget sender) {
                cdm.getSteerableTagCloudExternalController().addArtist(currArtist);
            }
        });
        addElement("Add artist's top tags to steerable", new ClickListener() {

            @Override
            public void onClick(Widget sender) {
                cdm.getSteerableTagCloudExternalController().addTags(currArtist.getDistinctiveTags());
            }
        });  
    }

    public void showAt(Event e, ArtistCompact aC) {
        this.currArtist = aC;
        super.showAt(e);
    }
    
}
