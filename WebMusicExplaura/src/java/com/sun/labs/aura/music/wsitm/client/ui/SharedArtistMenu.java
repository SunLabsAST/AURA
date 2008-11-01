/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui;

import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.ClientDataManager;
import com.sun.labs.aura.music.wsitm.client.event.DataEmbededClickListener;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import com.sun.labs.aura.music.wsitm.client.ui.ContextMenu.ArtistDependentSharedMenu;

/**
 *
 * @author mailletf
 */
public class SharedArtistMenu extends ContextMenu implements ArtistDependentSharedMenu {

    protected ArtistCompact currArtist;
    protected ClientDataManager cdm;
    
    public SharedArtistMenu(ClientDataManager cdm) {
        super();
        this.cdm = cdm;

        addElement("View artist details",
                new DataEmbededClickListener<SharedArtistMenu>(this) {

           @Override
            public void onClick(Widget sender) {
                History.newItem("artist:" + data.currArtist.getId());
            }


        });
        addElement("View tag cloud",
                new DataEmbededClickListener<SharedArtistMenu>(this) {

            @Override
            public void onClick(Widget sender) {
                TagDisplayLib.showTagCloud("Tag cloud for "+data.currArtist.getName(),
                        data.currArtist.getDistinctiveTags(), TagDisplayLib.ORDER.SHUFFLE, data.cdm);
            }
        });

        addElement("Start new steerable from artist's top tags",
                new DataEmbededClickListener<SharedArtistMenu>(this) {

            @Override
            public void onClick(Widget sender) {
                data.cdm.setSteerableReset(true);
                History.newItem("steering:" + data.currArtist.getId());
            }
        });
        addElement("Add artist to steerable",
                new DataEmbededClickListener<SharedArtistMenu>(this) {

            @Override
            public void onClick(Widget sender) {
                data.cdm.getSteerableTagCloudExternalController().addArtist(data.currArtist);
            }
        });
        addElement("Add artist's top tags to steerable",
                new DataEmbededClickListener<SharedArtistMenu>(this) {

            @Override
            public void onClick(Widget sender) {
                data.cdm.getSteerableTagCloudExternalController().addTags(data.currArtist.getDistinctiveTags());
            }
        });  
    }

    public void showAt(Event e, ArtistCompact aC) {
        this.currArtist = aC;
        super.showAt(e);
    }
    
}
